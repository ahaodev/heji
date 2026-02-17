package com.hao.heji.ui.report

import com.blankj.utilcode.util.LogUtils
import com.github.mikephil.charting.data.PieEntry
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.currentYearMonth
import com.hao.heji.data.BillType
import com.hao.heji.data.db.Bill
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.utils.YearMonth
import com.hao.heji.utils.launchIO
import java.math.BigDecimal

/**
 * 统计ViewModel
 *
 */
class ReportViewModel : BaseViewModel<ReportUiState>() {
    /**
     * 日期
     */
    var yearMonth: YearMonth = currentYearMonth
        set(value) {
            isAnnual = value.isYear()
            field = value
            LogUtils.d("${field.yearString()}annual : $value ")
        }

    /**
     * Annual 年度报表，默认月报表
     */
    private var isAnnual: Boolean = false

    private var lineDataType: Int = BillType.EXPENDITURE.value

    private var pieDataType: Int = BillType.EXPENDITURE.value

    fun selectTime(ym: YearMonth) {
        yearMonth = ym
        total()
        getLinChartData()
        getProportionChart()
        getReportList()
    }

     fun getReportBillInfoList(ymd: String) {
        launchIO({
            var data: MutableList<Bill>
            if (yearMonth.day == 0) {//按月查
                //TODO 目前暂未实现按年统计
                data = App.dataBase.billDao().findByMonth(ymd, type = null, Config.book.id)
                    .filter { bill ->
                        bill.images = App.dataBase.imageDao().findImagesId(bill.id)
                        return@filter true
                    }.toMutableList()
                send(ReportUiState.ReportBillInfoList(yearMonth.yearMonthString(), data))
            } else {//按天查
                data = App.dataBase.billDao().findByDay(ymd).filter {
                    it.images = App.dataBase.imageDao().findImagesId(it.id)
                    return@filter true
                }.toMutableList()
                send(ReportUiState.ReportBillInfoList(yearMonth.monthDayString(), data))
            }
        })
    }

    fun getCategoryBills(category: String, type: Int) {
        launchIO({
            val bills = App.dataBase.billDao().findByCategoryAndMonth(
                category, yearMonth.yearMonthString(), type
            ).filter {
                it.images = App.dataBase.imageDao().findImagesId(it.id)
                return@filter true
            }.toMutableList()
            send(ReportUiState.CategoryList(category, bills))
        })
    }

    fun getImages(imagesIDs: MutableList<String>) {
        launchIO({
            val data = App.dataBase.imageDao().findImage(imagesIDs)
            send(ReportUiState.Images(data))
        })
    }

    fun getReportList() {
        launchIO({
            var data =
                App.dataBase.billDao().listIncomeExpSurplusByMonth(yearMonth.yearMonthString())
            send(ReportUiState.ReportList(data))
        })
    }

    fun getProportionChart(type: Int = pieDataType) {
        launchIO({
            val list =
                App.dataBase.billDao().reportCategory(type, yearMonth.yearMonthString()).map {
                    // 支出为负数，收入为正数  money * -1 or money * 1
                    val data = it.money!!.multiply(BigDecimal(type))
                    return@map PieEntry(
                        it.percentage, it.category, data
                    )
                }.toMutableList()
            send(ReportUiState.ProportionChart(type, list))
        })
    }

    fun getLinChartData(type: Int = lineDataType) {
        launchIO({
            if (type == BillType.ALL.value) {
                val uiStateData = with(ReportUiState.LinChart(type)) {
                    all = arrayListOf(
                        App.dataBase.billDao().sumByMonth(
                            yearMonth.yearMonthString(), BillType.EXPENDITURE.value
                        ), App.dataBase.billDao().sumByMonth(
                            yearMonth.yearMonthString(), BillType.INCOME.value
                        )
                    )
                    this
                }
                send(uiStateData)
            } else {
                val data = App.dataBase.billDao().sumByMonth(yearMonth.yearMonthString(), type)
                send(ReportUiState.LinChart(type, data))
            }
        })
    }

    private fun total() {
        launchIO({
            val monthIncomeExpenditureData =
                App.dataBase.billDao().sumMonthIncomeExpenditure(yearMonth.yearMonthString())
            send(ReportUiState.Total(monthIncomeExpenditureData))
        })
    }
}