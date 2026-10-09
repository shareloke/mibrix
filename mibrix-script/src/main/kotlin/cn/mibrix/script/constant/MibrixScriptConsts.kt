/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.constant

/**
 * 脚本常量定义
 *
 * @author shareloke
 * @date 2026-10-08
 */
object MibrixScriptConsts {

    /** 危险包前缀（全限定名匹配） */
    val FORBIDDEN_IMPORT_PACKAGES: List<String> = listOf(
        "java.io.",
        "java.net.",
        "java.nio.",
        "javax.script.",
        "java.lang.reflect.",
        "jdk.",
        "sun.",
        "com.sun.",
        "java.util.concurrent.",
        "java.lang.management.",
    )

    /** 危险属性（.class / .metaClass 是逃逸的两大入口） */
    val FORBIDDEN_PROPERTIES: Set<String> = setOf("class", "metaClass")

    /** 允许导入的类（白名单）：仅数据/集合/时间/数学等无副作用类型 */
    val ALLOWED_IMPORTS = arrayOf(
        "java.lang.String",
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.Double",
        "java.lang.Boolean",
        "java.lang.Math",
        "java.math.BigDecimal",
        "java.math.BigInteger",
        "java.util.List",
        "java.util.Map",
        "java.util.Set",
        "java.util.ArrayList",
        "java.util.HashMap",
        "java.util.LinkedHashMap",
        "java.util.Arrays",
        "java.util.Collections",
        "java.time.LocalDate",
        "java.time.LocalDateTime",
        "java.time.format.DateTimeFormatter",
        "groovy.json.JsonSlurper",
        "groovy.json.JsonOutput",
    )

    /** 禁止的方法名（黑名单，如反射与进程执行入口） */
    val FORBIDDEN_METHODS: Set<String> = setOf(
        "exec", "exit", "halt", "getRuntime", "forName", "newInstance",
        "loadClass", "defineClass", "getClassLoader", "setAccessible",
        "invoke", "getDeclaredMethod", "getDeclaredField", "start", "stop",
        "destroy", "wait", "notify", "notifyAll",
    )

    /** 禁止的类名（黑名单，双保险：即使白名单被绕过也拦截） */
    val FORBIDDEN_CLASSES = listOf(
        "java.lang.Runtime",
        "java.lang.ProcessBuilder",
        "java.lang.Process",
        "java.lang.Thread",
        "java.lang.ThreadGroup",
        "java.io.File",
        "java.nio.file.Files",
        "java.nio.file.Paths",
        "java.nio.file.Path",
        "java.io.FileInputStream",
        "java.io.FileOutputStream",
        "java.io.FileReader",
        "java.io.FileWriter",
        "java.io.RandomAccessFile",
        "java.net.Socket",
        "java.net.ServerSocket",
        "java.net.DatagramSocket",
        "java.net.URL",
        "java.net.URI",
        "java.lang.Class",
        "java.lang.ClassLoader",
        "java.lang.reflect.Method",
        "java.lang.reflect.Field",
        "java.lang.reflect.Module",
        "java.lang.reflect.Constructor",
        "sun.misc.Unsafe",
        "java.net.HttpURLConnection",
        "java.net.URLConnection",
        "javax.script.ScriptEngineManager",
        "javax.script.ScriptEngine",
        "groovy.lang.GroovyShell",
        "groovy.lang.GroovyClassLoader",
        "groovy.util.GroovyScriptEngine",
        "groovy.util.Eval",
        "javax.script.Compilable",
        "javax.script.Invocable",
    )
}
