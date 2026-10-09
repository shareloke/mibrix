description = "mibrix-bom: 项目依赖、版本管理"

javaPlatform {
    allowDependencies()
}

dependencies {

    api(platform(libs.spring.boot.dependencies))
    api(platform(libs.springdoc.bom))
    api(platform(libs.hutool.bom))

    // inside project dependencies
    constraints {
        val mutableProject = arrayOf("mibrix-starter", "mibrix-data")
        rootProject.subprojects.filterNot { it.name == project.name || it.name in mutableProject }.forEach {
            api(project(it.path))
        }
    }
}

apply {
    val publishingConfig: File by rootProject.extra
    from(publishingConfig)
}
