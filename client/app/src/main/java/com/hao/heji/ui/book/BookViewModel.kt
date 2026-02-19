package com.hao.heji.ui.book

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.Result
import com.hao.heji.data.Status
import com.hao.heji.data.db.Book
import com.hao.heji.data.db.Category
import com.github.shamil.Xid
import com.hao.heji.data.BookType
import com.hao.heji.data.BillType
import com.hao.heji.data.repository.BookRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*

class BookViewModel(private val bookRepository: BookRepository) : ViewModel() {
    private val _bookLiveData = MediatorLiveData<Book>()
    private val _bookListLiveData = MediatorLiveData<MutableList<Book>>()
    private val bookDao get() = App.dataBase.bookDao()
    private val categoryDao get() = App.dataBase.categoryDao()

    private val booksFlow = App.dataBase.bookDao().allBooks()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            booksFlow.filterNotNull().collect {
                _bookListLiveData.postValue(it)
            }
        }
    }


    fun bookCreate(): LiveData<Book> {
        return _bookLiveData
    }

    fun createNewBook(name: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = bookDao.countByName(name)
            if (count > 0) {
                ToastUtils.showLong("账本名已经存在")
            } else {
                val book = Book(
                    id = Xid.string(),
                    name = name,
                    type = type,
                    crtUserId = Config.user.id
                )
                bookRepository.createBook(book)
                insertDefaultCategories(book.id, type)
                _bookLiveData.postValue(book)
            }
        }
    }

    fun insertDefaultCategories(bookId: String, type: String) {
        val bookType = BookType.fromLabel(type) ?: return
        bookType.expenditureCategories.forEachIndexed { index, name ->
            categoryDao.insert(Category(bookId = bookId, name = name, type = BillType.EXPENDITURE.value).apply {
                this.index = index
            })
        }
        bookType.incomeCategories.forEachIndexed { index, name ->
            categoryDao.insert(Category(bookId = bookId, name = name, type = BillType.INCOME.value).apply {
                this.index = index
            })
        }
    }

    fun countBook(book_id: String): Int {
        return App.dataBase.billDao().countByBookId(book_id)
    }

    fun bookList(): LiveData<MutableList<Book>> {
        return _bookListLiveData
    }

    fun getBookList() {
        viewModelScope.launch {
            val response = bookRepository.bookList()
            response.data?.let {
                if (it.isNullOrEmpty()) {
                    ToastUtils.showLong("没有更多账本")
                    return@launch
                }
                for (book in it) {
                    book.synced = Status.SYNCED
                    if (bookDao.exist(book.id) > 0) {
                        bookDao.update(book)
                    } else {
                        bookDao.insert(book)
                    }
                }
            }
        }
    }

    fun clearBook(id: String, call: (Result<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                App.dataBase.billDao().deleteByBookId(id)
                call(Result.Success("清除账单成功"))
            } catch (e: Throwable) {
                call(Result.Error(e))
            }
        }
    }

    fun deleteBook(id: String, call: (Result<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val billsCount = App.dataBase.billDao().countByBookId(id)
                if (billsCount > 0) {
                    ToastUtils.showLong("该账本下存在账单，无法直接删除")
                } else {
                    App.dataBase.bookDao().preDelete(id)
                    withContext(Dispatchers.Main) {
                        call(Result.Success("删除成功"))
                    }
                }
            } catch (e: Throwable) {
                call(Result.Error(e))
            }
        }
    }

    fun sharedBook(bookId: String, @MainThread call: (Result<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = bookRepository.sharedBook(bid = bookId)
                response.data?.let {
                    val shareCode = response.data as String
                    withContext(Dispatchers.Main) {
                        call(Result.Success(shareCode))
                    }
                }
            } catch (e: Throwable) {
                call(Result.Error(e))
            }
        }
    }

    fun joinBook(code: String, call: (Result<String>) -> Unit) {
        viewModelScope.launch {
            try {
                call(Result.Loading)
                val response = bookRepository.joinBook(code)
                if (response.success()) {
                    call(Result.Success(response.msg ?: "OK"))
                } else {
                    call(Result.Error(RuntimeException(response.msg ?: "加入账本失败")))
                }
            } catch (e: Throwable) {
                call(Result.Error(e))
            }
        }
    }
}