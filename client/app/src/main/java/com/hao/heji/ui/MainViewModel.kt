package com.hao.heji.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.db.Book
import com.hao.heji.data.repository.BookRepository
import com.hao.heji.launchIO
import com.hao.heji.utils.YearMonth
import com.hao.heji.utils.launch
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
        launch({
            val bookDao = App.dataBase.bookDao()
            //本地账本 离线模式
            if (Config.enableOfflineMode) {
                if (bookDao.count() == 0) {
                    bookDao.insert(Config.book)
                } else {
                    val books = bookDao.findInitBook(Config.user.id)
                    books.firstOrNull()?.let {
                        Config.setBook(it)
                    }
                }
            } else {
                //协同账本在线模式
                bookRepository.bookList().data?.let {
                    it.forEach { book ->
                        book.synced=1
                        val exist = bookDao.exist(book.id) > 0
                        if (exist)
                            bookDao.update(book)
                        else
                            bookDao.insert(book)
                        if (book.isInitial) {
                            Config.setBook(book)
                        }
                    }
                }
                val books = bookDao.findBookIdsByUser(Config.user.id)//查询本地是否存在账本
                if (books.isEmpty()) {
                    //本地新建账本
                    val book = Config.book
                    book.crtUserId = Config.user.id
                    book.type = "生活账本"
                    bookRepository.createBook(book)
                    Config.setBook(book)
                }
            }
        })
    }


}