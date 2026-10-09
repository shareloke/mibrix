/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.example.service;


import cn.mibrix.core.exception.MibrixChecker;
import cn.mibrix.example.model.CustomScriptParamModel;
import cn.mibrix.script.exception.MibrixScriptErrEnum;
import cn.mibrix.script.exception.MibrixScriptException;
import cn.mibrix.script.sandbox.MibrixScriptExecutor;
import cn.mibrix.script.meta.MibrixScriptContextDefinition;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 脚本 API 服务
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Service
public class DemoScriptApiService {

    @Resource
    private MibrixScriptExecutor scriptExecutor;

    public Object runScript(CustomScriptParamModel paramModel) {
        MibrixChecker.check(paramModel.getScript() != null, "脚本内容不能为空", MibrixScriptException.class);
        MibrixScriptErrEnum.SCRIPT_EMPTY.check(paramModel.getScript().isBlank());
        return scriptExecutor.execute(new MibrixScriptContextDefinition(paramModel.getScript(), paramModel.getParams()));
    }
}
