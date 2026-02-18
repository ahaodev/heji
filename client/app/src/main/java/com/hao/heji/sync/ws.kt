package com.hao.heji.sync

import com.hao.heji.json
import com.github.shamil.Xid

fun createSyncMessage(
    type: String,
    bookId: String,
    content: String,
    id: String = Xid.string(),
): SyncMessage {
    return SyncMessage(
        id = id,
        type = type,
        bookId = bookId,
        content = content,
    )
}

fun SyncMessage.toJson(): String {
    return json.encodeToString(SyncMessage.serializer(), this)
}