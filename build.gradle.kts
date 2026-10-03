import java.util.Base64

// 项目依赖、版本管理等配置文件
plugins {
    `maven-publish`
    signing
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

description = "是一个“搭积木”式理念打造的低代码库"

// 对外暴露的函数，子项目可以调用这些函数
extra["capitalizeEachWord"] = ::capitalizeEachWord
extra["publishingConfig"] = ::publishingConfig

// 项目全局变量
val projectGroup = "cn.mibrix"
val projectVersion = "2026.0.0.3"
val projectBomModule = "mibrix-bom"
val projectExclude = arrayOf("mibrix-starter")
val snapshotName = "snapshot"
val releaseName = "release"
val snapshotDirName = "$snapshotName-deploy"
val releaseDirName = "$releaseName-deploy"
val nexusReleaseUrl = "https://central.sonatype.com/api/v1/publisher"
val nexusSnapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
val isSnapshot = providers.gradleProperty(snapshotName).map { it.toBoolean() }
    .getOrElse(false) || gradle.startParameter.taskNames.firstOrNull()?.contains(snapshotName, true) == true

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

// 获取发布配置文件内容
fun publishingConfig() = rootProject.file("config/gradle/publishing.gradle.kts")

// ==================== 全局仓库配置 ====================
allprojects {
    group = projectGroup
    version = "$projectVersion${if (isSnapshot) "-SNAPSHOT" else ""}"

    // 项目仓库：本地仓库、中央仓库、阿里云仓库、插件仓库、Google仓库等
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

// ==================== 子项目配置 ====================
subprojects {
    val rLibs = rootProject.libs

    // 1. 应用基础插件
    apply {
        plugin("maven-publish")
        plugin("signing")
        plugin(rLibs.plugins.jreleaser.get().pluginId)
        // 根据项目名称应用不同的插件
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

    // 2. 代码质量与格式化工具配置 (非 BOM 模块)
    if (project.name != projectBomModule) {

        // 依赖管理
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

        // 配置Java相关任务
        apply {
            from(rootProject.file("config/gradle/java.gradle.kts"))
        }

        tasks {
            // 配置Java编译任务
            withType<JavaCompile> {
                options.encoding = "UTF-8"
            }

            // 生成Java文档
            existing(Javadoc::class) {
                options.encoding = "UTF-8"
            }

            // 配置Jar任务
            withType<Jar> {
                manifest {
                    attributes["Implementation-Title"] = project.name
                    attributes["Implementation-Version"] = project.version
                }
            }
        }
    }

    // 3. 签名配置
    signing {
        isRequired = !isSnapshot
        val signingKeyBase64: String by project
        val signingPassword: String by project
        val signingKey = String(Base64.getDecoder().decode(signingKeyBase64))
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }

    // 4. 发布仓库配置 (物理隔离 Snapshot 和 Release 目录)
    publishing {
        repositories {
            maven {
                name = snapshotName
                url = rootProject.layout.buildDirectory.dir(snapshotDirName).get().asFile.toURI()
            }
            maven {
                name = releaseName
                url = rootProject.layout.buildDirectory.dir(releaseDirName).get().asFile.toURI()
            }
        }
    }

    // 5. 按模块精准清理 staging 目录 (独立前置任务)
    val deployDirName = if (isSnapshot) snapshotDirName else releaseDirName

    // 计算当前模块在 staging 目录中的相对路径 (例如: cn/mibrix/mibrix-core)
    val moduleStagingPath = "${project.group.toString().replace('.', '/')}/${project.name}"
    val moduleDeployDir = rootProject.layout.buildDirectory.dir(deployDirName).map {
        it.dir(moduleStagingPath)
    }

    // 注册独立的清理任务
    val cleanModuleStaging by tasks.registering(Delete::class) {
        group = "publishing"
        description = "Clean staging directory for ${project.name} before publishing"
        val dir = moduleDeployDir.get().asFile
        if (dir.exists()) {
            dir.deleteRecursively()
            logger.lifecycle("🧹 Cleaned staging for ${project.name}: ${dir.relativeTo(rootProject.projectDir)}")
        }

        // Snapshot 模式下额外清理冗余校验和文件（在删除整个目录后其实已不需要，
        // 但保留此逻辑以应对仅部分清理的场景）
        if (isSnapshot) {
            doLast {
                if (dir.exists()) {
                    dir.walkTopDown()
                        .filter { it.isFile && it.extension in listOf("md5", "sha1", "sha256", "sha512", "asc") }
                        .forEach { file ->
                            file.delete()
                            logger.lifecycle("✅️ Removed redundant: ${file.relativeTo(rootProject.projectDir)}")
                        }
                }
            }
        }
    }

    // 6. 发布任务时序控制 (✅ 按模块精准清理 staging 目录，替代全局 clean)
    tasks.matching { it.name.startsWith("publishAllPublicationsTo") }.configureEach {
        dependsOn(cleanModuleStaging)
        mustRunAfter(cleanModuleStaging) // 双重保障：即使被其他任务间接触发，也确保清理先执行

        // Snapshot 发布后清理不必要的校验和签名文件（JReleaser 会重新生成）
        if (isSnapshot) {
            doLast {
                val deployDir = rootProject.layout.buildDirectory.dir(snapshotDirName).get().asFile
                if (deployDir.exists()) {
                    deployDir.walkTopDown()
                        .filter { it.isFile && it.extension in listOf("md5", "sha1", "sha256", "sha512", "asc") }
                        .forEach {
                            it.delete()
                            logger.lifecycle("✅️ Removed: ${it.relativeTo(deployDir)}")
                        }
                }
            }
        }
    }
}

// ==================== 根项目：JReleaser 与聚合发布任务 ====================

// 1. JReleaser 全局配置 (必须在根项目)
jreleaser {
    gitRootSearch = true

    signing {
        active = org.jreleaser.model.Active.ALWAYS
        pgp {
            armored = true
            mode = org.jreleaser.model.Signing.Mode.FILE
            artifacts.set(true)
            checksums.set(true)
        }
    }

    deploy {
        maven {
            pomchecker { enabled = false }

            // Release 配置 (Maven Central API)
            mavenCentral {
                create(releaseDirName) {
                    active = org.jreleaser.model.Active.RELEASE
                    url = nexusReleaseUrl
                    // ✅ 指向根项目的 build/release-deploy，收集所有子模块产物
                    stagingRepository(layout.buildDirectory.dir(releaseDirName).get())
                    applyMavenCentralRules = false
                    snapshotSupported = false
                    maxRetries = 1
                }
            }

            // Snapshot 配置 (Nexus2)
            nexus2 {
                create(snapshotDirName) {
                    active = org.jreleaser.model.Active.SNAPSHOT
                    snapshotUrl = nexusSnapshotUrl
                    stagingRepository(layout.buildDirectory.dir(snapshotDirName).get())
                    sign = false
                    checksums = false
                    sourceJar = true
                    javadocJar = true
                    snapshotSupported = true
                    closeRepository = false
                    releaseRepository = false
                    verifyPom = false
                }
            }
        }
    }
}

// 2. 聚合发布任务 (在根项目注册，统一调度所有子模块)
tasks.register("publishToMavenCentralSnapshot") {
    group = "publishing"
    description = "Build and publish SNAPSHOT version via JReleaser"

    // 依赖所有子模块的 Snapshot 发布任务
    subprojects.forEach { sub ->
        // 排除不需要发布的模块
        if (sub.name !in projectExclude) {
            dependsOn("${sub.path}:publishAllPublicationsToSnapshotRepository")
        }
    }
    // ✅ 产物全部写入 staging 目录后，再统一触发一次 JReleaser
    finalizedBy(tasks.named("jreleaserDeploy"))
}

tasks.register("publishToMavenCentralRelease") {
    group = "publishing"
    description = "Build and publish RELEASE version via JReleaser"

    subprojects.forEach { sub ->
        if (sub.name !in projectExclude) {
            dependsOn("${sub.path}:publishAllPublicationsToReleaseRepository")
        }
    }
    finalizedBy(tasks.named("jreleaserDeploy"))
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

// 排除指定项目任务执行，多模块项目下排除一些目录的任务执行，目录只担任管理模块的角色不需要其他任何需要构建的操作
project.allprojects.filter { it.name in projectExclude }.forEach { excludedProject ->
    excludedProject.tasks.configureEach { enabled = false }
}
