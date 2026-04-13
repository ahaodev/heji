package com.hao.heji.data.repository

import com.hao.heji.App
import com.hao.heji.data.BillType
import com.hao.heji.data.BookType
import com.hao.heji.data.Status
import com.hao.heji.data.db.Book
import com.hao.heji.data.db.Category
import com.hao.heji.network.HttpManager

class BookRepository(private val httpManager: HttpManager) {
    private val bookDao get() = App.dataBase.bookDao()
    private val billDao get() = App.dataBase.billDao()
    private val categoryDao get() = App.dataBase.categoryDao()

    suspend fun findBook(bid: String) = httpManager.findBook(bid)
    suspend fun createBook(book: Book) = bookDao.insert(book)

    suspend fun bookList() = httpManager.bookList()
    suspend fun sharedBook(bid: String) = httpManager.sharedBook(bid)
    suspend fun deleteBook(bid: String) = httpManager.deleteBook(bid)
    suspend fun updateBook(bid: String, bookName: String, bookType: String) =
        httpManager.updateBook(bid, bookName, bookType)

    suspend fun joinBook(sharedCode: String) = httpManager.joinBook(sharedCode)

    fun observeBooks() = bookDao.allBooks()

    fun countByName(name: String): Int = bookDao.countByName(name)

    fun findBookIdsByUser(userId: String): MutableList<String> = bookDao.findBookIdsByUser(userId)

    fun findLocalBook(bookId: String): Book? = bookDao.findBook(bookId).firstOrNull()

    fun upsertRemoteBooks(books: List<Book>) {
        books.forEach { book ->
            book.synced = Status.SYNCED
            if (bookDao.exist(book.id) > 0) {
                bookDao.update(book)
            } else {
                bookDao.insert(book)
            }
        }
    }

    fun createDefaultBook(userId: String, bookType: BookType = BookType.DAILY): Book {
        val book = Book(
            name = bookType.label,
            crtUserId = userId,
            type = bookType.label,
        )
        bookDao.insert(book)
        insertDefaultCategories(book.id, bookType.label)
        return book
    }

    fun insertDefaultCategories(bookId: String, type: String) {
        val bookType = BookType.fromLabel(type) ?: return
        bookType.expenditureCategories.forEachIndexed { index, name ->
            categoryDao.insert(
                Category(
                    bookId = bookId,
                    name = name,
                    type = BillType.EXPENDITURE.value,
                ).apply {
                    this.index = index
                }
            )
        }
        bookType.incomeCategories.forEachIndexed { index, name ->
            categoryDao.insert(
                Category(
                    bookId = bookId,
                    name = name,
                    type = BillType.INCOME.value,
                ).apply {
                    this.index = index
                }
            )
        }
    }

    fun countBookBills(bookId: String): Int = billDao.countByBookId(bookId)

    fun clearBookBills(bookId: String): Int = billDao.deleteByBookId(bookId)

    fun preDeleteBook(bookId: String): Int = bookDao.preDelete(bookId)
}
