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
