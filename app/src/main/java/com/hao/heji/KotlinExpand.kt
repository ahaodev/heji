package com.hao.heji

import com.hao.heji.data.converters.DateConverters
import java.util.*

/**
 * Kotlin 顶级扩展函数
 *@date 2022/3/1
 *@author 锅得铁
 *@constructor default constructor
 */

fun Date.string(): String {
    return DateConverters.date2Str(this)
}

fun String.date(): Date {
    return DateConverters.str2Date(this)
}

fun Date.calendar(): Calendar {
    val instance = Calendar.getInstance()
    instance.time = this
    return instance
}

/**
 * 检查String抛出异常
 */
fun String.requireNonEmpty(errorMessage: String) {
    if (isEmpty()) throw RuntimeException(errorMessage)
}