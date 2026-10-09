/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.ui.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 项目Web UI 控制器，提供编排控制台、后台管理ui等功能
 *
 * @author shareloke
 * @date 2026-10-08
 */
@Controller
@RequestMapping
class MibrixWebUiController {

    @GetMapping
    @Suppress("FunctionOnlyReturningConstant")
    fun index(): String = "index"
}
