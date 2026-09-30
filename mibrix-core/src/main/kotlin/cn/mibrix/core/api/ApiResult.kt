/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.core.api

import cn.mibrix.core.baseFailCode
import cn.mibrix.core.baseFailMessage
import cn.mibrix.core.baseOkCode
import cn.mibrix.core.baseOkMessage
import cn.mibrix.core.constant.CommonAssert
import cn.mibrix.core.constant.CommonErrCode
import io.swagger.v3.oas.annotations.media.Schema

/**
 * api 接口统一返回体
 *
 * 约定：
 * - 所有 Controller（含统一 CRUD 网关、控制台 API、流程调用 API）均返回 ApiResult 包装结构；
 * - HTTP 状态码表达传输层语义（由全局异常处理器按 ErrorCode.httpStatus 设置），
 *   业务结果以 [code] 为准，[CommonErrCode.SUCCESS] 即 "0" 表示成功；
 * - [traceId] 预留链路追踪位，由后续审计/日志能力填充。
 *
 * @author shareloke
 * @date 2026-09-30
 */
@Schema(description = "api 接口统一返回体")
data class ApiResult<R>(

    @field:Schema(description = "业务状态码")
    val code: String,

    @field:Schema(description = "业务状态描述")
    val message: String,

    @field:Schema(description = "预留路追踪位")
    val traceId: String? = null,

    @field:Schema(description = "业务数据")
    val data: R? = null,
) where R : Any? {

    /** 是否成功（以 code 判断，而非 HTTP 状态） */
    @get:Schema(description = "是否成功")
    val success: Boolean
        get() = code == baseOkCode

    companion object {

        /**
         * 成功返回
         *
         * @param R 业务数据类型
         * @param data 业务数据
         * @return the api result
         */
        @JvmStatic
        @JvmOverloads
        fun <R> ok(data: R? = null): ApiResult<R> = ApiResult(baseOkCode, baseOkMessage, data = data)

        /**
         * 失败返回
         *
         * @param R 业务数据类型
         * @param err 错误码/异常
         * @param message 错误描述/异常信息，若为空则使用 [err.message] 值
         * @return the api result
         */
        @JvmStatic
        @JvmOverloads
        fun <R> fail(err: CommonAssert, message: String? = null): ApiResult<R> =
            ApiResult(err.code, message ?: err.message)

        /**
         * 失败返回
         *
         * @param R 业务数据类型
         * @param code 业务状态码
         * @param message 业务状态描述
         * @return the api result
         */
        @JvmStatic
        fun <R> fail(code: String, message: String): ApiResult<R> = ApiResult(code, message)

        /**
         * 失败返回
         *
         * @param R 业务数据类型
         * @return the api result
         */
        @JvmStatic
        fun <R> fail(): ApiResult<R> = ApiResult(baseFailCode, baseFailMessage)
    }
}
