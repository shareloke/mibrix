/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.exception

/**
 * 错误码信息抽离
 *
 * @author shareloke
 * @date 2026-09-30
 */
interface ErrorCodeResponse {

    /**
     * 业务提示码
     */
    val code: Int

    /**
     * 业务提示信息
     */
    val message: String
}
