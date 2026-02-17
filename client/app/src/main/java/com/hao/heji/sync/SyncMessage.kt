package com.hao.heji.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncMessage(
    @SerialName("type") val type: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("content") val content: String,
    @SerialName("id") val id: String = "",
    @SerialName("receiver_ids") val receiverIds: List<String> = emptyList(),
) {
    object Type {
        const val ADD_BILL = "ADD_BILL"
        const val DELETE_BILL = "DELETE_BILL"
        const val UPDATE_BILL = "UPDATE_BILL"
        const val ADD_BILL_ACK = "ADD_BILL_ACK"
        const val DELETE_BILL_ACK = "DELETE_BILL_ACK"
        const val UPDATE_BILL_ACK = "UPDATE_BILL_ACK"

        const val ADD_BOOK = "ADD_BOOK"
        const val DELETE_BOOK = "DELETE_BOOK"
        const val UPDATE_BOOK = "UPDATE_BOOK"
        const val ADD_BOOK_ACK = "ADD_BOOK_ACK"
        const val DELETE_BOOK_ACK = "DELETE_BOOK_ACK"
        const val UPDATE_BOOK_ACK = "UPDATE_BOOK_ACK"
    }
}
