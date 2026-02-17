package com.hao.heji.sync

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.json
import com.hao.heji.sync.handler.AddBillHandler
import com.hao.heji.sync.handler.AddBookHandler
import com.hao.heji.sync.handler.DeleteBillHandler
import com.hao.heji.sync.handler.DeleteBookHandler
import com.hao.heji.sync.handler.IMessageHandler
import com.hao.heji.sync.handler.UpdateBillHandler
import com.hao.heji.sync.handler.UpdateBookHandler
import okhttp3.WebSocket

class SyncReceiver {
    private val handlers = mutableListOf<IMessageHandler>()

    fun register() {

        register(AddBookHandler())
        register(DeleteBookHandler())
        register(UpdateBookHandler())

        register(AddBillHandler())
        register(DeleteBillHandler())
        register(UpdateBillHandler())

    }

    fun register(handler: IMessageHandler) {
        if (!handlers.contains(handler))
            handlers.add(handler)
    }

    fun unregister(handler: IMessageHandler) {
        if (handlers.contains(handler))
            handlers.remove(handler)
    }

    fun unregister() {
        handlers.clear()
    }

    fun onReceiver(webSocket: WebSocket, text: String) {
        val message = json.decodeFromString(SyncMessage.serializer(), text)
        for (i in handlers) {
            if (i.canHandle(message)) {
                LogUtils.d("handle by ${i.javaClass.simpleName}", message)
                i.handleMessage(webSocket, message)
            }
        }
    }

}