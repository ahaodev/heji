package com.hao.heji.utils

import com.hao.heji.currentYearMonth
import com.hao.heji.data.converters.DateConverters
import java.io.Serializable
import java.util.*

/**
 *@date: 2021/5/11
 *@author: 锅得铁
 *#
 */
data class YearMonth(var year: Int, var month: Int = 0, var day: Int = 0) : Serializable {

    //当month =0 代表全年
    fun isYear(): Boolean = month == 0
    fun isYearMonth(): Boolean = (month != 0 && day == 0)
    fun isYearMonthDay(): Boolean = (month != 0 && day != 0)


    fun yearString() = "$year"

    fun yearMonthString() = "${year}-${if (month >= 10) month else "0$month"}"

    fun yearMonthDayString() =
        "${year}-${if (month >= 10) month else "0$month"}-${if (day >= 10) day else "0$day"}"

    fun monthDayString() =
        "${if (month >= 10) month else "0$month"}-${if (day >= 10) day else "0$day"}"

    companion object {
        fun format(date: Date?): YearMonth {
            if (Objects.isNull(date)) return currentYearMonth
            val time = DateConverters.date2Str(date).split("-")
            val year = time[0].toInt()
            val month = time[1].toInt()
            val day = time.let {
                (if (time[2].contains(" ")) time[2].split(" ")[0] else time[2]).toInt()
            }
            return YearMonth(year, month, day)
        }

        /**
         * @param yearMonth yyyy-mm
         */
        fun format(yearMonth: String): YearMonth {
            val split = yearMonth.split("-")
            return YearMonth(year = split[0].toInt(), split[1].toInt(), 0)
        }
    }


}