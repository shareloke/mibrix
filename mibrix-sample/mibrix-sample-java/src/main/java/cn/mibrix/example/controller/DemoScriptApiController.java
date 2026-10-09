/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.example.controller;


import cn.mibrix.core.api.ApiResult;
import cn.mibrix.example.model.CustomScriptParamModel;
import cn.mibrix.example.service.DemoScriptApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 脚本演示API接口管理
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Tag(name = "脚本演示API接口管理")
@RestController
@RequestMapping("/demo/script")
public class DemoScriptApiController {

    @Resource
    private DemoScriptApiService service;

    /**
     * 运行脚本
     * eg: "int a = 10; int b = a * num; System.out.print(b^2); return a * num * 10;"
     */
    @PostMapping("/run")
    @Operation(summary = "运行脚本")
    public ApiResult<Object> runScript(@RequestBody CustomScriptParamModel paramModel) {
        return ApiResult.ok(service.runScript(paramModel));
    }
}
