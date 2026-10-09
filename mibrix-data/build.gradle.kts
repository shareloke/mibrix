description = "mibrix-data: 数据访问模块、数据操作、第三方数据管理等"

subprojects {
    dependencies {
        implementation(platform(project(":mibrix-bom")))
        implementation(project(":mibrix-core"))
    }
}

kotlin {
    jvmToolchain(property("java.version")?.toString()?.toInt() ?: 21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
        suppressWarnings = true
    }
}
