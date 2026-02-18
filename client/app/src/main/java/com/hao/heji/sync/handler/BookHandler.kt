package com.hao.heji.sync.handler

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.data.Status
import com.hao.heji.data.db.Book
import com.hao.heji.json
import com.hao.heji.sync.SyncMessage

class AddBookHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.ADD_BOOK
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("处理通知 type=${message.type}")
        val book = json.decodeFromString(Book.serializer(), message.content)
        book.synced = Status.SYNCED
        App.dataBase.bookDao().upsert(book)
    }
}

class DeleteBookHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.DELETE_BOOK
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("处理通知 type=${message.type}")
        App.dataBase.bookDao().deleteById(message.content)
    }
}

class UpdateBookHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.UPDATE_BOOK
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("处理通知 type=${message.type}")
        val book = json.decodeFromString(Book.serializer(), message.content)
        book.synced = Status.SYNCED
        App.dataBase.bookDao().upsert(book)
    }
}