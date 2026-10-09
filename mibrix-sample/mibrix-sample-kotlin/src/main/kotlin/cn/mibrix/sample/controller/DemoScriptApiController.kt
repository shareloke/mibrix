/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.sample.controller

import cn.mibrix.core.api.ApiResult
import cn.mibrix.sample.model.CustomScriptParamModel
import cn.mibrix.sample.service.DemoScriptApiService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 自定义脚本演示接口管理
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Tag(name = "自定义脚本演示接口管理")
@RequestMapping("/demo/script")
@RestController
class DemoScriptApiController(private val service: DemoScriptApiService) {

    @PostMapping("/run")
    fun runScript(@RequestBody model: CustomScriptParamModel): ApiResult<Map<out String?, Any?>> =
        ApiResult.ok(service.runScript(model))
}
