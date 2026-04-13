package com.hao.heji.data.db

import android.os.Parcelable
import androidx.room.*
import com.hao.heji.config.Config
import com.hao.heji.data.Status
import com.hao.heji.data.converters.Iso8601ToLongSerializer
import com.hao.heji.data.converters.NullableIso8601ToLongSerializer
import com.github.shamil.Xid
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 账本
 * @date: 2021/7/8
 * @author: 锅得铁
 *
 */

@Serializable
@Parcelize
@Entity(tableName = Book.TAB_NAME, indices = [Index(value = [Book.COLUMN_NAME], unique = true)])
data class Book(

    @SerialName("_id")
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    var id: String = Xid.string(),

    @ColumnInfo(name = COLUMN_NAME)
    var name: String,//账本名称

    @SerialName("crt_user_id")
    @ColumnInfo(name = COLUMN_CREATE_USER)
    var crtUserId: String = Config.user.id,//创建人

    @SerialName("type")
    @ColumnInfo(name = COLUMN_TYPE)

    var type: String? = null,//账本类型
    @SerialName("crt_time")
    @kotlinx.serialization.Serializable(with = Iso8601ToLongSerializer::class)
    var crtTime: Long = System.currentTimeMillis(),
    @SerialName("upd_time")
    @kotlinx.serialization.Serializable(with = NullableIso8601ToLongSerializer::class)
    var updTime: Long? = 0,

    @SerialName("banner")
    @ColumnInfo(name = COLUMN_BANNER_URL)
    var bannerUrl: String? = null,//封面图片

    @ColumnInfo(name = "synced")
    var synced: Int = Status.NOT_SYNCED,

    @ColumnInfo(name = "deleted")
    var deleted: Int = Status.NOT_DELETED,

) : Parcelable {

    companion object {
        const val TAB_NAME = "book"
        const val COLUMN_ID = "book_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CREATE_USER = "crt_user_id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_BANNER_URL = "banner_url"
        const val COLUMN_ANCHOR = "anchor"

        const val COLUMN_SYNC_STATUS = "sync_status"
    }
}
