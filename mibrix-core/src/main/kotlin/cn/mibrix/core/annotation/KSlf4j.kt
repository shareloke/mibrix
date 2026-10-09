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
 * kotlin 日志注解 相当于 java @Slf4j
 *
 * @author shareloke
 * @date 2026-09-30
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KSlf4j {

    companion object {

        // 日志
        val <reified T> T.log: Logger
            inline get() = LoggerFactory.getLogger(T::class.java)

        // 自定义带颜色的日志
        val <reified T> T.cLog: ColorLogger
            inline get() = ColorLogger(LoggerFactory.getLogger(T::class.java))
    }
}
