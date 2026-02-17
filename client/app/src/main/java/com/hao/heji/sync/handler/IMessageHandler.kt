package com.hao.heji.sync.handler

import com.hao.heji.sync.SyncMessage
import okhttp3.WebSocket

interface IMessageHandler {
    fun canHandle(message: SyncMessage): Boolean
    fun handleMessage(webSocket: WebSocket, message: SyncMessage)
}
