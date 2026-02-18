package com.hao.heji.network

import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Book
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncChangesResponse(
    @SerialName("books") val books: List<Book> = emptyList(),
    @SerialName("bills") val bills: List<Bill> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_since") val nextSince: Long = 0,
)
