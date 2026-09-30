/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.constant

/**
 *
 *
 * @author shareloke
 * @date 2026-09-30
 */
interface ErrorCode {

    /** 业务错误码，"0" 为成功 */
    val code: String

    /** 默认提示信息（可被异常携带的信息覆盖） */
    val message: String
}
