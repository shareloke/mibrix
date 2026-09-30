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

include("mibrix-bom", "mibrix-core")


