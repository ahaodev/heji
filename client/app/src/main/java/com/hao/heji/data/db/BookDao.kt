package com.hao.heji.data.db

import androidx.room.*
import com.hao.heji.data.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert
    fun insert(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(book: Book): Long

    @Update(entity = Book::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(book: Book): Int

    @Query("DELETE FROM book  WHERE book_id=:bookId")
    fun deleteById(bookId: String)

    @Query("UPDATE book SET deleted =1, synced = 0 WHERE book_id=:bookId")
    fun preDelete(bookId: String): Int

    @Query("UPDATE book SET synced =:status WHERE book_id=:bookId")
    fun updateSyncStatus(bookId: String, status: Int = Status.SYNCED): Int

    @Query("select count() from book  where name=:name")
    fun countByName(name: String): Int

    @Query("SELECT * FROM book WHERE deleted!=1 ORDER BY book_id")
    fun allBooks(): Flow<MutableList<Book>>

    @Query("SELECT * FROM book WHERE (crt_user_id=:uid or book_id=:bid) AND synced NOT IN (1, 3)")
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
