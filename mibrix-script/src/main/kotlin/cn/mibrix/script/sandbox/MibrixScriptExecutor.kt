/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.sandbox

import cn.mibrix.script.meta.MibrixScriptContextDefinition
import org.springframework.stereotype.Component

/**
 *  脚本执行器
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Component
class MibrixScriptExecutor(private val sandbox: MibrixScriptSandbox) {

    fun execute(definition: MibrixScriptContextDefinition): Map<out String?, Any?> {
        val script = definition.script
        val timeoutMs = definition.timeoutMs
        val outputVar = definition.outputVar
        val injected = definition.variables
        val output = when (val returned = sandbox.execute(script, injected, timeoutMs)) {
            null -> emptyMap()
            is Map<*, *> -> returned.entries.associate { it.key.toString() to it.value }
            else -> mapOf(outputVar to returned)
        }
        return output
    }
}
