package com.hao.heji.ui.report

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.view.ViewStub
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.github.mikephil.charting.data.PieEntry
import com.hao.heji.R
import com.hao.heji.data.BillType
import com.hao.heji.data.db.dto.Income
import com.hao.heji.data.db.dto.IncomeTimeSurplus
import com.hao.heji.databinding.FragmentReportBinding
import com.hao.heji.databinding.LayoutEmptyBinding
import com.hao.heji.ui.base.render
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.popup.BillListPopup
import com.hao.heji.utils.ColorUtils
import com.hao.heji.utils.MyTimeUtils
import com.hao.heji.utils.YearMonth
import com.hao.heji.widget.DividerItemDecorator
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * 报告统计页面
 */
class ReportFragment : BaseFragment() {
    private val viewModel: ReportViewModel by koinViewModel()
    private val categoryTotalAdapter: CategoryTotalAdapter = CategoryTotalAdapter(mutableListOf())
    private val monthYearBillsAdapter: MonthYearBillAdapter = MonthYearBillAdapter(mutableListOf())
    private lateinit var emptyStubView: ViewStub
    internal val binding: FragmentReportBinding by lazy {
        FragmentReportBinding.inflate(
            layoutInflater
        )
    }
    private val billListPopup by lazy {
        val maxHeight = ScreenUtils.getScreenHeight() - toolBar.height
        BillListPopup.create(mainActivity, maxHeight) {
            viewModel.getImages(it.images)
        }
    }

    internal val colors = ColorUtils.groupColors()

    override fun onStart() {
        super.onStart()
        viewModel.selectTime(mainActivity.viewModel.globalYearMonth)
    }

    override fun layout() = binding.root

    override fun setUpToolBar() {
        super.setUpToolBar()
        showBlack()
        toolBar.title = "统计"
        showYearMonthTitle(year = viewModel.yearMonth.year,
            month = viewModel.yearMonth.month,
            showAllYear = true,
            onTabSelected = { year, month ->
                viewModel.selectTime(YearMonth(year, month))
            })
    }

    private fun setLinChart(type: Int, state: ReportUiState.LinChart) {
        if (type == BillType.EXPENDITURE.valueInt) {
            setExpenditureLineChartNodes(
                viewModel.yearMonth, state.data
            )
        }
        if (type == BillType.INCOME.valueInt) {
            setIncomeLineChartNodes(
                viewModel.yearMonth, state.data
            )
        }
        if (type == BillType.ALL.valueInt) {
            val arrays = state.all
            setIELineChartNodes(
                viewModel.yearMonth, expenditures = arrays[0], incomes = arrays[1]
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        render(viewModel) { state ->
            when (state) {
                is ReportUiState.Total -> {
                    incomeExpenditureInfo(state.data)
                }
                is ReportUiState.Images -> {
                    billListPopup.setImages(state.data)
                }
                is ReportUiState.LinChart -> {
                    val type = state.type
                    setLinChart(type, state)
                }
                is ReportUiState.ProportionChart -> {
                    setPieChartData(state.data)
                    categoryTotalAdapter.setList(state.data)
                }
                is ReportUiState.CategoryList -> {
                    billListPopup.show(state.category, state.data)
                }
                is ReportUiState.ReportList -> {
                    monthYearBillsAdapter.setList(state.data)
                }
                is ReportUiState.ReportBillInfoList -> {
                    billListPopup.show(state.time, state.data)
                }
            }
        }
    }

    override fun initView(rootView: View) {
        emptyStubView = rootView.findViewById(R.id.emptyStub)

        lineChartStyle(binding.lineChart)
        val yearMonth = viewModel.yearMonth
        binding.tvTypeExpenditure.setOnClickListener {
            viewModel.getLinChartData(BillType.EXPENDITURE.valueInt)
            lineChartSelectType(BillType.EXPENDITURE)
        }
        binding.tvTypeIncome.setOnClickListener {
            viewModel.getLinChartData(BillType.INCOME.valueInt)
            lineChartSelectType(BillType.INCOME)
        }
        binding.tvTypeAll.setOnClickListener {
            viewModel.getLinChartData(BillType.ALL.valueInt)
            lineChartSelectType(BillType.ALL)
        }

        pieChartStyle(binding.pieChartCategory)

        binding.tvTypeExpenditurePie.setOnClickListener {
            viewModel.getProportionChart(BillType.EXPENDITURE.valueInt)
            pieChartSelectType(BillType.EXPENDITURE)
        }
        binding.tvTypeIncomePie.setOnClickListener {
            viewModel.getProportionChart(BillType.INCOME.valueInt)
            pieChartSelectType(BillType.INCOME)
        }

        initCategoryListView()
        initTotalTitleView()
        initTotalListView()

        emptyStubView.setOnInflateListener { stub, inflated ->
            val emptyLayoutBinding = LayoutEmptyBinding.bind(inflated)
            emptyLayoutBinding.tvContext.text = "没有更多账单数据"
            emptyLayoutBinding.tvContext.setOnClickListener {
                ToastUtils.showShort("重试加载失败")
            }
            LogUtils.d("empty view inflated")
        }

    }

    /**
     * 收支总览
     */
    private fun incomeExpenditureInfo(money: Income) {
        val balance = money.income.minus(money.expenditure)//结余
        //月份天数
        val dayCount = MyTimeUtils.lastDayOfMonth(
            viewModel.yearMonth.year, viewModel.yearMonth.month
        ).split("-")[2].toInt()
        binding.apply {
            tvIncomeValue.text = money.income.toString()
            tvExpenditureValue.text = money.expenditure.toString()
            tvJieYuValue.text = balance.toPlainString()
            tvDayAVGValue.text =
                balance.divide(BigDecimal(dayCount), 2, RoundingMode.DOWN).toPlainString()
        }
        showEmptyView()

        //----列表标题年/月平均值
        var avg = if (viewModel.yearMonth.isYear()) {
            var month12 = BigDecimal(12)
            "月均支出：${
                money.expenditure.divide(
                    month12, 2, RoundingMode.DOWN
                )
            }  收入：${money.expenditure.div(month12)}"
        } else {
            val monthDayCount = BigDecimal(
                MyTimeUtils.getMonthLastDay(
                    viewModel.yearMonth.year, viewModel.yearMonth.month
                )
            )
            "日均支出：${
                money.expenditure.divide(
                    monthDayCount, 2, RoundingMode.DOWN
                )
            }  收入：${
                money.income.div(
                    monthDayCount
                )
            }"
        }
        binding.tvYearMonthAVG.text = SpannableString.valueOf(avg)
    }

    /**
     * 分类报表 列表视图
     */
    private fun initCategoryListView() {
        binding.recyclerCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategory.adapter = categoryTotalAdapter
        categoryTotalAdapter.setOnItemClickListener { adapter, view, position ->
            val categoryItem: PieEntry = adapter.getItem(position) as PieEntry
            val billType: Int = with(categoryItem) {
                val money = data as BigDecimal
                money.signum()//返回 -1 | 0 | 1  与BillType一致
            }
            viewModel.getCategoryBills(categoryItem.label, billType)
        }
    }

    /**
     * 年|月 报表视图
     */
    private fun initTotalListView() {
        //basic
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.isSmoothScrollbarEnabled = true
        binding.recyclerBaobiao.apply {
            layoutManager = linearLayoutManager
            adapter = monthYearBillsAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            addItemDecoration(
                DividerItemDecorator(
                    resources.getDrawable(
                        R.drawable.inset_recyclerview_divider, mainActivity.theme
                    )
                )
            )
        }

        //listener
        monthYearBillsAdapter.setOnItemClickListener { adapter, view, position ->
            val itemEntity: IncomeTimeSurplus = adapter.getItem(position) as IncomeTimeSurplus
            val ymd = viewModel.yearMonth
            itemEntity.time?.let {
                //时间可以是月份和天
                if (it.contains("月")) {//根据全年的情况
                    ymd.month = it.split("月")[0].toInt()
                } else {
                    val arrays = it.split("-")
                    ymd.month = arrays[0].toInt()
                    ymd.day = arrays[1].toInt()
                }
            }
            viewModel.getReportBillInfoList(ymd.yearMonthString())
        }

    }

    /**
     * 报表标题【日期】-【收入】-【支出】-【结余】
     */
    private fun initTotalTitleView() {
        val year = viewModel.yearMonth.isYear()

        binding.layoutTotalList.apply {
            if (year) {
                tvDate.text = "月份"
            }
            val textColor = resources.getColor(R.color.textRemark, mainActivity.theme)
            tvSurplus.setTextColor(textColor)
            tvIncome.setTextColor(textColor)
            tvExpenditure.setTextColor(textColor)
            tvDate.setTextColor(textColor)
        }
    }

    /**
     * 没有数据时显示空视图
     */
    private fun showEmptyView() {
        if (binding.tvDayAVGValue.text.equals("0.00")) {
            emptyStubView.visibility = View.VISIBLE
            binding.nestedSccrollView.visibility = View.GONE
        } else {
            emptyStubView.visibility = View.GONE
            binding.nestedSccrollView.visibility = View.VISIBLE
        }
    }

    /**
     * 折线图类型
     *
     * @param  type 收入|支出|全部
     */
    private fun lineChartSelectType(type: BillType) {
        binding.tvTypeExpenditure.apply {
            setBackgroundColor(resources.getColor(R.color.transparent, mainActivity.theme))
            setTextColor(resources.getColor(R.color.textRemark, mainActivity.theme))
        }

        binding.tvTypeIncome.apply {
            setBackgroundColor(resources.getColor(R.color.transparent, mainActivity.theme))
            setTextColor(resources.getColor(R.color.textRemark, mainActivity.theme))
        }


        binding.tvTypeAll.apply {
            setBackgroundColor(resources.getColor(R.color.transparent, mainActivity.theme))
            setTextColor(resources.getColor(R.color.textRemark, mainActivity.theme))
        }

        when (type) {
            BillType.EXPENDITURE -> {
                binding.tvTypeExpenditure.apply {
                    background =
                        resources.getDrawable(R.drawable.shape_tag_left_blue, mainActivity.theme)
                    setTextColor(resources.getColor(R.color.white, mainActivity.theme))
                }
            }
            BillType.INCOME -> {
                binding.tvTypeIncome.apply {
                    setBackgroundColor(resources.getColor(R.color.colorPrimary, mainActivity.theme))
                    setTextColor(resources.getColor(R.color.white, mainActivity.theme))
                }
            }
            BillType.ALL -> {
                binding.tvTypeAll.apply {
                    background =
                        resources.getDrawable(R.drawable.shape_tag_right_blue, mainActivity.theme)
                    setTextColor(resources.getColor(R.color.white, mainActivity.theme))
                }
            }
        }
    }

    private fun pieChartSelectType(type: BillType) {
        binding.tvTypeExpenditurePie.apply {
            setBackgroundColor(resources.getColor(R.color.transparent, mainActivity.theme))
            setTextColor(resources.getColor(R.color.textRemark, mainActivity.theme))
        }

        binding.tvTypeIncomePie.apply {
            setBackgroundColor(resources.getColor(R.color.transparent, mainActivity.theme))
            setTextColor(resources.getColor(R.color.textRemark, mainActivity.theme))
        }


        when (type) {
            BillType.EXPENDITURE -> {
                binding.tvTypeExpenditurePie.apply {
                    background =
                        resources.getDrawable(R.drawable.shape_tag_left_blue, mainActivity.theme)
                    setTextColor(resources.getColor(R.color.white, mainActivity.theme))
                }
            }
            BillType.INCOME -> {
                binding.tvTypeIncomePie.apply {
                    background =
                        resources.getDrawable(R.drawable.shape_tag_right_blue, mainActivity.theme)
                    setTextColor(resources.getColor(R.color.white, mainActivity.theme))
                }
            }
            else -> {}
        }
    }
}