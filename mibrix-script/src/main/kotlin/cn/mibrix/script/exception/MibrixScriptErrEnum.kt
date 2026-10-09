/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.script.exception

import cn.mibrix.core.exception.CommonErrChecker


/**
 * 脚本沙箱错误码码枚举
 *
 * @author shareloke
 * @date 2026-10-08
 */
enum class MibrixScriptErrEnum(override val code: Int, override val message: String) : CommonErrChecker {

    SCRIPT_COMPILE_FAILED(43001, "脚本编译失败"),
    SCRIPT_FORBIDDEN(43002, "脚本包含被禁止的访问"),
    SCRIPT_TIMEOUT(43003, "脚本执行超时"),
    SCRIPT_EXECUTION_FAILED(43004, "脚本执行失败"),
    SCRIPT_EMPTY(43005, "脚本内容不能为空"),
    SCRIPT_TOO_LONG(43006, "脚本长度超过限制"),

    ;

    override fun throwEx() = throw MibrixScriptException(this)
    override fun throwEx(message: String) = throw MibrixScriptException(this, message)
}
