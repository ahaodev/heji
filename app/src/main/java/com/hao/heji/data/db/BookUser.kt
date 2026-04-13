package com.hao.heji.data.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
@Entity(tableName = "book_user")
class BookUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val name: String,
    @ColumnInfo(name = "authority") val authority: Int = 0
) : Parcelable {

    @Ignore
    fun fromAuthority(authority: Int = this.authority): String {
        if (authority == 0) return "创建者"
        if (authority == 1) return "用户"
        if (authority == 3) return "查账人"
        return "authority"
    }
}