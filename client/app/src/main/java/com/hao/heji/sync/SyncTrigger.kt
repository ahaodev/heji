package com.hao.heji.sync

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.Status
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
 * 2. 通过MQTT同步
 *
 * synced + deleted 组合判断：
 * - synced=0, deleted=0 → ADD（新建未同步）
 * - synced=2, deleted=0 → UPDATE（修改未同步）
 * - synced=0, deleted=1 → DELETE（删除未同步）
 */
class SyncTrigger(private val scope: CoroutineScope) {
    private val billDao = App.dataBase.billDao()
    private val bookDao = App.dataBase.bookDao()
    private val bookUserDao = App.dataBase.bookUserDao()

    private val bookJob = scope.launch(Dispatchers.IO) {
        delay(1000)
        LogUtils.d("观察账本")
        var isProcessing = false
        bookDao.flowNotSynced(Config.user.id, Config.book.id).collect {
            if (isProcessing) {
                LogUtils.d("Books 正在处理中...")
                return@collect
            }
            if (it.isEmpty()) {
                return@collect
            }
            isProcessing = true
            for (b in it) {
                val bookUsers = bookUserDao.findUsersId(b.id)
                LogUtils.d("同步账本: ${b.id}, synced=${b.synced}, deleted=${b.deleted}")
                try {
                    val sent = when {
                        b.deleted == Status.DELETED -> {
                            MqttSyncClient.getInstance().send(
                                createSyncMessage(
                                    SyncMessage.Type.DELETE_BOOK, b.id, toUsers = bookUsers
                                )
                            )
                        }
                        b.synced == Status.UPDATED -> {
                            val bookJson = json.encodeToString(Book.serializer(), b)
                            MqttSyncClient.getInstance().send(
                                createSyncMessage(
                                    SyncMessage.Type.UPDATE_BOOK, bookJson, toUsers = bookUsers
                                )
                            )
                        }
                        b.synced == Status.NOT_SYNCED -> {
                            val bookJson = json.encodeToString(Book.serializer(), b)
                            MqttSyncClient.getInstance().send(
                                createSyncMessage(
                                    SyncMessage.Type.ADD_BOOK, bookJson, toUsers = bookUsers
                                )
                            )
                        }
                        else -> false
                    }
                    if (sent) {
                        bookDao.updateSyncStatus(b.id, Status.SYNCING)
                    }
                } catch (e: Exception) {
                    LogUtils.e("同步账本失败: ${b.id}", e)
                }
            }
            isProcessing = false
        }
    }

    private val billJob = scope.launch(Dispatchers.IO) {
        delay(1000)
        LogUtils.d("观察账单")
        var isProcessing = false
        billDao.flowNotSynced(Config.book.id).collect {
            if (isProcessing) {
                LogUtils.d("正在处理中...")
                return@collect
            }
            if (it.isEmpty()) {
                return@collect
            }

            isProcessing = true
            LogUtils.d("开始处理账单 count=${it.size}...")
            for (bill in it) {
                LogUtils.d("同步账单: ${bill.id}, synced=${bill.synced}, deleted=${bill.deleted}")
                try {
                    val users = bookUserDao.findUsersId(bill.bookId)
                    val sent = when {
                        bill.deleted == Status.DELETED -> {
                            MqttSyncClient.getInstance().send(
                                createSyncMessage(
                                    SyncMessage.Type.DELETE_BILL, content = bill.id, toUsers = users
                                )
                            )
                        }
                        bill.synced == Status.UPDATED -> {
                            val billJson = json.encodeToString(Bill.serializer(), bill)
                            MqttSyncClient.getInstance().send(
                                createSyncMessage(
                                    SyncMessage.Type.UPDATE_BILL, content = billJson, toUsers = users
                                )
                            )
                        }
                        bill.synced == Status.NOT_SYNCED -> {
                            val billJson = json.encodeToString(Bill.serializer(), bill)
                            MqttSyncClient.getInstance().send(
                                createSyncMessage(
                                    SyncMessage.Type.ADD_BILL, content = billJson, toUsers = users
                                )
                            )
                        }
                        else -> false
                    }
                    if (sent) {
                        billDao.updateSyncStatus(bill.id, Status.SYNCING)
                    }
                } catch (e: Exception) {
                    LogUtils.e("同步账单失败: ${bill.id}", e)
                }
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