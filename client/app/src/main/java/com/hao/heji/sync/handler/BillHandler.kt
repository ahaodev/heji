package com.hao.heji.sync.handler

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.data.db.Bill
import com.hao.heji.json
import com.hao.heji.sync.MqttSyncClient
import com.hao.heji.sync.SyncMessage
import com.hao.heji.sync.convertToAck
import com.hao.heji.sync.toJson

class AddBillHandler : IMessageHandler {

    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.ADD_BILL || message.type == SyncMessage.Type.ADD_BILL_ACK
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d("开始处理消息 type=${message.type}")
        val billDao = App.dataBase.billDao()
        if (message.type == SyncMessage.Type.ADD_BILL_ACK) {
            billDao.updateSyncStatus(billId = message.content, 1)
            return
        }
        val bill = json.decodeFromString(Bill.serializer(), message.content)
        bill?.let {
            billDao.insert(bill)
            val ack = message.convertToAck(SyncMessage.Type.ADD_BILL_ACK, bill.id)
            MqttSyncClient.getInstance().send(ack)
        }
    }
}

class DeleteBillHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.DELETE_BILL || message.type == SyncMessage.Type.DELETE_BILL_ACK
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d(message)
        val billDao = App.dataBase.billDao()
        if (message.type == SyncMessage.Type.DELETE_BILL_ACK) {
            billDao.deleteById(message.content)
            return
        }
        billDao.deleteById(message.content)
        val ack = message.convertToAck(SyncMessage.Type.DELETE_BILL_ACK, message.content)
        MqttSyncClient.getInstance().send(ack)
    }
}

class UpdateBillHandler : IMessageHandler {
    override fun canHandle(message: SyncMessage): Boolean {
        return message.type == SyncMessage.Type.UPDATE_BILL || message.type == SyncMessage.Type.UPDATE_BILL_ACK
    }

    override fun handleMessage(message: SyncMessage) {
        LogUtils.d(message)
        val billDao = App.dataBase.billDao()

        if (message.type == SyncMessage.Type.UPDATE_BILL_ACK) {
            billDao.updateSyncStatus(billId = message.content, 1)
            return
        }
        val bill = json.decodeFromString(Bill.serializer(), message.content)
        bill?.let {
            billDao.update(bill)
            val ack = message.convertToAck(SyncMessage.Type.UPDATE_BILL_ACK, bill.id)
            MqttSyncClient.getInstance().send(ack)
        }
    }
}