plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

description = "mibrix-data-jpa: spring JPA 对数据访问、操作等功能"

dependencies {
    api(libs.spring.boot.starter.data.jpa)
}

apply {
    val publishingConfig: File by rootProject.extra
    from(publishingConfig)
}
