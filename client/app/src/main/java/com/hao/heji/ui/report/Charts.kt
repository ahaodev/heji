package com.hao.heji.ui.report

import android.graphics.Color
import android.text.SpannableString
import android.widget.TextView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.hao.heji.R
import com.hao.heji.currentYearMonth
import com.hao.heji.data.BillType
import com.hao.heji.data.db.dto.BillTotal
import com.hao.heji.utils.MyTimeUtils
import com.hao.heji.utils.YearMonth
import java.util.*

/**
 * 统计ReportFragment折线图样式设置
 * @author hao
 */
internal fun ReportFragment.lineChartStyle(lineChart: LineChart) {
    lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry, h: Highlight) {
        }

        override fun onNothingSelected() {
        }
    })
    lineChart.setDrawGridBackground(false)
    lineChart.setTouchEnabled(true)
    var markerView: MarkerView = object : MarkerView(mainActivity, R.layout.marker_linechart) {
        val markerContext = findViewById<TextView>(R.id.tvContext)
        override fun refreshContent(e: Entry, highlight: Highlight?) {
            var sourceString = "${e.x.toInt()}日\n${e.data}:${e.y}"
            if (e.y == 0f)
                sourceString = "${e.x.toInt()}日\n无记录"
            val spannableString = SpannableString(sourceString)
            markerContext.text = spannableString
            super.refreshContent(e, highlight)
        }
    }
    markerView.chartView = lineChart
    lineChart.marker = markerView

    val xAxis: XAxis = lineChart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
    xAxis.setDrawAxisLine(false)
    xAxis.setDrawGridLines(false)
    //xAxis.axisMinimum = 1f
    xAxis.textSize = 16f
//    xAxis.axisLineColor = Color.parseColor("#93A8B1")
//    xAxis.textColor = Color.parseColor("#566974")
    xAxis.mLabelWidth
    xAxis.granularity = 1f
    //xAxis.valueFormatter = LargeValueFormatter()
    //xAxis.labelCount = 11//强制显示X 不设置则缩
    xAxis.labelRotationAngle = 45f
    lineChart.axisRight.isEnabled = false
    lineChart.extraLeftOffset = 6f
    lineChart.axisLeft.valueFormatter = object : LargeValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            if (value < 1000) return value.toString()//金额小于1K不格式化
            return super.getFormattedValue(value)
        }
    }
    lineChart.xAxis.valueFormatter = IndexAxisValueFormatter()
    lineChart.axisLeft.axisMinimum = 0f//设置不显示负数
    lineChart.axisRight.axisMinimum = 0f
    lineChart.description.isEnabled = false//不显示description label
}

private fun lineDataSetStyle() {

}

private fun lineChartConvertAdapter(bills: List<BillTotal>, yearMonth: YearMonth): LineDataSet {
    val map = mutableMapOf<String, Entry>()
    if (yearMonth.isYear()) {
        for (day in 1..12) {
            val x = if (day < 10) "0$day" else day.toString()
            val entry = Entry(x.toFloat(), 0f)
            map[x] = entry
        }
        bills.map {
            val month = it.time.split("-")[1]
            map.replace(
                month,
                Entry(month.toFloat(), it.money.toFloat(), BillType.transform(it.type).valueString)
            )
            return@map Entry(
                month.toFloat(),
                it.money.toFloat(),
                BillType.transform(it.type).valueString
            )
        }.toMutableList()
        val entries = map.values.toMutableList()
        return LineDataSet(entries, parserBillsType(bills))
    } else {
        var dayCount = MyTimeUtils.getMonthLastDay(
            yearMonth.year,
            yearMonth.month
        )
        if (bills.isNotEmpty() && currentYearMonth == yearMonth && bills.last().time.split("-")[2].toInt() < currentYearMonth.day) {
            dayCount = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        }
        for (day in 1..dayCount) {
            val x = if (day < 10) "0$day" else day.toString()
            val entry = Entry(x.toFloat(), 0f)
            map[x] = entry
        }
        bills.map {
            val day = it.time.split("-")[2]
            map.replace(
                day,
                Entry(day.toFloat(), it.money.toFloat(), BillType.transform(it.type).valueString)
            )
            return@map Entry(day.toFloat(), it.money.toFloat(), BillType.transform(it.type).valueString)
        }.toMutableList()

        val entries = map.values.toMutableList()
        return LineDataSet(entries, parserBillsType(bills))
    }
}

private fun parserBillsType(bills: List<BillTotal>) =
    if (bills.isEmpty()) "收入" else BillType.transform(bills[0].type).valueString

fun ReportFragment.setIncomeLineChartNodes(yearMonth: YearMonth, bills: MutableList<BillTotal>) {
    lineChartConvertAdapter(bills, yearMonth).apply {
        lineDataSetStyle(this, R.color.income, R.drawable.shape_gradient_income)
        binding.lineChart.data = LineData(this)
        binding.lineChart.invalidate()
    }
}

internal fun ReportFragment.setExpenditureLineChartNodes(yearMonth: YearMonth, bills: MutableList<BillTotal>) {
    lineChartConvertAdapter(bills, yearMonth).apply {
        lineDataSetStyle(this, R.color.expenditure, R.drawable.shape_gradient_expenditure)
        binding.lineChart.data = LineData(this)
        binding.lineChart.invalidate()
    }
}

internal fun ReportFragment.setIELineChartNodes(
    yearMonth: YearMonth,
    expenditures: List<BillTotal>,
    incomes: List<BillTotal>
) {
    val expenditureDataSet = lineChartConvertAdapter(expenditures, yearMonth).apply {
        lineDataSetStyle(this, R.color.expenditure, R.drawable.shape_gradient_expenditure)
    }
    val incomeDataSet = lineChartConvertAdapter(incomes, yearMonth).apply {
        lineDataSetStyle(this, R.color.income, R.drawable.shape_gradient_income)
    }
    binding.lineChart.data = LineData(expenditureDataSet, incomeDataSet)
    binding.lineChart.invalidate()
}

private fun ReportFragment.lineDataSetStyle(
    lineDataSet: LineDataSet,
    colorRes: Int,
    fillDrawableRes: Int
) {
    lineDataSet.color = resources.getColor(colorRes, mainActivity.theme)
    lineDataSet.mode = LineDataSet.Mode.LINEAR
    lineDataSet.circleRadius = 2f
    lineDataSet.setCircleColor(resources.getColor(colorRes, mainActivity.theme))
    //lineDataSet.setDrawCircles(false)//开启节点小圆点 false
    lineDataSet.valueTextSize = 8f
    var valuesTestColors = lineDataSet.values.map {
        return@map if (it.y > 0) resources.getColor(colorRes, mainActivity.theme)
        else resources.getColor(R.color.transparent, mainActivity.theme)
    }.toMutableList()
    lineDataSet.setValueTextColors(valuesTestColors)
    lineDataSet.setDrawFilled(true)
    lineDataSet.fillDrawable = resources.getDrawable(fillDrawableRes, mainActivity.theme)
}

/**
 * 饼图样式
 */
internal fun ReportFragment.pieChartStyle(pieChart: PieChart) {

    pieChart.setUsePercentValues(true)
    pieChart.description.isEnabled = false
    pieChart.setExtraOffsets(5f, 5f, 5f, 5f)

    pieChart.dragDecelerationFrictionCoef = 0.95f

    //chart.setCenterTextTypeface(tfLight)
    //chart.setCenterText(generateCenterSpannableText())

    pieChart.isDrawHoleEnabled = true
    pieChart.setHoleColor(Color.WHITE)

    pieChart.setTransparentCircleColor(Color.WHITE)
    pieChart.setTransparentCircleAlpha(110)
    pieChart.holeRadius = 50f
    pieChart.transparentCircleRadius = 55f
    pieChart.centerText = "收/支比例"
    pieChart.setDrawCenterText(true)

    pieChart.rotationAngle = 0f
    // enable rotation of the chart by touch
    // enable rotation of the chart by touch
    pieChart.isRotationEnabled = true
    pieChart.isHighlightPerTapEnabled = true

    // chart.setUnit(" €");
    // chart.setDrawUnitsInChart(true);

    // add a selection listener

    // chart.setUnit(" €");
    // chart.setDrawUnitsInChart(true);

    // add a selection listener
    pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
        override fun onValueSelected(e: Entry?, h: Highlight?) {
            val pieEntry = e as PieEntry
            pieChart.centerText = SpannableString("${pieEntry.label}\n${pieEntry.data}")

        }

        override fun onNothingSelected() {
        }
    })

    pieChart.animateY(1400, Easing.EaseInOutQuad)
    // entry label styling
    pieChart.setEntryLabelColor(Color.WHITE)
    //chart.setEntryLabelTypeface(tfRegular)
    pieChart.setEntryLabelTextSize(12f)
    pieChart.setUsePercentValues(true)
    pieChart.setDrawEntryLabels(true)
    pieChart.invalidate()
}

/**
 * 饼图数据
 */
internal fun ReportFragment.setPieChartData(entries: MutableList<PieEntry>) {

    // NOTE: The order of the entries when being added to the entries array determines their position around the center of
    // the chart.

    // NOTE: The order of the entries when being added to the entries array determines their position around the center of
    // the chart.
//
//        for (i in 0 until count) {
//            entries.add(PieEntry((Math.random() * range + range / 5).toFloat(),
//                    parties[i % parties.size]))
//        }

    val dataSet = PieDataSet(entries, " ")

    dataSet.setDrawIcons(false)

    dataSet.sliceSpace = 3f
    dataSet.iconsOffset = MPPointF(0f, 40f)
    dataSet.selectionShift = 5f

    dataSet.colors = colors
    //dataSet.setSelectionShift(0f);

    //dataSet.setSelectionShift(0f);
    val data = PieData(dataSet)
    data.setValueFormatter(PercentFormatter(binding.pieChartCategory))
    data.setValueTextSize(11f)
    data.setValueTextColor(Color.WHITE)
    //data.setValueTypeface(tfLight)
    binding.pieChartCategory.data = data

    //设置描述的位置
    dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
    dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
    dataSet.valueLinePart1Length = 0.4f//设置描述连接线长度
    dataSet.valueLinePart2Length = 0.4f//设置数据连接线长度
    dataSet.isUsingSliceColorAsValueLineColor = true
    dataSet.setValueTextColors(colors)
    // undo all highlights

    // undo all highlights
    binding.pieChartCategory.highlightValues(null)

    binding.pieChartCategory.invalidate()
}
