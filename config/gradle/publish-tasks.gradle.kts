// 发布任务相关配置
val excludeProjectDirs: Array<String> by gradle.extra

// 聚合发布任务 (在根项目注册，统一调度所有子模块)
tasks.register("publishToMavenCentralSnapshot") {
    group = "publishing"
    description = "Build and publish SNAPSHOT version via JReleaser"

    // 依赖所有子模块的 Snapshot 发布任务
    subprojects.forEach { sub ->
        // 排除不需要发布的模块
        if (sub.name !in excludeProjectDirs) {
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
        if (sub.name !in excludeProjectDirs) {
            dependsOn("${sub.path}:publishAllPublicationsToReleaseRepository")
        }
    }
    finalizedBy(tasks.named("jreleaserDeploy"))
}

// 排除指定项目任务执行，多模块项目下排除一些目录的任务执行，目录只担任管理模块的角色不需要其他任何需要构建的操作
project.allprojects.filter { it.name in excludeProjectDirs }.forEach { excludedProject ->
    excludedProject.tasks.configureEach { enabled = false }
}
