plugins {
    alias(libs.plugins.kotlin.spring)
}

description = "mibrix-web-ui: 项目Web UI 模块，提供编排控制台、后台管理ui等功能"

dependencies {
    implementation(platform(project(":mibrix-bom")))
    api(libs.spring.boot.starter.web)
}

val frontendDir = file("${projectDir}/src/main/resources/ui")
val staticDir = file("${projectDir}/src/main/resources/static")

// ==================== 安装前端依赖 Task ====================
tasks.register<Exec>("pnpmInstall") {
    group = "frontend"
    description = "Installs frontend dependencies via pnpm"
    workingDir = frontendDir
    commandLine("pnpm", "install", "--frozen-lockfile")

    // 仅在 package.json 或 pnpm-lock.yaml 变化时重新执行
    inputs.file("${frontendDir}/package.json")
    inputs.file("${frontendDir}/pnpm-lock.yaml")
    outputs.dir("${frontendDir}/node_modules")
}

// ==================== 前端构建 Task ====================
tasks.register<Exec>("buildFrontend") {
    group = "frontend"
    description = "Builds frontend project with pnpm build"
    dependsOn("pnpmInstall")
    workingDir = frontendDir
    commandLine("pnpm", "build")

    // 增量构建：仅当源码或配置变化时重新构建
    inputs.dir("${frontendDir}/src")
    inputs.file("${frontendDir}/package.json")
    inputs.file("${frontendDir}/vite.config.ts") // 根据实际配置文件调整
    outputs.dir("${frontendDir}/dist") // ⚠️ 修改为你实际的构建输出目录名
}

// ==================== 前端清理 Task ====================
tasks.register<Delete>("cleanFrontendDist") {
    group = "frontend"
    description = "Cleans frontend build output"
    delete("${frontendDir}/dist")
}

// ==================== 拷贝前端产物到 static 目录 ====================
tasks.register<Copy>("copyFrontendToStatic") {
    group = "frontend"
    description = "Copies frontend build output to Spring Boot static resources"
    dependsOn("buildFrontend")

    from("${frontendDir}/dist") // ⚠️ 与 buildFrontend 的输出目录一致
    into(staticDir)

    // 拷贝前先清理旧的 static 内容，避免残留文件
    doFirst {
        delete(staticDir)
    }
}

// 确保处理资源之前，前端已经构建并拷贝完毕
tasks.named("processResources") {
    dependsOn("copyFrontendToStatic")
}

// ==================== 排除 resource/ui 源码，减小 JAR 体积 ====================
tasks.named<Jar>("jar") {
    exclude("ui/**")
}

tasks.named<Jar>("sourcesJar") {
    // sourcesJar 只需打包源码，不应包含前端构建产物
    exclude("ui/**")
    exclude("static/**")
}

tasks.named("clean") {
    dependsOn("cleanFrontendDist")
}

kotlin {
    jvmToolchain(property("java.version")?.toString()?.toInt() ?: 21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
        suppressWarnings = true
    }
}

apply {
    val publishingConfig: File by rootProject.extra
    from(publishingConfig)
}
