package com.hao.heji.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hao.heji.App
import com.hao.heji.BuildConfig
import com.hao.heji.data.converters.BookUsersConverters
import com.hao.heji.data.converters.DateConverters
import com.hao.heji.data.converters.MoneyConverters
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.BillDao
import com.hao.heji.data.db.BillWithImageDao
import com.hao.heji.data.db.Book
import com.hao.heji.data.db.BookDao
import com.hao.heji.data.db.BookUSerDao
import com.hao.heji.data.db.BookUser
import com.hao.heji.data.db.Category
import com.hao.heji.data.db.CategoryDao
import com.hao.heji.data.db.Image
import com.hao.heji.data.db.ImageDao
import java.util.concurrent.Executors

/**
 * @date: 2020/8/28
 * @author: 锅得铁
 * #
 */
@Database(
    entities = [
        Book::class,
        BookUser::class,
        Category::class,
        Bill::class,
        Image::class,
    ],
    version = 2
)
@TypeConverters(DateConverters::class, MoneyConverters::class, BookUsersConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookUserDao(): BookUSerDao
    abstract fun billDao(): BillDao
    abstract fun imageDao(): ImageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun billImageDao(): BillWithImageDao

    override fun clearAllTables() {}

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //默认数据库名称
        fun getInstance(userName: String, context: Context = App.context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, "${userName}.db").also { INSTANCE = it }
            }

        private fun buildDatabase(
            context: Context,
            dbName: String
        ) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, dbName
        )
            .fallbackToDestructiveMigration(dropAllTables = true) //暴力丢弃强插升级
            .setQueryCallback({ sqlQuery, bindArgs ->
                if (BuildConfig.DEBUG) {
                    val filledSql = bindArgs.fold(sqlQuery) { sql, arg ->
                        sql.replaceFirst("?", when (arg) {
                            is String -> "'$arg'"
                            null -> "NULL"
                            else -> arg.toString()
                        })
                    }
                    Log.d("SQL", filledSql)
                }
            }, Executors.newSingleThreadExecutor())
            .allowMainThreadQueries() //.addMigrations(MIGRATION_1_2)
            .build()

        /**
         * 升级
         */
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //do
            }
        }


    }

    fun reset() {
        INSTANCE?.let {
            if (it.isOpen) {
                it.close()
            }
        }
        INSTANCE = null
    }
}