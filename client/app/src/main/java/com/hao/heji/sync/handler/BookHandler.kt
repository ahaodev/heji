package com.hao.heji.sync.handler

import com.hao.heji.App
import com.hao.heji.data.Status
import com.hao.heji.data.db.Book
import com.hao.heji.json
import com.hao.heji.sync.MqttSyncClient
import com.hao.heji.sync.SyncMessage
import com.hao.heji.sync.convertToAck
import com.hao.heji.sync.toJson

class AddBookHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.ADD_BOOK || message.type == SyncMessage.Type.ADD_BOOK_ACK
    }

    override fun handleMessage(message: SyncMessage) {
        if (message.type == SyncMessage.Type.ADD_BOOK) {
            val book = json.decodeFromString(Book.serializer(), message.content)
            book?.let {
                book.synced = Status.SYNCED
                App.dataBase.bookDao().upsert(book)
                val ack = message.convertToAck(SyncMessage.Type.ADD_BOOK_ACK, book.id)
                MqttSyncClient.getInstance().send(ack)
            }
        }
        if (message.type == SyncMessage.Type.ADD_BOOK_ACK) {
            App.dataBase.bookDao().updateSyncStatus(message.content)
        }
    }
}

class DeleteBookHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.DELETE_BOOK || message.type == SyncMessage.Type.DELETE_BOOK_ACK
    }

    override fun handleMessage(message: SyncMessage) {
        if (message.type == SyncMessage.Type.DELETE_BOOK) {
            val bookId = message.content
            App.dataBase.bookDao().deleteById(bookId)
            val ack = message.convertToAck(SyncMessage.Type.DELETE_BOOK_ACK, bookId)
            MqttSyncClient.getInstance().send(ack)
        }
        if (message.type == SyncMessage.Type.DELETE_BOOK_ACK) {
            App.dataBase.bookDao().deleteById(message.content)
        }
    }
}

class UpdateBookHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.UPDATE_BOOK || message.type == SyncMessage.Type.UPDATE_BOOK_ACK
    }

    override fun handleMessage(message: SyncMessage) {
        if (message.type == SyncMessage.Type.UPDATE_BOOK) {
            val book = json.decodeFromString(Book.serializer(), message.content)
            book?.let {
                App.dataBase.bookDao().upsert(book)
                val ack = message.convertToAck(SyncMessage.Type.UPDATE_BOOK_ACK, book.id)
                MqttSyncClient.getInstance().send(ack)
            }
        }
        if (message.type == SyncMessage.Type.UPDATE_BOOK_ACK) {
            App.dataBase.bookDao().updateSyncStatus(message.content)
        }
    }
}