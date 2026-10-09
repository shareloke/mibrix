/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.exception

/**
 * 框架通用断言错误码。
 *
 * @author shareloke
 * @date 2026-09-30
 */
interface CommonErrChecker : ErrorCodeResponse, MibrixChecker {

    override fun throwEx() = throw MibrixException(this)
    override fun throwEx(message: String) = throw MibrixException(this, message)
}
