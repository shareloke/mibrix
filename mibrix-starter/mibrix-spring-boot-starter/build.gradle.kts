description = "mibrix-spring-boot-starter: 快速启动器，对 SpringBoot 模块进行封装、依赖集合，提供统一的启动配置等功能"

dependencies {
    implementation(platform(project(":mibrix-bom")))
    api(project(":mibrix-core"))
    api(project(":mibrix-script"))
    api(project(":mibrix-web-ui"))
}

apply {
    val publishingConfig: File by rootProject.extra
    from(publishingConfig)
}
