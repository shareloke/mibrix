/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.sandbox

import cn.mibrix.core.annotation.KSlf4j
import cn.mibrix.core.annotation.KSlf4j.Companion.log
import cn.mibrix.script.constant.MibrixScriptConsts
import cn.mibrix.script.exception.MibrixScriptErrEnum
import cn.mibrix.script.exception.MibrixScriptException
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import groovy.lang.Binding
import groovy.lang.GroovyClassLoader
import groovy.lang.Script
import groovy.transform.ThreadInterrupt
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.runtime.InvokerHelper
import org.kohsuke.groovy.sandbox.SandboxTransformer
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils

/**
 * 自定义脚本执行安全沙箱
 *
 * @author shareloke
 * @date 2026-10-08
 */
@KSlf4j
@Component
class MibrixScriptSandbox : DisposableBean {

    companion object {
        const val DEFAULT_TIMEOUT_MS = 3_000L
        const val MAX_SCRIPT_LENGTH = 5 * 1024 * 1024 // 100KB

        /** 编译产物缓存：脚本文本 -> Script Class（上限 1000，防内存膨胀） */
        val SCRIPT_CACHE: Cache<String, Class<out Script>> =
            Caffeine.newBuilder()
                .maximumSize(1_000)
                // 防止超长脚本撑爆内存，限制单个 key 的最大长度
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build()
    }

    // 编译配置
    private val compilerConfiguration by lazy { buildCompilerConfiguration() }

    /** 共享 ClassLoader：编译产物缓存复用，沙箱配置在编译器层面统一约束 */
    private val groovyClassLoader = GroovyClassLoader(this::class.java.classLoader, compilerConfiguration)

    // 脚本执行线程池（守护线程，泄漏的脚本线程不阻塞 JVM 退出）
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "mixel-script-worker").apply { isDaemon = true }
    }

    // ✅ 全局单例拦截器（线程安全）
    private val interceptor = MibrixSandboxInterceptor()

    /**
     * 在沙箱中执行脚本。
     *
     * @param scriptText 脚本文本
     * @param bindings 注入变量（流程引擎注入 request / variables / context / log）
     * @param timeoutMs 超时毫秒，超时后中断脚本线程并抛 [MibrixScriptErrEnum.SCRIPT_TIMEOUT]
     * @return 脚本最后一行的返回值
     */
    fun execute(
        scriptText: String,
        bindings: Map<String, Any?> = emptyMap(),
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    ): Any? {
        validateScript(scriptText)
        val scriptClass = compile(scriptText)

        // 提交脚本执行任务到线程池
        val future = runSandboxedScript(scriptClass, bindings)

        return try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (ex: Exception) {
            throw when (val cause = ex.cause) {
                is MibrixScriptException -> cause

                // 业务异常直接透传，不重复包装
                is InterruptedException -> {
                    Thread.currentThread().interrupt()
                    MibrixScriptException(MibrixScriptErrEnum.SCRIPT_TIMEOUT, "脚本被中断", ex)
                }

                is TimeoutException -> {
                    // 中断脚本线程，配合 ThreadInterrupt 检查点生效
                    future.cancel(true)
                    MibrixScriptException(MibrixScriptErrEnum.SCRIPT_TIMEOUT, "脚本执行超过 ${timeoutMs}ms 被强制中断")
                }

                else -> MibrixScriptException(
                    MibrixScriptErrEnum.SCRIPT_EXECUTION_FAILED,
                    "脚本执行失败: ${cause?.message ?: ex.message}",
                    ex,
                )
            }
        }
    }

    /**
     * 构建编译配置
     * 配置安全定制：关闭闭包/方法定义/包导入，仅允许导入白名单类
     * 配置 AST 检查：禁止在脚本中使用危险类
     *
     * @return 编译配置
     */
    private fun buildCompilerConfiguration(): CompilerConfiguration {
        // 预导入安全类：脚本零 import 也能用 BigDecimal/日期时间工具
        val importCustomizer = ImportCustomizer().apply {
            addImports(*MibrixScriptConsts.ALLOWED_IMPORTS)
        }

        // 安全定制：关闭闭包/方法定义/包导入，仅允许导入白名单类
        val secureASTCustomizer = SecureASTCustomizer().apply {
            // ---- 安全定制 ----
            isClosuresAllowed = true
            isMethodDefinitionAllowed = false
            isPackageAllowed = false
            // 间接导入检查会误拦 Groovy 运行期内部类（org.codehaus.groovy.runtime.GStringImpl
            // 等），且与闭包/GString 不兼容，故关闭；逃逸防护由四层自定义 AST 检查 +
            // 接收者黑名单 + 导入白名单承担（见 SandboxForbidden/SandboxAstCustomizer）
            isIndirectImportCheckEnabled = false

            // 禁止在脚本中使用危险类
            disallowedReceivers = MibrixScriptConsts.FORBIDDEN_CLASSES
        }

        return CompilerConfiguration().apply {
            addCompilationCustomizers(
                importCustomizer,
                secureASTCustomizer,
                SandboxTransformer(),
                // 循环/方法入口注入中断检查点，是超时强制中断能够生效的前提
                ASTTransformationCustomizer(ThreadInterrupt::class.java),
            )
        }
    }

    /** 编译（带缓存）；沙箱违规归类 SCRIPT_FORBIDDEN，语法错误归类 SCRIPT_COMPILE_FAILED */
    private fun compile(scriptText: String) = try {
        val normalized = normalizeScript(scriptText)
        // ✅ MD5 仅 32 字节，作为 Key 极其轻量
        val cacheKey = DigestUtils.md5DigestAsHex(normalized.toByteArray())
        SCRIPT_CACHE.get(cacheKey) {
            // ✅ 关键：用闭包捕获的 normalized 原文编译，而非 Caffeine 传入的 key
            @Suppress("UNCHECKED_CAST")
            groovyClassLoader.parseClass(normalized) as Class<out Script>
        } ?: throw MibrixScriptException(MibrixScriptErrEnum.SCRIPT_COMPILE_FAILED, "脚本编译产物为空")
    } catch (ex: MultipleCompilationErrorsException) {
        val raw = ex.message ?: ""
        // 沙箱自定义检查器的违规信息（SANDBOX: 前缀）
        val forbidden = Regex("SANDBOX: (.+?)(?: @ line|\\n|$)").find(raw)?.groupValues?.get(1)?.trim()
        if (forbidden != null) {
            log.warn("[sandbox] 拦截危险脚本: {}", forbidden)
            throw MibrixScriptException(MibrixScriptErrEnum.SCRIPT_FORBIDDEN, "禁止访问 $forbidden")
        }
        // SecureASTCustomizer 的导入白名单拦截也归类为禁止访问
        val security = Regex("java\\.lang\\.SecurityException: (.+?)(?:\\n|$)").find(raw)?.groupValues?.get(1)?.trim()
        if (security != null) {
            log.warn("[sandbox] 拦截危险脚本: {}", security)
            throw MibrixScriptException(MibrixScriptErrEnum.SCRIPT_FORBIDDEN, security)
        }
        // 普通语法错误：提取「文件: 行: 详情」中的详情部分
        val detail = raw.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.matches(Regex(".*:\\s*\\d+: .+")) }
            ?.replace(Regex("^.*:\\s*\\d+: "), "")
            ?: raw.lineSequence().firstOrNull { it.isNotBlank() && it.trim() != "startup failed:" }?.trim()
            ?: "未知编译错误"
        throw MibrixScriptException(MibrixScriptErrEnum.SCRIPT_COMPILE_FAILED, "脚本编译失败: ${detail.take(300)}")
    }

    // ✅ 修复：对脚本做归一化 + 哈希作为缓存 key
    private fun normalizeScript(text: String): String {
        // 去除注释、多余空白、BOM 等不影响语义的内容
        return text.trim()
            .replace(Regex("//.*"), "")
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * 提取脚本前置校验逻辑
     * 校验脚本内容是否为空、是否超过最大长度
     *
     * @param scriptText 脚本内容
     */
    private fun validateScript(scriptText: String) {
        if (scriptText.isBlank() || scriptText.isEmpty()) {
            throw MibrixScriptException(MibrixScriptErrEnum.SCRIPT_EMPTY)
        }
        if (scriptText.length > MAX_SCRIPT_LENGTH) {
            throw MibrixScriptException(MibrixScriptErrEnum.SCRIPT_TOO_LONG)
        }
    }

    /**
     * 提取沙箱执行核心逻辑（内部可能有自己的异常，但不在这里抛业务异常）
     */
    private fun runSandboxedScript(scriptClass: Class<out Script>, bindings: Map<String, Any?>) =
        executor.submit<Any?> {
            // 提交脚本执行任务到线程池
            val script = InvokerHelper.createScript(scriptClass, Binding(bindings))
            // ✅ 使用 Checker 静态方法注册/注销拦截器
            interceptor.register()
            try {
                script.run()
            } finally {
                // ⚠️ 必须在 finally 中注销，防止 ThreadLocal 泄漏
                interceptor.unregister()
            }
        }

    /** 销毁沙箱，释放所有资源 */
    override fun destroy() {
        executor.shutdownNow()
        groovyClassLoader.close() // ← 关键：释放所有已加载的 Class
        SCRIPT_CACHE.invalidateAll()
        log.info("[sandbox] MibrixScriptSandbox destroyed")
    }
}
