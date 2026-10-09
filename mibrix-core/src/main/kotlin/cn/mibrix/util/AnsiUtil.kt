/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.util

import java.util.logging.Level

/**
 * 描述：控制台颜色字体输出
 * <pre>
 * FINEST  -> TRACE
 * FINER   -> DEBUG
 * FINE    -> DEBUG
 * CONFIG  -> INFO
 * INFO    -> INFO
 * WARNING -> WARN
 * SEVERE  -> ERROR
 * </pre>
 * @author shareloke
 * @date 2026-09-30
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object AnsiUtil {
    private val enableColor: Boolean by lazy { detectColor() }
    private var level: Level = Level.CONFIG
    const val RESET = "\u001B[0m"
    const val DEFAULT = "\u001B[39m"
    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val MAGENTA = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"

    @JvmStatic
    fun detectColor(): Boolean {
        // 1. 显式禁用（NO_COLOR 标准 https://no-color.org）
        if (System.getenv("NO_COLOR") != null) return false

        // 2. 显式强制启用
        if (System.getenv("FORCE_COLOR") != null) return true

        // 3. CI 环境通常支持颜色但 console() 为 null
        if (System.getenv("CI") != null) return true

        // 4. TERM 变量存在 → 大概率支持
        val term = System.getenv("TERM") ?: ""
        if (term.contains("color", ignoreCase = true) || term != "dumb") return true

        // 5. Windows Terminal / VS Code Terminal 等现代终端
        if (System.getenv("WT_SESSION") != null) return true // Windows Terminal
        if (System.getenv("TERMINAL_EMULATOR") != null) return true // JetBrains Terminal

        // 6. 最后兜底：console() 非空 → 一定是真终端
        if (System.console() != null) return true

        // 7. 默认开启（IDE 控制台也能渲染 ANSI）
        return true
    }

    /**
     * set logger Level
     *
     * @param l
     * @return
     */
    @JvmStatic
    fun setLevel(l: Level): Level {
        val old = level
        level = l
        return old
    }

    private fun logFmt(msg: String, color: String) = if (enableColor) {
        println(fmtLogColorStr(msg, color))
    } else {
        println(msg)
    }

    @JvmStatic
    fun fmtLogColorStr(msg: String, colorCode: String): String = "$colorCode[系统日志] -- $msg $RESET"

    /**
     * get current logger Level
     *
     * @return
     */
    @JvmStatic
    fun level(): Level = level

    @JvmStatic
    fun black(msg: String) = fmtLogColorStr(msg, BLACK)

    @JvmStatic
    fun printBlack(msg: String) = logFmt(msg, BLACK)

    @JvmStatic
    fun red(msg: String) = fmtLogColorStr(msg, RED)

    @JvmStatic
    fun printRed(msg: String) = logFmt(msg, RED)

    @JvmStatic
    fun green(msg: String) = fmtLogColorStr(msg, GREEN)

    @JvmStatic
    fun printGreen(msg: String) = logFmt(msg, GREEN)

    @JvmStatic
    fun yellow(msg: String) = fmtLogColorStr(msg, YELLOW)

    @JvmStatic
    fun printYellow(msg: String) = logFmt(msg, YELLOW)

    @JvmStatic
    fun blue(msg: String) = fmtLogColorStr(msg, BLUE)

    @JvmStatic
    fun printBlue(msg: String) = logFmt(msg, BLUE)

    @JvmStatic
    fun magenta(msg: String) = fmtLogColorStr(msg, MAGENTA)

    @JvmStatic
    fun printMagenta(msg: String) = logFmt(msg, MAGENTA)

    @JvmStatic
    fun cyan(msg: String) = fmtLogColorStr(msg, CYAN)

    @JvmStatic
    fun printCyan(msg: String) = logFmt(msg, CYAN)

    @JvmStatic
    fun white(msg: String) = fmtLogColorStr(msg, WHITE)

    @JvmStatic
    fun printWhite(msg: String) = logFmt(msg, WHITE)
}
