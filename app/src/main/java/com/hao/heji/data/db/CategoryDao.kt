package com.hao.heji.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * @date: 2020/9/16
 * @author: 锅得铁
 * #
 */
@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(category: Category)

    @Query("SELECT count(0) FROM category")
    fun count(): Int

    @Query("SELECT count(0) FROM category WHERE hashValue=:hasCode")
    fun exist(hasCode: Int): Int

    @Query("SELECT _id FROM category WHERE _id=:id")
    fun findByID(id: String): String

    @Query("SELECT * FROM category WHERE name=:name and type=:type")
    fun findByNameAndType(name: String, type: Int): MutableList<Category>

    @Query("SELECT * FROM  category WHERE book_id=:bookID AND type =:type AND deleted != 1 ORDER BY `index` DESC,_id DESC ")
    fun observeIncomeOrExpenditure(bookID: String, type: Int): Flow<MutableList<Category>>

    @Query("SELECT * FROM  category WHERE book_id=:bookID AND type =:type AND deleted != 1 ORDER BY `index` DESC,_id DESC ")
    fun findIncomeOrExpenditure(bookID: String, type: Int): MutableList<Category>

    @Query("SELECT * FROM category WHERE book_id=:bookID AND type=:type AND (parentId IS NULL OR parentId = '') AND deleted != 1 ORDER BY `index` DESC, _id DESC")
    fun observeParentCategories(bookID: String, type: Int): Flow<MutableList<Category>>

    @Query("SELECT * FROM category WHERE book_id=:bookID AND type=:type AND (parentId IS NULL OR parentId = '') AND deleted != 1 ORDER BY `index` DESC, _id DESC")
    fun findParentCategories(bookID: String, type: Int): MutableList<Category>

    @Query("SELECT * FROM category WHERE parentId=:parentId AND deleted != 1 ORDER BY `index` DESC, _id DESC")
    fun findChildCategories(parentId: String): MutableList<Category>

    @Query("SELECT * FROM category WHERE parentId=:parentId AND deleted != 1 ORDER BY `index` DESC, _id DESC")
    fun observeChildCategories(parentId: String): Flow<MutableList<Category>>


    @Query("SELECT * FROM  category WHERE deleted == 0 or synced == 0")
    fun observeNotUploadOrDelete(): Flow<MutableList<Category>>

    @Query("SELECT * FROM category WHERE name =:name")
    fun queryByCategoryName(name: String): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(category: Category)

    @Delete
    fun delete(category: Category) // updateOrders();

    @Query("DELETE FROM bill WHERE bill_id=:id")
    fun deleteById(id: String)
}