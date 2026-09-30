/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.constant

/**
 * 框架通用错误码。模块级错误码请实现 [ErrorCode] 自行扩展，不要往这里堆。
 *
 * @author shareloke
 * @date 2026-09-30
 */
enum class CommonErrCode(override val code: String, override val message: String) : CommonAssert {

    SUCCESS("200", "成功"),
    FAIL("400", "失败"),
    ERROR("500", "系统异常"),

    PARAM_INVALID("40000", "参数校验失败"),
    UNAUTHORIZED("40100", "未认证或登录已过期"),
    FORBIDDEN("40300", "无访问权限"),
    NOT_FOUND("40400", "资源不存在"),
    METHOD_NOT_ALLOWED("40500", "请求方法不支持"),
    CONFLICT("40900", "数据冲突或版本不一致"),
    SYSTEM_ERROR("50000", "系统内部错误"),
    DOWNSTREAM_ERROR("50200", "下游服务调用失败"),
}
