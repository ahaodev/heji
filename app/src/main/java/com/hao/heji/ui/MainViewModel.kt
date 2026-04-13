package com.hao.heji.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.hao.heji.config.Config
import com.hao.heji.data.repository.BookRepository
import com.hao.heji.utils.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * @date: 2020/11/3
 * @author: 锅得铁
 * # APP运行时 UI常量共享存储
 */
class MainViewModel(private val bookRepository: BookRepository) : ViewModel() {

    /**
     * 全局选择的年月（home to subpage）
     */
    var globalYearMonth: YearMonth =
        YearMonth(Calendar.getInstance()[Calendar.YEAR], Calendar.getInstance()[Calendar.MONTH] + 1)

    /**
     * 选择账本
     */
    fun switchModelAndBook() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!Config.enableOfflineMode) {
                try {
                    bookRepository.bookList().data?.let(bookRepository::upsertRemoteBooks)
                } catch (e: Exception) {
                    LogUtils.e("拉取远程账本列表失败，使用本地数据", e)
                }
            }

            val books = bookRepository.findBookIdsByUser(Config.user.id)
            if (books.isEmpty()) {
                Config.setBook(bookRepository.createDefaultBook(Config.user.id))
                return@launch
            }
            // 优先使用上次保存的账本（如果仍存在）
            val savedBook = Config.bookOrNull
            if (savedBook != null && books.contains(savedBook.id)) {
                // 从数据库重新加载以确保数据最新
                bookRepository.findLocalBook(savedBook.id)?.let {
                    Config.setBook(it)
                }
                return@launch
            }

            // 没有保存的账本或已被删除，使用第一个
            books.firstOrNull()?.let { bookId ->
                bookRepository.findLocalBook(bookId)?.let {
                    Config.setBook(it)
                }
            }
        }
    }
}
