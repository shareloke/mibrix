/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.util

import cn.hutool.core.util.StrUtil as HuToolStrUtil
import cn.mibrix.core.constant.MibrixConsts.STR_CHARS

/**
 * 字符串工具类
 *
 * @author shareloke
 * @date 2026-09-30
 */
object StrUtil : HuToolStrUtil() {

    /**
     * 生成随机字符串
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun randomStr(length: Int) = buildString {
        repeat(length) {
            append(STR_CHARS.random())
        }
    }
}
