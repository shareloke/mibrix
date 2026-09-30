description = "mibrix-bom: 项目依赖、版本管理"

javaPlatform {
    allowDependencies()
}

dependencies {

    api(platform(libs.spring.boot.dependencies))
}


publishing {
    publications {
        create<MavenPublication>("mavenBom") {
            from(components["javaPlatform"])

            pom {
                val projectName = project.name
                val projectGithubUrl: String by project
                val licenseName: String by project
                val licenseUrl: String by project
                val developerList: String by project
                val capitalizeEachWord: (String) -> String by rootProject.extra

                name.set(capitalizeEachWord(projectName))
                description.set(project.description)
                url.set(projectGithubUrl)
                packaging = "pom"

                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }

                developers {
                    developer {
                        developerList.split("|").forEach {
                            val infoArr = it.split(";")
                            id.set(infoArr[0])
                            name.set(infoArr[1])
                            email.set(infoArr[2])
                        }
                    }
                }

                scm {
                    connection.set("scm:git:git:$projectGithubUrl")
                    developerConnection.set("scm:git:ssh:$projectGithubUrl")
                    url.set(projectGithubUrl)
                }
            }
        }
    }
}
