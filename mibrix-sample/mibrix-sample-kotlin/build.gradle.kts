plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

description = "mibrix-sample-kotlin: 项目示例Kotlin版本"

dependencies {
    implementation(platform(project(":mibrix-bom")))
    implementation(project(":mibrix-starter:mibrix-spring-boot-starter"))
    implementation(libs.springdoc.webmvc.scalar)
    runtimeOnly(libs.mysql.connector)
    testImplementation(libs.spring.boot.starter.test)
}

kotlin {
    jvmToolchain(property("java.version")?.toString()?.toInt() ?: 21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xjvm-default=all")
        suppressWarnings = true
    }
}

springBoot.mainClass.set("cn.mibrix.sample.MibrixSampleKotlinApplicationKt")
