/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.exception

import cn.hutool.core.util.ObjectUtil

/**
 * 断言接口，用于在异常中携带业务错误码和默认提示信息。
 *
 * @author shareloke
 * @date 2026-09-30
 */
interface MibrixChecker {

    /**
     * 抛异常
     */
    @Throws(MibrixException::class)
    fun throwEx()

    /**
     * 抛指定异常信息
     *
     * @param message 异常信息
     */
    @Throws(MibrixException::class)
    fun throwEx(message: String)

    /**
     * 检查表达式是否为 true，为 true 时抛指定异常信息
     *
     * @param expr 表达式
     */
    fun check(expr: Boolean) {
        if (expr) {
            throwEx()
        }
    }

    /**
     * 检查表达式是否为 true，为 true 时抛指定异常提示信息
     *
     * @param expr 表达式
     * @param message 异常提示信息
     */
    fun check(expr: Boolean, message: String) {
        if (expr) {
            throwEx(message)
        }
    }

    /**
     * 检查对象是否为 null，为null时抛出异常，否则返回对象。
     *
     * @param obj 对象
     */
    fun <T> requireNotNull(obj: T?): T {
        if (ObjectUtil.isNull(obj)) {
            throwEx()
        }
        return obj!!
    }

    /**
     * 检查对象是否为 null，为null时抛出指定异常提示信息，否则返回对象。
     *
     * @param obj 对象
     * @param message 异常提示信息
     */
    fun <T> requireNotNull(obj: T?, message: String): T {
        if (ObjectUtil.isNull(obj)) {
            throwEx(message)
        }
        return obj!!
    }

    companion object {

        /**
         * 检查，表达式为 [true] 时，对外排除指定类型的异常信息
         *
         * @param clazz 异常类型 class
         * @param expr 检查的表达式
         * @param message 排除异常定义的信息
         */
        @JvmStatic
        @JvmOverloads
        fun <E : MibrixException> check(expr: Boolean, message: String? = null, clazz: Class<E>? = null) {
            if (expr) {
                clazz ?: throw MibrixException(message)
                throw when (message) {
                    null -> clazz.getConstructor().newInstance()
                    else -> clazz.getConstructor(String::class.java).newInstance(message)
                }
            }
        }

        /**
         * 检查对象是否为 null，为null时抛出异常，否则返回对象。
         *
         * @param clazz 异常类型 class
         * @param obj 对象
         * @param message 异常提示信息
         */
        @JvmStatic
        @JvmOverloads
        fun <E : MibrixException, T> requireNotNull(obj: T?, message: String? = null, clazz: Class<out E>? = null): T {
            check(ObjectUtil.isNull(obj), message, clazz)
            return obj!!
        }
    }
}
