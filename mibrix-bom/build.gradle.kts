description = "mibrix-bom: 项目依赖、版本管理"

javaPlatform {
    allowDependencies()
}

dependencies {

    api(platform(libs.spring.boot.dependencies))
    api(platform(libs.springdoc.bom))
}

apply {
    val publishingConfig: () -> File by rootProject.extra
    from(publishingConfig())
}
