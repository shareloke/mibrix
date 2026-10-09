/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.annotation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 日志包装器
 *
 * @author shareloke
 * @date 2026-09-30
 */
object LogWrapper {

    fun getLogger(clazz: Class<*>): Logger = LoggerFactory.getLogger(clazz)

    fun getColorLogger(clazz: Class<*>): ColorLogger = ColorLogger(LoggerFactory.getLogger(clazz))
}
