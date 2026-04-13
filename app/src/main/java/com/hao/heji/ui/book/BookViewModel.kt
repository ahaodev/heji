package com.hao.heji.ui.book

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.config.Config
import com.hao.heji.data.Result
import com.hao.heji.data.db.Book
import com.github.shamil.Xid
import com.hao.heji.data.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookViewModel(private val bookRepository: BookRepository) : ViewModel() {
    private val _bookLiveData = MediatorLiveData<Book>()
    private val _bookListLiveData = MediatorLiveData<MutableList<Book>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.observeBooks().filterNotNull().collect {
                _bookListLiveData.postValue(it)
            }
        }
    }


    fun bookCreate(): LiveData<Book> {
        return _bookLiveData
    }

    fun createNewBook(name: String, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = bookRepository.countByName(name)
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
                bookRepository.insertDefaultCategories(book.id, type)
                _bookLiveData.postValue(book)
            }
        }
    }

    fun countBook(id: String): Int {
        return bookRepository.countBookBills(id)
    }

    fun bookList(): LiveData<MutableList<Book>> {
        return _bookListLiveData
    }

    fun getBookList() {
        viewModelScope.launch {
            val response = bookRepository.bookList()
            response.data?.let {
                if (it.isEmpty()) {
                    ToastUtils.showLong("没有更多账本")
                    return@launch
                }
                bookRepository.upsertRemoteBooks(it)
            }
        }
    }

    fun clearBook(id: String, call: (Result<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bookRepository.clearBookBills(id)
                call(Result.Success("清除账单成功"))
            } catch (e: Throwable) {
                call(Result.Error(e))
            }
        }
    }

    fun deleteBook(id: String, call: (Result<String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val billsCount = bookRepository.countBookBills(id)
                if (billsCount > 0) {
                    ToastUtils.showLong("该账本下存在账单，无法直接删除")
                } else {
                    bookRepository.preDeleteBook(id)
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
