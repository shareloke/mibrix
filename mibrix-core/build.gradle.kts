description = "mibrix-core: 通用模型、异常、工具、API返回体、分页等"

dependencies{
    api(libs.springdoc.common)
}

dependencyManagement{
    imports{
        mavenBom(libs.springdoc.bom.get().toString())
    }
}
