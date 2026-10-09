/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.sandbox

import cn.mibrix.script.constant.MibrixScriptConsts
import cn.mibrix.script.exception.MibrixScriptException
import cn.mibrix.script.exception.MibrixScriptErrEnum
import org.kohsuke.groovy.sandbox.GroovyInterceptor

/**
 * 自定义沙箱拦截器，用于限制脚本执行的类和方法调用
 *
 * @author shareloke
 * @date 2026-10-08
 */
class MibrixSandboxInterceptor : GroovyInterceptor() {

    /**
     * 拦截实例方法调用
     * 放行：返回 super.onMethodCall(invoker, receiver, method, args)
     * 拦截：抛出异常
     */
    @Throws(Throwable::class)
    override fun onMethodCall(invoker: Invoker, receiver: Any?, method: String, args: Array<out Any?>): Any? {
        val className = when (receiver) {
            is Class<*> -> receiver.name
            else -> receiver?.javaClass?.name
        }
        if (className != null && isForbidden(className)) {
            if (className == "java.lang.System") {
                val allowedSystemMethods = setOf(
                    "println",
                    "print",
                    "printf",
                    "format", // System.out / System.err 的方法
                    "currentTimeMillis",
                    "nanoTime", // 常用的安全时间方法
                )
                // 如果调用的方法在白名单内，直接放行
                if (method in allowedSystemMethods) {
                    return super.onMethodCall(invoker, receiver, method, args)
                }
            }
            throw MibrixScriptException(
                MibrixScriptErrEnum.SCRIPT_FORBIDDEN,
                "沙箱拦截: 禁止调用 $className.$method",
            )
        }
        // ✅ 放行：调用 invoker 继续执行
        return super.onMethodCall(invoker, receiver, method, *args)
    }

    /**
     * 拦截静态方法调用 (如 System.exit(0))
     */
    @Throws(Throwable::class)
    override fun onStaticCall(invoker: Invoker, type: Class<*>, method: String, args: Array<out Any?>): Any? {
        if (isForbidden(type.name)) {
            throw MibrixScriptException(
                MibrixScriptErrEnum.SCRIPT_FORBIDDEN,
                "沙箱拦截: 禁止静态调用 ${type.name}.$method",
            )
        }
        return super.onStaticCall(invoker, type, method, *args)
    }

    /**
     * 拦截构造器实例化 (如 new File("/etc/passwd"))
     */
    @Throws(Throwable::class)
    override fun onNewInstance(invoker: Invoker, type: Class<*>, args: Array<out Any?>): Any? {
        if (isForbidden(type.name)) {
            throw MibrixScriptException(
                MibrixScriptErrEnum.SCRIPT_FORBIDDEN,
                "沙箱拦截: 禁止实例化 ${type.name}",
            )
        }
        return super.onNewInstance(invoker, type, *args)
    }

    /**
     * 拦截属性读取 (如 System.properties)
     */
    @Throws(Throwable::class)
    override fun onGetProperty(invoker: Invoker, receiver: Any?, property: String): Any? {
        val className = when (receiver) {
            is Class<*> -> receiver.name
            else -> receiver?.javaClass?.name
        }
        if (className != null && isForbidden(className)) {
            // ✅ 允许读取 System.out 和 System.err
            if (className == "java.lang.System" && property in setOf("out", "err")) {
                return super.onGetProperty(invoker, receiver, property)
            }

            throw MibrixScriptException(
                MibrixScriptErrEnum.SCRIPT_FORBIDDEN,
                "沙箱拦截: 禁止读取 $className.$property",
            )
        }
        return super.onGetProperty(invoker, receiver, property)
    }

    private fun isForbidden(className: String) = MibrixScriptConsts.FORBIDDEN_CLASSES.any {
        className == it || className.startsWith("$it.")
    }
}
