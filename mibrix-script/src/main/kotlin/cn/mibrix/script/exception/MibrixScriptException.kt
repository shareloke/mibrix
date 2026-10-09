/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.exception

import cn.mibrix.core.baseErrorCode
import cn.mibrix.core.baseErrorMessage
import cn.mibrix.core.exception.CommonErrChecker
import cn.mibrix.core.exception.MibrixException

/**
 * 脚本执行操作异常
 *
 * @author shareloke
 * @date 2026-10-08
 */
class MibrixScriptException(
    override val code: Int,
    override val message: String,
    override val cause: Throwable? = null,
) : MibrixException(code, message, cause) {

    @JvmOverloads
    constructor(message: String? = null) : this(baseErrorCode, message ?: baseErrorMessage)

    @JvmOverloads
    constructor(err: CommonErrChecker, errMsg: String? = null, cause: Throwable? = null) : this(
        err.code,
        errMsg ?: err.message,
        cause,
    )
}
