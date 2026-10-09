/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.annotation

import cn.mibrix.util.AnsiUtil
import org.slf4j.Logger

/**
 * 日志颜色包装器
 *
 * @author shareloke
 * @date 2026-09-30
 */
@Suppress("unused")
class ColorLogger(private val delegate: Logger) {

    companion object {

        private val enableColor: Boolean by lazy { AnsiUtil.detectColor() }

        private const val RESET = "\u001B[0m"
        private val COLORS = mapOf(
            "ERROR" to "\u001B[31m",
            "WARN" to "\u001B[33m",
            "INFO" to "\u001B[32m",
            "DEBUG" to "\u001B[36m",
            "TRACE" to "\u001B[90m",
        )
    }

    fun error(msg: String?, vararg args: Any?) = delegate.error(AnsiUtil.red(msg ?: ""), *args)
    fun error(msg: String?, cause: Throwable) = delegate.error(AnsiUtil.red(msg ?: ""), cause)
    fun warn(msg: String?, vararg args: Any?) = delegate.warn(AnsiUtil.yellow(msg ?: ""), *args)
    fun warn(msg: String?, cause: Throwable) = delegate.warn(AnsiUtil.yellow(msg ?: ""), cause)
    fun info(msg: String?, vararg args: Any?) = delegate.info(AnsiUtil.green(msg ?: ""), *args)
    fun info(msg: String?, cause: Throwable) = delegate.info(AnsiUtil.green(msg ?: ""), cause)
    fun debug(msg: String?, vararg args: Any?) {
        if (delegate.isDebugEnabled) delegate.debug(AnsiUtil.cyan(msg ?: ""), *args)
    }

    fun debug(msg: String?, cause: Throwable) {
        if (delegate.isDebugEnabled) delegate.debug(AnsiUtil.cyan(msg ?: ""), cause)
    }

    fun trace(msg: String?, vararg args: Any?) {
        if (delegate.isTraceEnabled) delegate.trace(AnsiUtil.blue(msg ?: ""), *args)
    }

    fun trace(msg: String?, cause: Throwable) {
        if (delegate.isTraceEnabled) delegate.trace(AnsiUtil.blue(msg ?: ""), cause)
    }
}
