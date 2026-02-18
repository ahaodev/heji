package com.hao.heji.sync.handler

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.data.Status
import com.hao.heji.data.db.Bill
import com.hao.heji.json
import com.hao.heji.sync.SyncMessage

class AddBillHandler : IMessageHandler {

    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.ADD_BILL
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("处理通知 type=${message.type}")
        val bill = json.decodeFromString(Bill.serializer(), message.content)
        bill.synced = Status.SYNCED
        App.dataBase.billDao().upsert(bill)
    }
}

class DeleteBillHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.DELETE_BILL
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("处理通知 type=${message.type}")
        App.dataBase.billDao().deleteById(message.content)
    }
}

class UpdateBillHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.UPDATE_BILL
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("处理通知 type=${message.type}")
        val bill = json.decodeFromString(Bill.serializer(), message.content)
        bill.synced = Status.SYNCED
        App.dataBase.billDao().upsert(bill)
    }
}