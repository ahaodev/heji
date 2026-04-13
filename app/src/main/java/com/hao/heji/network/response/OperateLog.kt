package com.hao.heji.network.response

import kotlinx.serialization.Serializable

@Serializable
data class OperateLog(val bookId: String,
                      val opeID: String,
                      val opeType: Int,
                      val opeClass: Int) {
    companion object {
        const val DELETE = 0
        const val UPDATE = 1

        const val BOOK = 0
        const val BILL = 1
        const val CATEGORY = 2
    }
}