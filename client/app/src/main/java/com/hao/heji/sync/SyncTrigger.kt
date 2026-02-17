package com.hao.heji.sync

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Book
import com.hao.heji.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 同步触发器
 * 1. 观察本地数据库数据（根据同步状态）
 * 2. 通过节点服务同步
 */
class SyncTrigger(private val scope: CoroutineScope) {
    private val billDao = App.dataBase.billDao()
    private val bookDao = App.dataBase.bookDao()
    private val bookUserDao = App.dataBase.bookUserDao()

    private val bookJob = scope.launch(Dispatchers.IO) {
        delay(1000)
        LogUtils.d("观察账本")
        var isProcessing = false
        bookDao.flowNotSynced(Config.user.id,Config.book.id).collect {
            if (isProcessing) {
                LogUtils.d("Books 正在处理中...")
                return@collect
            }
            if (it.size <= 0) {
                LogUtils.d("Books 空的数据触发...")
                return@collect
            }
            for (b in it) {
                val bookUsers = bookUserDao.findUsersId(b.id)
                LogUtils.d(b,bookUsers)
//                when (b.syncStatus) {
//                    NEW -> {
//                        val bookJson = json.encodeToString(Book.serializer(),(b)
//                        LogUtils.d()
//                        WebSocketClient.getInstance().send(
//                            createSyncMessage(
//                                SyncMessage.Type.ADD_BOOK, bookJson, toUsers = bookUsers
//                            )
//                        )
//                    }
//
//                    DELETED -> {
//                        WebSocketClient.getInstance().send(
//                            createSyncMessage(
//                                SyncMessage.Type.DELETE_BOOK, b.id, toUsers = bookUsers
//                            )
//                        )
//                    }
//
//                    UPDATED -> {
//                        val bookJson = json.encodeToString(Book.serializer(),(b)
//                        WebSocketClient.getInstance().send(
//                            createSyncMessage(
//                                SyncMessage.Type.UPDATE_BOOK, bookJson, toUsers = bookUsers
//                            )
//                        )
//                    }
//                }
            }
        }
    }
    private val billJob = scope.launch(Dispatchers.IO) {
        delay(1000)
        LogUtils.d("观察账单")
        var isProcessing = false
        billDao.flowNotSynced(Config.book.id).collect {
            if (isProcessing) {
                LogUtils.d("正在处理中...")
                LogUtils.d("本次触发不生效...")
                return@collect
            }
            if (it.size <= 0) {
                LogUtils.d("空的数据触发...")
                return@collect
            }

            isProcessing = true
            LogUtils.d("开始处理账单 count=${it.size}...")
            for (bill in it) {
                LogUtils.d(it)
//                when (bill.syncStatus) {
//                    NEW -> {
//                        LogUtils.d("同步...")
//                        val json = json.encodeToString(Bill.serializer(),(bill)
//                        val users = bookUserDao.findUsersId(bill.bookId)
//                        WebSocketClient.getInstance().send(
//                            createSyncMessage(
//                                SyncMessage.Type.ADD_BILL, content = json, toUsers = users
//                            )
//                        )
//                    }
//
//                    DELETED -> {
//                        LogUtils.d("删除...")
//                        val users = bookUserDao.findUsersId(bill.id)
//                        WebSocketClient.getInstance().send(
//                            createSyncMessage(
//                                SyncMessage.Type.DELETE_BILL, content = bill.id, toUsers = users
//                            )
//                        )
//                    }
//
//                    UPDATED -> {
//                        LogUtils.d("更新...")
//                        val json = json.encodeToString(Bill.serializer(),(bill)
//                        val users = bookUserDao.findUsersId(bill.bookId)
//                        WebSocketClient.getInstance().send(
//                            createSyncMessage(
//                                SyncMessage.Type.UPDATE_BILL, content = json, toUsers = users
//                            )
//                        )
//                    }
//                }
            }
            LogUtils.d("本次变更账单处理完成...")
            isProcessing = false
        }
    }

    fun register() {
        bookJob.start()
        billJob.start()
    }

    fun unregister() {
        bookJob.cancel()
        billJob.cancel()
    }
}