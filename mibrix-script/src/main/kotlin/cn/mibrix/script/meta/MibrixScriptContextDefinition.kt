/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.meta

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 脚本上下文定义
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Schema(description = "脚本上下文定义")
data class MibrixScriptContextDefinition(
    @field:Schema(description = "脚本内容")
    val script: String,

    @field:Schema(description = "脚本上下文变量")
    val variables: Map<String, Any?>,

    @field:Schema(description = "超时时间，单位毫秒，默认5秒")
    val timeoutMs: Long,

    @field:Schema(description = "输出变量名，默认scriptResult")
    val outputVar: String? = "scriptResult",
) {
    constructor(script: String, variables: Map<String, Any> = emptyMap()) : this(script, variables, 5000)
}
