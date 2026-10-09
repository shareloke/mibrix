rootProject.name = "mibrix"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "aliyuu-gradle-plugin"
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
            isAllowInsecureProtocol = true
        }
    }
}

include("mibrix-bom", "mibrix-core", "mibrix-web-ui", "mibrix-script")

val excludeProjectDirs by gradle.extra(arrayOf("mibrix-data", "mibrix-sample", "mibrix-starter"))
val projectPrefix = "mibrix-"

rootDir.listFiles { file -> file.isDirectory() && file.name in excludeProjectDirs }?.forEach { file ->
    val pName = file.name
    file.listFiles { rFile ->
        rFile.isDirectory && rFile.parentFile.name == pName && rFile.name.startsWith(projectPrefix)
    }?.forEach {
        include(":${pName}:${it.name}")
    }
}
