// Java插件配置
configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("java.version")?.toString()?.toInt() ?: 21))
    }
}
