plugins {
    alias(libs.plugins.kotlin.spring)
}

description = "mibrix-script: 自定义脚本操作功能"

dependencies {
    implementation(platform(project(":mibrix-bom")))
    implementation(project(":mibrix-core"))
    api(libs.groovy.core)
    implementation(libs.groovy.json)
    implementation(libs.groovy.sandbox)
    implementation(libs.groovy.jsr223)
    implementation(libs.jakarta.annotation.api)
    testImplementation(kotlin("test"))
}

apply {
    val publishingConfig: File by rootProject.extra
    from(publishingConfig)
}
