package com.hao.heji.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.BillType
import com.hao.heji.data.BookType
import com.hao.heji.data.Status
import com.hao.heji.data.db.Book
import com.hao.heji.data.db.Category
import com.hao.heji.data.repository.BookRepository
import com.hao.heji.utils.YearMonth
import com.hao.heji.utils.launch
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
        launch({
            val bookDao = App.dataBase.bookDao()
            if (!Config.enableOfflineMode) {
                bookRepository.bookList().data?.let {
                    it.forEach { book ->
                        book.synced = Status.SYNCED
                        val exist = bookDao.exist(book.id) > 0
                        if (exist)
                            bookDao.update(book)
                        else
                            bookDao.insert(book)
                    }
                }
            }

            val books = bookDao.findBookIdsByUser(Config.user.id)
            if (books.isEmpty()) {
                createDefaultBook(bookDao)
                return@launch
            }

            // 优先使用上次保存的账本（如果仍存在）
            val savedBook = Config.bookOrNull
            if (savedBook != null && books.contains(savedBook.id)) {
                // 从数据库重新加载以确保数据最新
                bookDao.findBook(savedBook.id).firstOrNull()?.let {
                    Config.setBook(it)
                }
                return@launch
            }

            // 没有保存的账本或已被删除，使用第一个
            books.firstOrNull()?.let { bookId ->
                bookDao.findBook(bookId).firstOrNull()?.let {
                    Config.setBook(it)
                }
            }
        })
    }

    private fun createDefaultBook(bookDao: com.hao.heji.data.db.BookDao) {
        val bookType = BookType.DAILY
        val book = Book(
            name = bookType.label,
            crtUserId = Config.user.id,
            type = bookType.label,
        )
        bookDao.insert(book)
        val categoryDao = App.dataBase.categoryDao()
        bookType.expenditureCategories.forEachIndexed { index, name ->
            categoryDao.insert(Category(bookId = book.id, name = name, type = BillType.EXPENDITURE.value).apply {
                this.index = index
            })
        }
        bookType.incomeCategories.forEachIndexed { index, name ->
            categoryDao.insert(Category(bookId = book.id, name = name, type = BillType.INCOME.value).apply {
                this.index = index
            })
        }
        Config.setBook(book)
    }


}