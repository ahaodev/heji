package com.hao.heji.network

import kotlinx.serialization.Serializable

/**
 * @date: 2020/9/23
 *
 * @author: 锅得铁
 * #
 */
@Serializable
data class BaseResponse<T>(
    var code: Int = 0,
    var msg: String? = null,
    var data: T? = null
) {
    fun success(): Boolean {
        return code == 0
    }
}