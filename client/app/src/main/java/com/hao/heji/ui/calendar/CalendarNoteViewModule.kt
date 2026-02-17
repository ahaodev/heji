package com.hao.heji.ui.calendar

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.haibin.calendarview.Calendar
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.R
import com.hao.heji.currentYearMonth
import com.hao.heji.data.db.BillDao
import com.hao.heji.ui.adapter.DayBillsNode
import com.hao.heji.ui.adapter.DayIncome
import com.hao.heji.ui.adapter.DayIncomeNode
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.utils.launchIO


internal class CalendarNoteViewModule : BaseViewModel< CalenderUiState>() {
    private val billDao: BillDao = App.dataBase.billDao()
    var selectYearMonth = currentYearMonth

    fun getImages(bid: String) {
        launchIO({
            val images = App.dataBase.imageDao().findByBillId(billId = bid)
            send(CalenderUiState.Images(images))
        })
    }

    /**
     * 更新日期
     * @param year 年
     * @param month 月
     */
    fun updateYearMonth(year: Int, month: Int) {
        launchIO({
            var map = mutableMapOf<String, Calendar>()
            billDao.findEveryDayIncomeByMonth(Config.book.id, selectYearMonth.yearMonthString())
                .filter {
                    it.expenditure.toString() != "0" || it.income.toString() != "0"
                }.forEach { dayIncome ->
                var yymmdd = dayIncome.time!!.split("-")
                val calender: Calendar = getSchemeCalendar(
                    year = yymmdd[0].toInt(),
                    month = yymmdd[1].toInt(),
                    day = yymmdd[2].toInt(),
                    expenditure = dayIncome.expenditure.toString(),
                    income = dayIncome.income.toString()
                )
                //map["${dayIncome.time}-${dayIncome.income }${dayIncome.expenditure}"] = calender
                map[calender.toString()] = calender// Key需是calendar string
            }
            send(CalenderUiState.Calender(map))
            LogUtils.d(year, month, "$map")
        }, {})
    }

    /**
     * 日账单
     * @param calendar 日历对象
     */
    fun getDayBills(calendar: Calendar) {
        LogUtils.d(calendar.toString())
        launchIO({
            val dateTime = TimeUtils.millis2String(calendar.timeInMillis, "yyyy-MM-dd")
            val dayBills = billDao.findByDay(dateTime, Config.book.id)
            var expenditure = "0"
            var income = "0"
            calendar.schemes?.forEach { scheme ->
                if (scheme.type == 1) {
                    income = scheme.obj as String
                } else {
                    expenditure = scheme.obj as String
                }
            }
            var dayIncome = DayIncome(
                expected = expenditure,
                income = income,
                year = calendar.year,
                month = calendar.month,
                monthDay = calendar.day,
                weekday = calendar.week
            )
            var parentNode = mutableListOf<BaseNode>()//天节点
            var childNodes = emptyList<BaseNode>().toMutableList()//天收支节点
            dayBills.forEach {
                it.images = App.dataBase.imageDao().findImagesId(it.id)//查询账单下照片ID
                var billsNode = DayBillsNode(it)
                childNodes.add(billsNode)
            }
            if (childNodes.size > 0) {
                parentNode.add(DayIncomeNode(childNodes, dayIncome))
            }
            send(CalenderUiState.DayBills(parentNode))
            LogUtils.d(dayIncome.toString())
        }, {})
    }

    /**
     * @param year 年
     * @param month 月
     * @param day 日
     * @param expenditure 支出
     * @param income 收入
     */
    fun getSchemeCalendar(
        year: Int,
        month: Int,
        day: Int,
        expenditure: String = "0",
        income: String = "0"
    ): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        if (expenditure != "0") {
            val expenditureScheme = Calendar.Scheme()
            expenditureScheme.type = -1
            expenditureScheme.shcemeColor = App.context.getColor(R.color.expenditure)
            expenditureScheme.obj = "-$expenditure"
            calendar.addScheme(expenditureScheme)
        }

        if (income != "0") {
            val incomeScheme = Calendar.Scheme()
            incomeScheme.type = 1
            incomeScheme.shcemeColor = App.context.getColor(R.color.income)
            incomeScheme.obj = "+$income"
            calendar.addScheme(incomeScheme)
        }
        return calendar
    }
}