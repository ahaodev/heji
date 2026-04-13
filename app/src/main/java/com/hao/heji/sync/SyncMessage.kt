package com.hao.heji.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.github.shamil.Xid

@Serializable
data class SyncMessage(
    @SerialName("id") val id: String = Xid.string(),
    @SerialName("type") val type: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("content") val content: String,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis(),
) {
    object Type {
        const val ADD_BILL = "ADD_BILL"
        const val DELETE_BILL = "DELETE_BILL"
        const val UPDATE_BILL = "UPDATE_BILL"

        const val ADD_BOOK = "ADD_BOOK"
        const val DELETE_BOOK = "DELETE_BOOK"
        const val UPDATE_BOOK = "UPDATE_BOOK"

        const val IMAGE_UPLOADED = "IMAGE_UPLOADED"
        const val IMAGE_DELETED = "IMAGE_DELETED"
    }
}
