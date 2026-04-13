package com.hao.heji.data.repository

import com.hao.heji.App
import com.hao.heji.data.db.Book
import com.hao.heji.network.HttpManager

class BookRepository(private val httpManager: HttpManager) {
    suspend fun findBook(bid: String) = httpManager.findBook(bid)
    suspend fun createBook(book: Book) = App.dataBase.bookDao().insert(book)

    suspend fun bookList() = httpManager.bookList()
    suspend fun sharedBook(bid: String) = httpManager.sharedBook(bid)
    suspend fun deleteBook(bid: String) = httpManager.deleteBook(bid)
    suspend fun updateBook(bid: String, bookName: String, bookType: String) =
        httpManager.updateBook(bid, bookName, bookType)

    suspend fun joinBook(sharedCode: String) = httpManager.joinBook(sharedCode)
}