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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val bookId get() = Config.book.id

    fun selectTime(ym: YearMonth) {
        yearMonth = ym
        total()
        getLinChartData()
        getProportionChart()
        getReportList()
    }

     fun getReportBillInfoList(ymd: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var data: List<Bill>
            if (yearMonth.day == 0) {
                data = App.dataBase.billDao().findByMonth(ymd, type = null, bookId)
                    .filter { bill ->
                        bill.images = App.dataBase.imageDao().findImagesId(bill.id)
                        return@filter true
                    }
                send(ReportUiState.ReportBillInfoList(yearMonth.yearMonthString(), data))
            } else {
                data = App.dataBase.billDao().findByDay(ymd, bookId).filter {
                    it.images = App.dataBase.imageDao().findImagesId(it.id)
                    return@filter true
                }
                send(ReportUiState.ReportBillInfoList(yearMonth.monthDayString(), data))
            }
        }
    }

    fun getCategoryBills(category: String, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bills = App.dataBase.billDao().findByCategoryAndMonth(
                category, yearMonth.yearMonthString(), type, bookId
            ).filter {
                it.images = App.dataBase.imageDao().findImagesId(it.id)
                return@filter true
            }
            send(ReportUiState.CategoryList(category, bills))
        }
    }

    fun getImages(imagesIDs: MutableList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = App.dataBase.imageDao().findImage(imagesIDs)
            send(ReportUiState.Images(data))
        }
    }

    fun getReportList() {
        viewModelScope.launch(Dispatchers.IO) {
            val data =
                App.dataBase.billDao().listIncomeExpSurplusByMonth(yearMonth.yearMonthString(), bookId)
            send(ReportUiState.ReportList(data))
        }
    }

    fun getProportionChart(type: Int = pieDataType) {
        viewModelScope.launch(Dispatchers.IO) {
            val list =
                App.dataBase.billDao().reportCategory(type, yearMonth.yearMonthString(), bookId).map {
                    val data = it.money!!.multiply(BigDecimal(type))
                    return@map PieEntry(
                        it.percentage, it.category, data
                    )
                }
            send(ReportUiState.ProportionChart(type, list))
        }
    }

    fun getLinChartData(type: Int = lineDataType) {
        viewModelScope.launch(Dispatchers.IO) {
            if (type == BillType.ALL.value) {
                val uiStateData = with(ReportUiState.LinChart(type)) {
                    all = arrayListOf(
                        App.dataBase.billDao().sumByMonth(
                            yearMonth.yearMonthString(), BillType.EXPENDITURE.value, bookId
                        ), App.dataBase.billDao().sumByMonth(
                            yearMonth.yearMonthString(), BillType.INCOME.value, bookId
                        )
                    )
                    this
                }
                send(uiStateData)
            } else {
                val data = App.dataBase.billDao().sumByMonth(yearMonth.yearMonthString(), type, bookId)
                send(ReportUiState.LinChart(type, data))
            }
        }
    }

    private fun total() {
        viewModelScope.launch(Dispatchers.IO) {
            val monthIncomeExpenditureData =
                App.dataBase.billDao().sumMonthIncome(yearMonth.yearMonthString(), bookId)
            send(ReportUiState.Total(monthIncomeExpenditureData))
        }
    }
}