package com.hao.heji.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    fun insert(book: Book): Long

    @Update(entity = Book::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(book: Book): Int

    @Query("DELETE FROM book  WHERE book_id=:bookId")
    fun deleteById(bookId: String)

    @Query("UPDATE book SET deleted =1 WHERE book_id=:bookId")
    fun preDelete(bookId: String): Int

    @Query("UPDATE book SET synced =1 WHERE book_id=:bookId")
    fun updateSyncStatus(bookId: String): Int

    @Query("select count() from book  where name=:name")
    fun countByName(name: String): Int

    @Query("SELECT * FROM book WHERE deleted!=1 ORDER BY book_id")
    fun allBooks(): Flow<MutableList<Book>>

    @Query("SELECT * FROM book WHERE (crt_user_id=:uid or book_id=:bid)  AND synced!=1")
    fun flowNotSynced(uid:String,bid:String):Flow<MutableList<Book>>

    @Query("SELECT count(0) FROM book")
    fun count(): Int

    @Query("SELECT count(0) FROM book WHERE book_id=:bookId")
    fun exist(bookId: String): Int

    @Query("SELECT * FROM book WHERE book_id =:id")
    fun findBook(id: String): MutableList<Book>

    @Query("SELECT book_id FROM book WHERE crt_user_id=:crtUserId")
    fun findBookIdsByUser(crtUserId: String): MutableList<String>
}
