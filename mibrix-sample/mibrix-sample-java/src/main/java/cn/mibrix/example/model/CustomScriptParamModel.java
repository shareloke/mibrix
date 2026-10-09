/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.example.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 自定义脚本参数模型
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Getter
@Setter
@Schema(description = "自定义脚本参数模型")
public class CustomScriptParamModel {

    @Schema(description = "脚本内容", example = "int a = 10; int b = a * num; System.out.print(b^2); return a * num * 10;")
    private String script;

    @Schema(description = "脚本参数", example = "{\"num\": 2}")
    private Map<String, Object> params;
}
