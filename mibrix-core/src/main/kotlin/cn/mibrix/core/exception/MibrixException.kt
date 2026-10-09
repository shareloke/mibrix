/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.exception

import cn.mibrix.core.baseErrorCode
import cn.mibrix.core.baseErrorMessage

/**
 * 统一异常基类：携带 CommonErrCode，便于 @RestControllerAdvice 统一转 ApiResult。
 *
 * @author shareloke
 * @date 2026-10-06
 */
open class MibrixException(override val code: Int, override val message: String, cause: Throwable? = null) :
    RuntimeException(message, cause), ErrorCodeResponse {

    @JvmOverloads
    constructor(errMsg: String? = null) : this(baseErrorCode, errMsg ?: baseErrorMessage)

    @JvmOverloads
    constructor(err: CommonErrChecker, errMsg: String? = null, cause: Throwable? = null) : this(
        err.code,
        errMsg ?: err.message,
        cause,
    )
}
