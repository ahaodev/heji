package com.hao.heji.data.converters

import androidx.room.TypeConverter
import com.blankj.utilcode.util.TimeUtils
import java.util.*

/**
 * @date: 2020/9/20
 * @author: 锅得铁
 * #时间转换 -注意查的时候月份-1 ，存的时候不处理
 */
object DateConverters {
    const val DB_PATTERN = "yyyy-MM-dd HH:mm:ss"
    const val SHOW_PATTERN = "yyyy-MM-dd HH:mm"

    @JvmStatic
    @TypeConverter
    fun str2Date(value: String?): Date {
        return if (value == null) Date() else TimeUtils.string2Date(value, DB_PATTERN)
    }

    @JvmStatic
    @TypeConverter
    fun date2Str(date: Date?): String {
        return if (date == null) "null" else TimeUtils.date2String(date, DB_PATTERN)
    }
}