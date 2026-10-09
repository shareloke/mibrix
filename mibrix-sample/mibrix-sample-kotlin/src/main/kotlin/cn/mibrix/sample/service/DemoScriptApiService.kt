/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.sample.service

import cn.mibrix.core.check
import cn.mibrix.sample.model.CustomScriptParamModel
import cn.mibrix.script.exception.MibrixScriptException
import cn.mibrix.script.meta.MibrixScriptContextDefinition
import cn.mibrix.script.sandbox.MibrixScriptExecutor
import org.springframework.stereotype.Service

/**
 * 自定义脚本服务类
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Service
class DemoScriptApiService(private val scriptExecutor: MibrixScriptExecutor) {

    fun runScript(model: CustomScriptParamModel): Map<out String?, Any?> {
        check<MibrixScriptException>(model.script.isNotBlank()) { "脚本不能为空" }
        return scriptExecutor.execute(
            MibrixScriptContextDefinition(
                model.script,
                model.params,
            ),
        )
    }
}
