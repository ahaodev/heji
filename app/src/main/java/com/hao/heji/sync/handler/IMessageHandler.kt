package com.hao.heji.sync.handler

import com.hao.heji.sync.SyncMessage

interface IMessageHandler {
    fun canHandle(message: SyncMessage): Boolean
    fun handleMessage(message: SyncMessage)
}
