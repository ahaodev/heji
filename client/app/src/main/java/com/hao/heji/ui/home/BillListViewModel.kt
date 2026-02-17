package com.hao.heji.ui.home

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.hao.heji.App
import com.hao.heji.ui.adapter.DayBillsNode
import com.hao.heji.ui.adapter.DayIncome
import com.hao.heji.ui.adapter.DayIncomeNode
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.utils.MyTimeUtils
import com.hao.heji.utils.launchIO
import kotlinx.coroutines.flow.distinctUntilChanged

internal class BillListViewModel : BaseViewModel<BillListUiState>() {

    fun getImages(billId: String) {
        launchIO({
            val data = App.dataBase.imageDao().findByBillId(billId = billId)
            send(BillListUiState.Images(data))
        })
    }

    /**
     * yyyy-mm
     */
    fun getMonthBills(yearMonth: String) {
        launchIO({
            //根据月份查询收支的日子
            var monthEveryDayIncome =
                App.dataBase.billDao().findEveryDayIncomeByMonth(yearMonth = yearMonth)
            //日节点
            var listDayNodes = mutableListOf<BaseNode>()
            monthEveryDayIncome.forEach { dayIncome ->
                var yymmdd = dayIncome.time!!.split("-")
                var incomeNode = DayIncome(
                    expected = dayIncome.expenditure.toString(),
                    income = dayIncome.income.toString(),
                    year = yymmdd[0].toInt(),
                    month = yymmdd[1].toInt(),
                    monthDay = yymmdd[2].toInt(),
                    weekday = TimeUtils.getChineseWeek(
                        dayIncome.time,
                        TimeUtils.getSafeDateFormat(MyTimeUtils.PATTERN_DAY)
                    )
                )
                //日节点下子账单
                val dayListNodes = mutableListOf<BaseNode>()
                App.dataBase.billDao().findByDay(dayIncome.time!!).forEach {
                    it.images = App.dataBase.imageDao().findImagesId(it.id)
                    dayListNodes.add(DayBillsNode(it))
                }
                var dayItemNode = DayIncomeNode(dayListNodes, incomeNode)
                listDayNodes.add(dayItemNode)
            }
            LogUtils.d("Select YearMonth:${yearMonth} ${listDayNodes.size}")
            send(BillListUiState.Bills(listDayNodes))
        }, {
            send(BillListUiState.Error(it))
        })
    }

    /**
     * 获取收入支出 总览
     * @param yearMonth yyyy:mm
     */
    fun getSummary(yearMonth: String) {
        launchIO({
            LogUtils.d("Between by time:$yearMonth")
            App.dataBase.billDao().sumIncome(yearMonth).distinctUntilChanged().collect {
                LogUtils.d(it)
                send(BillListUiState.Summary(it))
            }
        }, {
            send(BillListUiState.Error(it))
        })

    }
}