package com.hao.heji.ui.home

import android.os.Bundle
import android.view.View
import android.view.ViewStub
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.hao.heji.R
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.dto.Income
import com.hao.heji.databinding.FragmentBillsHomeBinding
import com.hao.heji.databinding.LayoutBillsTopBinding
import com.hao.heji.ui.base.render
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.base.hideRefreshing
import com.hao.heji.ui.base.swipeRefreshLayout
import com.hao.heji.ui.adapter.DayBillsNode
import com.hao.heji.ui.adapter.DayIncomeNode
import com.hao.heji.ui.adapter.NodeBillsAdapter
import com.hao.heji.ui.create.ArgAddBill
import com.hao.heji.ui.create.CreateBillFragmentArgs
import com.hao.heji.ui.popup.PopupBillInfo
import com.hao.heji.utils.ClickUtils
import com.hao.heji.utils.YearMonth
import com.hao.heji.widget.CardDecoration
import java.util.*

/**
 * 账单列表
 *
 */
class BillListFragment : BaseFragment() {

    private lateinit var subTotalLayoutBinding: LayoutBillsTopBinding
    private lateinit var stubTotalView: ViewStub

    private val homeViewModel: BillListViewModel by activityViewModel()
    private lateinit var adapter: NodeBillsAdapter
    private val binding: FragmentBillsHomeBinding by lazy {
        FragmentBillsHomeBinding.inflate(layoutInflater).apply {
            viewClick()
        }
    }

    private fun FragmentBillsHomeBinding.viewClick() {
        fab.setOnClickListener {
            val bill = Bill(time = Date())
            val bundle = CreateBillFragmentArgs(
                ArgAddBill(false, bill)
            ).toBundle()
            findNavController().navigate(
                R.id.nav_bill_add, bundle
            )
        }
    }

    private val popupView by lazy {
        PopupBillInfo.create(activity = mainActivity, delete = {
            ToastUtils.showLong("Delete OK")
        }, update = {})
    }


    override fun initView(rootView: View) {
        stubTotalView = rootView.findViewById(R.id.total)
        adapter = NodeBillsAdapter()
        initBillsAdapter()
        stubTotalView.setOnInflateListener { _, inflated ->   //提前设置避免多次设置
            subTotalLayoutBinding = LayoutBillsTopBinding.bind(inflated)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val yearMonthString = mainActivity.viewModel.globalYearMonth.yearMonthString()
        homeViewModel.getMonthBills(yearMonthString)
        homeViewModel.getSummary(yearMonthString)
        render(homeViewModel) {
            when (it) {
                is BillListUiState.Bills -> {
                    if (it.nodeList.isEmpty()) {
                        adapter.setDiffNewData(mutableListOf())//设置DiffCallback使用setDiffNewData避免setList
                        binding.homeRecycler.minimumHeight = getRootViewHeight()//占满一屏
                        stubTotalView.visibility = View.GONE
                        adapter.setEmptyView(R.layout.layout_empty)
                    } else {
                        stubTotalView.visibility = View.VISIBLE
                        adapter.setDiffNewData(it.nodeList)
                    }
                    //adapter.loadMoreModule.loadMoreEnd()//单月不分页，直接显示没有跟多
                    hideRefreshing(binding.refreshLayout)
                }

                is BillListUiState.Summary -> {
                    totalIncomeExpense(it.income)
                }

                is BillListUiState.Error -> {
                    ToastUtils.showLong(it.t.message)
                }

                is BillListUiState.Images -> {
                    popupView.setImages(it.data)
                }
            }
        }
    }

    override fun setUpToolBar() {
        super.setUpToolBar()
        toolBar.title = ""
        showYearMonthTitle(
            year = mainActivity.viewModel.globalYearMonth.year, //默认为当前时间,
            month = mainActivity.viewModel.globalYearMonth.month,//默认为当前月份
            onTabSelected = { year, month ->
                notifyData(year, month)
                mainActivity.viewModel.globalYearMonth = YearMonth(year, month)
            }
        )
        toolBar.navigationIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_baseline_dehaze_24,
            mainActivity.theme
        )
        toolBar.setNavigationOnClickListener {
            //展开侧滑菜单
            mainActivity.openDrawer()
        }
        binding.imgCalendar.setOnClickListener {
            findNavController().navigate(R.id.nav_calendar_note)
        }
        binding.imgTotal.setOnClickListener {
            findNavController().navigate(R.id.nav_report)
        }
        swipeRefreshLayout(binding.refreshLayout) {
            notifyData()
        }
        binding.materialupAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            val isFullyShow = verticalOffset >= 0
            binding.refreshLayout.isEnabled = isFullyShow
        }


    }

    override fun layout() = binding.root

    /**
     * 汇总收支
     *
     * @param year  年
     * @param month 月
     */
    private fun totalIncomeExpense(incomeExpense: Income) {
        val noData = incomeExpense.income.signum() == 0 && incomeExpense.expenditure.signum() == 0
        if (noData) {
            stubTotalView.visibility = View.GONE
            return
        }
        stubTotalView.visibility = View.VISIBLE
        notifyTotalLayout(incomeExpense)

    }

    private fun notifyTotalLayout(incomeExpense: Income) {
        if (this::subTotalLayoutBinding.isInitialized) {
            with(subTotalLayoutBinding) {
                val totalRevenue = incomeExpense.income.subtract(incomeExpense.expenditure)//总揽
                tvExpenditure.text = incomeExpense.expenditure.toString()
                tvIncome.text = incomeExpense.income.toString()
                val textColor =
                    if (totalRevenue.toLong() > 0) R.color.income else R.color.expenditure
                tvSurplus.setTextColor(ContextCompat.getColor(tvSurplus.context, textColor))
                tvSurplus.text = totalRevenue.toPlainString()
            }

        }
    }

    /**
     * 初始化账单列表适配器
     */
    private fun initBillsAdapter() {
        adapter.recyclerView = binding.homeRecycler
        binding.homeRecycler.layoutManager = LinearLayoutManager(mainActivity)
        binding.homeRecycler.adapter = adapter
        binding.homeRecycler.addItemDecoration(CardDecoration(8))
        adapter.setDiffCallback(object : ItemCallback<BaseNode>() {
            override fun areItemsTheSame(oldItem: BaseNode, newItem: BaseNode): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: BaseNode, newItem: BaseNode): Boolean {
                return newItem == oldItem
            }
        })
        adapter.setOnItemClickListener { adapter: BaseQuickAdapter<*, *>, _: View?, position: Int ->
            ClickUtils.debouncing {
                if (adapter.getItem(position) is DayIncomeNode) { //日视图
                    val dayIncomeNode = adapter.getItem(position) as DayIncomeNode
                    val dayIncome = dayIncomeNode.dayIncome
                    val calendar = Calendar.getInstance()
                    calendar[dayIncome.year, dayIncome.month - 1] = dayIncome.monthDay

                    val args =
                        CreateBillFragmentArgs(
                            ArgAddBill(
                                false,
                                Bill(time = calendar.time)
                            )
                        )//选择的日期
                    findNavController()
                        .navigate(R.id.nav_bill_add, args.toBundle())
                } else { //日详细列表ITEM
                    val dayBills = adapter.getItem(position) as DayBillsNode
                    val bill = dayBills.bill
                    popupView.show(bill)
                    if (bill.images.isNotEmpty()) {
                        homeViewModel.getImages(bill.id)
                    }
                }

            }
        }
        binding.homeRecycler.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            if (scrollY > oldScrollY) { //向下滑动隐藏
                binding.fab.hide()
            } else {//向上滑动显示
                binding.fab.show()
            }
        }

    }

    /**
     * 刷新列表数据、收支情况数据
     * @param year  年
     * @param month 月
     */
    private fun notifyData(
        year: Int = mainActivity.viewModel.globalYearMonth.year,
        month: Int = mainActivity.viewModel.globalYearMonth.month
    ) {
        val yearMonth = YearMonth(year, month)
        val ymStr = yearMonth.yearMonthString()
        homeViewModel.getMonthBills(ymStr)
        homeViewModel.getSummary(ymStr)
    }
}


