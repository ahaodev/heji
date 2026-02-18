package com.hao.heji.sync

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.Status
import com.hao.heji.data.db.Book
import com.hao.heji.network.HttpManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get

/**
 * 同步触发器 (v2 — HTTP 同步)
 * 观察本地数据库中 synced != SYNCED 的记录，通过 HTTP API 上传到服务端。
 * 成功后标记 SYNCED 或硬删除本地记录。
 *
 * 同步顺序：账本优先于账单，避免 FOREIGN KEY 约束失败。
 */
class SyncTrigger(private val scope: CoroutineScope) {
    private val billDao = App.dataBase.billDao()
    private val bookDao = App.dataBase.bookDao()
    private val httpManager: HttpManager = get(HttpManager::class.java)
    private val syncedBookIds = mutableSetOf<String>()

    private val bookJob = scope.launch(Dispatchers.IO) {
        delay(1000)
        LogUtils.d("观察账本变更（HTTP 同步）")
        var isProcessing = false
        bookDao.flowNotSynced(Config.user.id).collect { books ->
            if (isProcessing || books.isEmpty()) return@collect
            isProcessing = true
            for (b in books) {
                syncBook(b)
            }
            isProcessing = false
        }
    }

    private val billJob = scope.launch(Dispatchers.IO) {
        delay(2000)
        LogUtils.d("观察账单变更（HTTP 同步）")
        var isProcessing = false
        billDao.flowNotSynced(Config.user.id).collect { bills ->
            if (isProcessing || bills.isEmpty()) return@collect
            isProcessing = true
            LogUtils.d("开始处理账单 count=${bills.size}...")
            for (bill in bills) {
                LogUtils.d("同步账单: ${bill.id}, synced=${bill.synced}, deleted=${bill.deleted}")
                try {
                    when {
                        bill.deleted == Status.DELETED -> {
                            httpManager.deleteBill(bill.id)
                            billDao.deleteById(bill.id)
                            LogUtils.d("账单已删除: ${bill.id}")
                        }
                        else -> {
                            ensureBookSynced(bill.bookId)
                            httpManager.createBill(bill)
                            billDao.updateSyncStatus(bill.id, Status.SYNCED)
                            LogUtils.d("账单已同步: ${bill.id}")
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.e("同步账单失败: ${bill.id}", e)
                }
            }
            LogUtils.d("本次变更账单处理完成...")
            isProcessing = false
        }
    }

    private suspend fun syncBook(b: Book) {
        LogUtils.d("同步账本: ${b.id}, synced=${b.synced}, deleted=${b.deleted}")
        try {
            when {
                b.deleted == Status.DELETED -> {
                    httpManager.deleteBook(b.id)
                    bookDao.deleteById(b.id)
                    LogUtils.d("账本已删除: ${b.id}")
                }
                else -> {
                    httpManager.createBook(b)
                    bookDao.updateSyncStatus(b.id, Status.SYNCED)
                    syncedBookIds.add(b.id)
                    LogUtils.d("账本已同步: ${b.id}")
                }
            }
        } catch (e: Exception) {
            LogUtils.e("同步账本失败: ${b.id}", e)
        }
    }

    /**
     * 确保账单对应的账本已在服务端存在。
     * 即使本地标记为 SYNCED，也可能服务端数据丢失（如重建数据库），
     * 因此首次遇到该 bookId 时主动上传一次（服务端 upsert 保证幂等）。
     */
    private suspend fun ensureBookSynced(bookId: String) {
        if (syncedBookIds.contains(bookId)) return
        val books = bookDao.findBook(bookId)
        if (books.isNotEmpty()) {
            val book = books[0]
            try {
                httpManager.createBook(book)
                bookDao.updateSyncStatus(book.id, Status.SYNCED)
                LogUtils.d("前置同步账本: ${book.id}")
            } catch (e: Exception) {
                LogUtils.e("前置同步账本失败: ${book.id}", e)
            }
        }
        syncedBookIds.add(bookId)
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