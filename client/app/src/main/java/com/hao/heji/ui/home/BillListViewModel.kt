package com.hao.heji.ui.home

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.hao.heji.App
import com.hao.heji.config.Config
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
            val monthEveryDayIncome =
                App.dataBase.billDao().findEveryDayIncomeByMonth(Config.book.id, yearMonth)
            //日节点
            val listDayNodes = mutableListOf<BaseNode>()
            monthEveryDayIncome.forEach { dayIncome ->
                val yymmdd = dayIncome.time!!.split("-")
                val incomeNode = DayIncome(
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
                val dayBills = App.dataBase.billDao().findByDay(dayIncome.time!!, Config.book.id)
                // 批量查询当日所有账单的图片ID
                val billIds = dayBills.map { it.id }
                val imageMap = App.dataBase.imageDao().findImagesIdByBillIds(billIds)
                    .groupBy({ it.billId }, { it.imageId })
                dayBills.forEach {
                    it.images = (imageMap[it.id] ?: emptyList()).toMutableList()
                    dayListNodes.add(DayBillsNode(it))
                }
                val dayItemNode = DayIncomeNode(dayListNodes, incomeNode)
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
            App.dataBase.billDao().sumIncome(yearMonth, Config.book.id).distinctUntilChanged().collect {
                LogUtils.d(it)
                send(BillListUiState.Summary(it))
            }
        }, {
            send(BillListUiState.Error(it))
        })

    }
}