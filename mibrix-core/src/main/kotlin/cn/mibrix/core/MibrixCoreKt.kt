/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core

import cn.mibrix.core.exception.CommonErrEnum
import cn.mibrix.core.exception.MibrixChecker
import cn.mibrix.core.exception.MibrixException

val baseOkCode = CommonErrEnum.SUCCESS.code
val baseOkMessage = CommonErrEnum.SUCCESS.message
val baseFailCode = CommonErrEnum.FAIL.code
val baseFailMessage = CommonErrEnum.FAIL.message
val baseErrorCode = CommonErrEnum.ERROR.code
val baseErrorMessage = CommonErrEnum.ERROR.message

inline fun <reified E : MibrixException> check(expr: Boolean) = MibrixChecker.check(expr, null, E::class.java)

inline fun <reified E : MibrixException> check(expr: Boolean, noinline messageProvider: () -> String = { "" }) =
    MibrixChecker.check(expr, messageProvider(), E::class.java)

inline fun <reified E : MibrixException, T> requireNotNull(obj: T?) =
    MibrixChecker.requireNotNull(obj, null, E::class.java)

inline fun <reified E : MibrixException, T> requireNotNull(obj: T?, noinline messageProvider: () -> String = { "" }) =
    MibrixChecker.requireNotNull(obj, messageProvider(), E::class.java)
