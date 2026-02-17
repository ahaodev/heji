package com.hao.heji.ui.report

import com.github.mikephil.charting.data.PieEntry
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Image
import com.hao.heji.data.db.dto.BillTotal
import com.hao.heji.data.db.dto.Income
import com.hao.heji.data.db.dto.IncomeTimeSurplus
import com.hao.heji.ui.base.IUiState

/**
 * @see ReportFragment
 *
 */
sealed interface ReportUiState : IUiState {
    /**
     * 统计总览
     */
    class Total(val data: Income) : ReportUiState

    /**
     * 折线图
     *
     * @property type
     * @property data
     */
    class LinChart(val type: Int, val data: List<BillTotal> = emptyList()) :
        ReportUiState {
        var all: ArrayList<List<BillTotal>> = arrayListOf()
    }

    /**
     * 占比（饼状和条形）
     *
     * @property data
     */
    class ProportionChart(val type: Int, val data: List<PieEntry>) : ReportUiState

    /**
     * 报表
     * @property data
     */
    class ReportList(val data: List<IncomeTimeSurplus>) : ReportUiState

    /**
     * 账单图片
     *
     * @property data images
     */
    class Images(val data: MutableList<Image>) : ReportUiState

    /**
     * 分类账单列表 show Popup bill list
     *
     * @property data
     */
    class CategoryList(val category: String, val data: List<Bill>) : ReportUiState

    /**
     * 列表 day list
     *
     * @property data
     */
    class ReportBillInfoList(val time: String, val data: List<Bill>) : ReportUiState
}