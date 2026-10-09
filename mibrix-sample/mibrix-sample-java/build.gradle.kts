plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

description = "mibrix-sample-java: 项目示例Java版本"

dependencies {
    implementation(platform(project(":mibrix-bom")))
    implementation(project(":mibrix-starter:mibrix-spring-boot-starter"))
    implementation(libs.springdoc.webmvc.scalar)
    compileOnly(libs.lombok)
    runtimeOnly(libs.mysql.connector)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.test)
}

springBoot.mainClass.set("cn.mibrix.sample.MibrixSampleJavaApplication")
