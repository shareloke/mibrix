/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.exception

/**
 * 框架通用错误码。模块级错误码请实现 [CommonErrChecker] 自行扩展
 *
 * @author shareloke
 * @date 2026-09-30
 */
enum class CommonErrEnum(override val code: Int, override val message: String) : CommonErrChecker {

    SUCCESS(200, "成功"),
    FAIL(400, "失败"),
    ERROR(500, "系统异常"),

    PARAM_INVALID(400000, "参数校验失败"),
    UNAUTHORIZED(401000, "未认证或登录已过期"),
    FORBIDDEN(403000, "无访问权限"),
    NOT_FOUND(404000, "资源不存在"),
    METHOD_NOT_ALLOWED(405000, "请求方法不支持"),
    CONFLICT(409000, "数据冲突或版本不一致"),
    SYSTEM_ERROR(500000, "系统内部错误"),
    DOWNSTREAM_ERROR(502000, "下游服务调用失败"),
}
