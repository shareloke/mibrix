/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.sample.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 自定义脚本参数模型
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Schema(description = "自定义脚本参数模型")
data class CustomScriptParamModel(
    @field:Schema(description = "自定义脚本")
    val script: String,

    @field:Schema(description = "自定义脚本参数")
    val params: Map<String, Any> = emptyMap(),
)
