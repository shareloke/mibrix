/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.constant

/**
 * 常量定义
 *
 * @author shareloke
 * @date 2026-10-06
 */
object MibrixConsts {

    const val ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    const val NUM_STR = "0123456789"

    val STR_CHARS
        get() = ALPHANUMERIC.toCharArray()

    val NUM_CHARS
        get() = NUM_STR.toCharArray()
}
