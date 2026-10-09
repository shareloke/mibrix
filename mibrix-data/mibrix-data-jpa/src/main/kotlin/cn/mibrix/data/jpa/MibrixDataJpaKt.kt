/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.data.jpa

import cn.hutool.extra.spring.SpringUtil

val mibrixDao: MibrixDao by lazy {
    SpringUtil.getBean(MibrixDao::class.java) ?: throw IllegalArgumentException("MibrixDao not found")
}
