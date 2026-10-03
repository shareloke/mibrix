description = "mibrix-core: 通用模型、异常、工具、API返回体、分页等"

dependencies {
    implementation(platform(project(":mibrix-bom")))
    api(libs.springdoc.common)
}

kotlin {
    jvmToolchain(property("java.version")?.toString()?.toInt() ?: 17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
        suppressWarnings = true
    }
}

apply {
    val publishingConfig: () -> File by rootProject.extra
    from(publishingConfig())
}
