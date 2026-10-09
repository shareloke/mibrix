/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 项目示例Kotlin应用
 *
 * @author shareloke
 * @date 2026-10-05
 */
@SpringBootApplication
class MibrixSampleKotlinApplication

fun main(args: Array<String>) {
    runApplication<MibrixSampleKotlinApplication>(*args)
}
