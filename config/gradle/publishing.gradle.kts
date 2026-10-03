// 发布配置 pom 信息
fun MavenPublication.pomCreate() {
    val projectName = project.name
    val projectGithubUrl = "https://github.com/shareloke/mibrix"
    val licenseName = "Apache-2.0"
    val licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    val developerList = arrayOf("Javen;shareloke;526412864@qq.com")
    val capitalizeEachWord: (String) -> String by rootProject.extra

    this.pom {

        val pName = capitalizeEachWord(projectName)
        name.set(projectName)
        description.set(project.project.description ?: pName)
        url.set(projectGithubUrl)

        // 许可证 信息
        licenses {
            license {
                name.set(licenseName)
                url.set(licenseUrl)
            }
        }

        // 开发者 信息
        developers {
            developer {
                developerList.forEach { devInfo ->
                    val infoArr = devInfo.split(";")
                    if (infoArr.size >= 3) {
                        id.set(infoArr[0])
                        name.set(infoArr[1])
                        email.set(infoArr[2])
                    }
                }
            }
        }

        // 代码管理 信息
        scm {
            connection.set("scm:git:git:$projectGithubUrl.git")
            developerConnection.set("scm:git:ssh:$projectGithubUrl.git")
            url.set(projectGithubUrl)
        }
    }
}

// 配置发布配置
configure<PublishingExtension> {
    publications {

        // 配置 java 项目的发布配置
        pluginManager.withPlugin("java") {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pomCreate()
            }
        }

        // 配置 java-platform 项目的发布配置
        pluginManager.withPlugin("java-platform") {
            create<MavenPublication>("mavenBom") {
                from(components["javaPlatform"])

                pomCreate()
            }
        }
    }
}
