// 项目依赖、版本管理等配置文件
plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

description = "是一个“搭积木”式的理念打造的全栈低代码框架库"

extra["capitalizeEachWord"] = ::capitalizeEachWord
val taskNames = gradle.startParameter.taskNames
val isSnapshot = taskNames.isNotEmpty() && taskNames[0].contains("NexusSnapshot")
val projectGroup: String by project
val projectVersion: String by project
val projectBomModule: String by project
val projectExclude: String by project

/**
 * 驼峰命名法转为大驼峰命名法
 * @param str 原始字符串
 * @return 大驼峰命名法字符串
 * @example "helloWorld" -> "HelloWorld"
 */
fun capitalizeEachWord(str: String): String {
    return str.replace("-", " ").split(" ").filter { it.isNotEmpty() }.joinToString(" ") { word ->
        word.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }
}

allprojects {
    group = projectGroup
    version = "$projectVersion${if (isSnapshot) "-SNAPSHOT" else ""}"

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            name = "aliyun-maven-public"
            url = uri("https://maven.aliyun.com/repository/public")
            isAllowInsecureProtocol = true
        }
        gradlePluginPortal()
        google()
    }
}

subprojects {
    val rLibs = rootProject.libs

    apply {
        plugin("maven-publish")
        when (project.name) {
            projectBomModule -> plugin("java-platform")
            else -> {
                plugin("java-library")
                plugin(rLibs.plugins.spring.dependency.management.get().pluginId)
                plugin(rLibs.plugins.kotlin.jvm.get().pluginId)
                plugin(rLibs.plugins.detekt.get().pluginId)
                plugin(rLibs.plugins.spotless.get().pluginId)
            }
        }
    }

    if (project.name != projectBomModule) {
        dependencies {
            detektPlugins(rLibs.detekt.plugin.formatting)
        }

        // 配置代码检查(只负责代码质量检查)
        detekt {
            toolVersion = rLibs.versions.detekt.get()
            // 配置规则集，基于默认配置构建
            buildUponDefaultConfig = true
            // 自动修复 (Detekt 可以修复某些非格式化问题，如移除未使用的导入)
            autoCorrect = true
            parallel = true
            ignoreFailures = false
            source.setFrom("src/main/kotlin")
            config.setFrom("$rootDir/config/detekt/detekt.yml")
            baseline = file("${projectDir}/detekt/baseline.xml")
        }

        // 配置代码格式(只负责代码格式化）
        spotless {
            format("misc") {
                target("*.md", ".gitignore")
                trimTrailingWhitespace()
                endWithNewline()
            }

            kotlin {
                target("src/**/*.kt")
                targetExclude("${layout.buildDirectory}/**/*.kt", "**/generated-sources/**")
                ktlint().setEditorConfigPath("$rootDir/.editorconfig").editorConfigOverride(
                    mapOf(
                        // 配置代码风格
                        "ktlint_code_style" to "intellij_idea",
                        // 配置缩进
                        "continuation_indent_size" to 4,
                        // 禁用尾随逗号（可选）
                        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                        // 禁用通配符导入
                        "ktlint_standard_no-wildcard-imports" to "disabled",
                        // 禁用尾随逗号（可选）
                        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                        // 禁用类签名换行（可选）
                        "ktlint_standard_class-signature" to "disabled",
                        // 禁用类签名换行（可选）
                        "ktlint_standard_parameter-list-wrapping" to "disabled",
                    ),
                )
                // 移除行尾空格
                trimTrailingWhitespace()
                // 文件末尾强制添加空行
                endWithNewline()
                // 添加版权信息
                licenseHeaderFile("${rootDir}/config/spotless/copyright.kt", "package ")
            }
        }
    }
}


// ==================== Git Hooks 安装（仅根项目）====================
val installGitHooks by tasks.registering(Copy::class) {
    group = "build setup"
    description = "初始化安装 Git 钩子（仅根项目执行）"

    val gitDir = rootProject.file(".git")
    val targetDir = rootProject.file(".git/hooks")
    val sourceDir = rootProject.file("scripts/git-hooks")

    // ⭐ 核心守卫：非根项目或缺少必要目录时直接跳过
    onlyIf {
        project == rootProject && gitDir.exists() && sourceDir.exists()
    }

    inputs.dir(sourceDir).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(targetDir)

    from(sourceDir)
    into(targetDir)

    // ⭐ 强制 LF 换行符，彻底解决 Windows CRLF 导致的 bad interpreter
    filter { line -> line.replace("\r\n", "\n").replace("\r", "\n") }
    filteringCharset = Charsets.UTF_8.name()

    // ⭐ 设置可执行权限（Windows 自动忽略，Linux/macOS 生效）
    filePermissions { unix(0b111_101_101) }

    doLast {
        logger.lifecycle("✅ Git hooks installed successfully (root: ${rootProject.name})")
    }
}

// ==================== Pre-Commit / Pre-Push Tasks ====================
val smartPreCommitCheck by tasks.registering {
    group = "verification"
    description = "运行 pre-commit 钩子对所有模块进行增量校验"
    // 声明依赖，由 Gradle 统一调度，避免 ProcessBuilder 嵌套启动新 JVM
    dependsOn("spotlessApply", "detekt", "spotlessCheck")

    doFirst {
        val changedFiles = providers.exec {
            commandLine("git", "diff", "--cached", "--name-only", "--diff-filter=ACMR", "*.kt")
            workingDir(rootProject.projectDir)
        }.standardOutput.asText.get().trim()

        if (changedFiles.isBlank()) {
            logger.lifecycle("✅ 没有需要检查的 Kotlin 文件，跳过校验。")
        } else {
            logger.lifecycle("🔍 Pre-commit 检查文件:\n$changedFiles")
        }
    }

    doLast {
        // 格式化后将修改过的文件重新加入暂存区
        val formattedFiles = providers.exec {
            commandLine("git", "diff", "--cached", "--name-only", "--diff-filter=ACMR", "*.kt")
            workingDir(rootProject.projectDir)
        }.standardOutput.asText.get().trim()

        formattedFiles.lines().filter { it.isNotBlank() }.forEach { filePath ->
            if (rootProject.file(filePath).exists()) {
                providers.exec {
                    commandLine("git", "add", filePath)
                    workingDir(rootProject.projectDir)
                }
            }
        }
        logger.lifecycle("✅ Pre-commit 检查通过！")
    }
}

// 配置全局任务 git 提交检查
tasks {
    register("preCommitCheck") {
        group = "verification"
        description = "运行 pre-commit 钩子检查所有模块"
        dependsOn("smartPreCommitCheck")
    }

    register("prePushCheck") {
        group = "verification"
        description = "运行 pre-push 钩子检查所有模块"
        dependsOn("check")
    }
}

project.allprojects.filter { it.name in projectExclude.split(",") }.forEach { it ->
    it.tasks.forEach { it.enabled = false }
}
