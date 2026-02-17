package com.hao.heji.sync

import com.hao.heji.json
import com.github.shamil.Xid

fun createSyncMessage(
    type: String,
    content: String,
    id: String = Xid.string(),
    toUsers: List<String> = emptyList()
): SyncMessage {
    return SyncMessage(
        id = id,
        type = type,
        senderId = com.hao.heji.config.Config.user.id,
        content = content,
        receiverIds = toUsers,
    )
}

fun SyncMessage.toJson(): String {
    return json.encodeToString(SyncMessage.serializer(), this)
}

fun SyncMessage.convertToAck(
    type: String,
    content: String,
): SyncMessage {
    return createSyncMessage(type, content, id, listOf(senderId))
}