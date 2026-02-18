package com.hao.heji.network

import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Book
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncChangesResponse(
    @SerialName("books") val books: List<SyncBook> = emptyList(),
    @SerialName("bills") val bills: List<SyncBill> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_since") val nextSince: Long = 0,
)

/**
 * 同步拉取时的账本 DTO，包含 deleted_at 用于判断是否已删除。
 * 服务端对已删除记录会返回 deleted_at 字段（soft delete）。
 */
@Serializable
data class SyncBook(
    @SerialName("_id") val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String? = null,
    @SerialName("crt_user_id") val crtUserId: String = "",
    @SerialName("banner") val bannerUrl: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
) {
    fun toBook(): Book = Book(
        id = id,
        name = name,
        type = type,
        crtUserId = crtUserId,
        bannerUrl = bannerUrl,
    )
}

/**
 * 同步拉取时的账单 DTO，包含 deleted_at 用于判断是否已删除。
 */
@Serializable
data class SyncBill(
    @SerialName("_id") val id: String,
    @SerialName("book_id") val bookId: String = "",
    @SerialName("money") val money: Double = 0.0,
    @SerialName("type") val type: Int = -1,
    @SerialName("category") val category: String? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("crt_user") val crtUser: String = "",
    @SerialName("remark") val remark: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
) {
    fun toBill(): Bill = Bill(
        id = id,
        bookId = bookId,
        money = java.math.BigDecimal(money).divide(java.math.BigDecimal(100), 2, java.math.RoundingMode.DOWN),
        type = type,
        category = category,
        crtUser = crtUser,
        remark = remark,
    ).also {
        if (time != null) {
            try {
                it.time = com.blankj.utilcode.util.TimeUtils.string2Date(
                    time, com.hao.heji.data.converters.DateConverters.DB_PATTERN
                )
            } catch (_: Exception) { }
        }
    }
}
