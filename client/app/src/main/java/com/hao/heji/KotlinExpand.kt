package com.hao.heji

import com.hao.heji.data.converters.DateConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

fun String.getObjectTime(): Date {
    val time = "${Integer.parseInt(this.substring(0, 8), 16)}000".toLong()
    return Date(time)
}
/**
 * 检查String抛出异常
 */
inline fun String.requireNonEmpty(errorMessage: String) {
    if (isEmpty()) throw RuntimeException(errorMessage)
}

fun CoroutineScope.launchIO(
    block: suspend () -> Unit,
    error: suspend (Throwable) -> Unit = { it.printStackTrace() }
): Job {
    return launch(Dispatchers.IO) {
        try {
            block()
        } catch (e: Throwable) {
            error(e)
            e.printStackTrace()
        }
    }
}
fun CoroutineScope.launch(
    block: suspend () -> Unit,
    error: suspend (Throwable) -> Unit = { it.printStackTrace() }
): Job {
    return launch() {
        try {
            block()
        } catch (e: Throwable) {
            error(e)
            e.printStackTrace()
        }
    }
}