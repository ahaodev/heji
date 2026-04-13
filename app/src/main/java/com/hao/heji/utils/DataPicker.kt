package com.hao.heji.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import com.hao.heji.string
import java.util.*

/**
 * TODO
 * @date 2022/5/16
 * @author 锅得铁
 * @since v1.0
 */
class DataPicker(val activity: Activity) {
    fun selectDay(calendar: Calendar) {
        val onDateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                calendar[year, month] = dayOfMonth
                calendar.time.string()

                selectHourAndMinute(
                    year = year,
                    month = month + 1,//实际保存时，选择的时间需要+1（month：0-11 ）
                    dayOfMonth = dayOfMonth,
                    hourOfDay = calendar[Calendar.HOUR_OF_DAY],
                    minute = calendar[Calendar.MINUTE]
                )
            }
        val yearMonth = YearMonth.format(calendar.time)
        val dialog = DatePickerDialog(
            activity,
            onDateSetListener,
            yearMonth.year,
            yearMonth.month - 1,
            yearMonth.day
        )
        dialog.setOnDateSetListener(onDateSetListener)
        dialog.show()
    }


    /**
     * 选择小时和分钟
     *
     * @param yearTime 年份-月份
     */
    private fun selectHourAndMinute(
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hourOfDay: Int,
        minute: Int,
    ) {
        val onTimeSetListener =
            TimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int ->
                if (hourOfDay == 0 && minute == 0) return@OnTimeSetListener
                val selectTime =
                    "$year-$month-$dayOfMonth $hourOfDay:$minute:00"//yyyy-MM-dd hh:mm:00
                //setTime(DateConverters.str2Date(selectTime))
            }
        val timePickerDialog =
            TimePickerDialog(activity, onTimeSetListener, hourOfDay, minute, true)
        timePickerDialog.show()
    }
}