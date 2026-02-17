package com.hao.heji.data.db

import android.os.Parcelable
import androidx.room.*
import com.hao.heji.config.Config
import com.hao.heji.data.converters.LogicConverters
import com.hao.heji.data.db.mongo.ObjectId
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

/**
 * 账本
 * @date: 2021/7/8
 * @author: 锅得铁
 *
 */

@Parcelize
@Entity(tableName = Book.TAB_NAME, indices = [Index(value = [Book.COLUMN_NAME], unique = true)])
data class Book(

    @param:Json(name = "_id")
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    var id: String = ObjectId().toHexString(),

    @ColumnInfo(name = COLUMN_NAME)
    var name: String,//账本名称

    @param:Json(name = "crt_user_id")
    @ColumnInfo(name = COLUMN_CREATE_USER)
    var crtUserId: String = Config.user.id,//创建人

    @param:Json(name = "type")
    @ColumnInfo(name = COLUMN_TYPE)

    var type: String? = null,//账本类型
    @param:Json(name = "crt_time")

    var crtTime: Long = System.currentTimeMillis(),
    @param:Json(name = "upd_time")
    var updTime: Long? = 0,

    @param:Json(name = "banner")
    @ColumnInfo(name = COLUMN_BANNER_URL)
    var bannerUrl: String? = null,//封面图片

    @ColumnInfo(name = "synced")
    var synced: Int = 0,

    @ColumnInfo(name = "deleted")
    var deleted: Int = 0,

    @param:Json(name = "is_initial")
    @ColumnInfo(name = COLUMN_FIRST)
    @param:TypeConverters(LogicConverters::class)
    var isInitial: Boolean = false//初始账本

) : Parcelable {

    companion object {
        const val TAB_NAME = "book"
        const val COLUMN_ID = "book_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CREATE_USER = "crt_user_id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_BANNER_URL = "banner_url"
        const val COLUMN_ANCHOR = "anchor"
        const val COLUMN_FIRST = "is_initial"
        const val COLUMN_SYNC_STATUS = "sync_status"
    }
}
