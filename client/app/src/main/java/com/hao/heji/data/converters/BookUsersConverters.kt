package com.hao.heji.data.converters

import androidx.room.TypeConverter
import com.hao.heji.data.db.BookUser
import com.hao.heji.json
import kotlinx.serialization.encodeToString

object BookUsersConverters {

    @JvmStatic
    @TypeConverter
    fun str2Users(value: String?): MutableList<BookUser> {
        return if (value.isNullOrEmpty()||value =="null") {
            mutableListOf()
        } else json.decodeFromString<MutableList<BookUser>>(value)

    }

    @JvmStatic
    @TypeConverter
    fun users2Str(users: MutableList<BookUser>?): String {
        return if (users?.isEmpty() == true) "null" else json.encodeToString(users)
    }
}