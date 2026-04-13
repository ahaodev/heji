package com.hao.heji.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MyTimeUtils {
    const val PATTERN_MILLISECOND = "yyy-MM-dd HH:mm:ss:SSS"
    const val PATTERN_SECOND = "yyy-MM-dd HH:mm:ss"
    const val PATTERN_DAY = "yyy-MM-dd"

    @JvmStatic
    fun firstDayOfMonth(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val firstDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, firstDay)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    @JvmStatic
    fun lastDayOfMonth(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        val lastDay = if (month == 2) {
            calendar.getLeastMaximum(Calendar.DAY_OF_MONTH)
        } else {
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        calendar.set(Calendar.DAY_OF_MONTH, lastDay)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    @JvmStatic
    fun getMonthLastDay(year: Int, month: Int): Int {
        val a = Calendar.getInstance()
        a.set(Calendar.YEAR, year)
        a.set(Calendar.MONTH, month - 1)
        a.set(Calendar.DATE, 1)
        a.roll(Calendar.DATE, -1)
        return a.get(Calendar.DATE)
    }
}
