import java.util.*

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

// 项目全局变量
val projectGroup = "cn.mibrix"
val projectVersion = libs.versions.mibrix.get()
val projectBomModule = "mibrix-bom"
val snapshotName = "snapshot"
val releaseName = "release"
val snapshotDirName = "$snapshotName-deploy"
val releaseDirName = "$releaseName-deploy"
val nexusReleaseUrl = "https://central.sonatype.com/api/v1/publisher"
val nexusSnapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
val isSnapshot = providers.gradleProperty(snapshotName).map { it.toBoolean() }
    .getOrElse(false) || gradle.startParameter.taskNames.firstOrNull()?.contains(snapshotName, true) == true

// 对外暴露的函数，子项目可以调用这些函数
val excludeProjectDirs: Array<String> by gradle.extra
// 获取发布配置文件内容
val publishingConfig by extra(rootProject.file("config/gradle/publishing.gradle.kts"))
// 驼峰命名法转为大驼峰命名法
val capitalizeEachWord by extra { str: String ->
    return@extra str.replace("-", " ").split(" ").filter { it.isNotEmpty() }.joinToString(" ") { word ->
        word.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }
}

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

    if (project.name !in excludeProjectDirs) {

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
                source.setFrom("src/main/kotlin", "src/main/java")
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

                java {
                    target("src/**/*.java")
                    targetExclude("${layout.buildDirectory}/**/*.java", "**/generated-sources/**")
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

                withType<Javadoc> {
                    (options as StandardJavadocDocletOptions).apply {
                        // 允许未知标签（如 @date）
                        tags("date:a:Date")
                        // 可选：静默输出，减少控制台噪音
                        addStringOption("Xdoclint:none", "-quiet")
                    }

                    // 忽略 Javadoc 警告和错误，避免阻断构建
                    isFailOnError = false
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
        }

        // 6. 发布任务时序控制 (✅ 按模块精准清理 staging 目录，替代全局 clean)
        tasks.matching { it.name.startsWith("publishAllPublicationsTo") }.configureEach {
            dependsOn(cleanModuleStaging)
            mustRunAfter(cleanModuleStaging) // 双重保障：即使被其他任务间接触发，也确保清理先执行

            // Snapshot 发布后清理不必要的校验和签名文件（JReleaser 会重新生成）
            if (isSnapshot) {
                doLast {
                    val deployDir = moduleDeployDir.get().asFile
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
}

// ==================== 根项目：JReleaser 与聚合发布任务 ====================

// JReleaser 全局配置 (必须在根项目)
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

// 加载应用 Git Hooks 与发布任务配置
apply {
    from(rootProject.file("config/gradle/git-hooks.gradle.kts"))
    from(rootProject.file("config/gradle/publish-tasks.gradle.kts"))
}
