package com.hao.heji.ui.calendar

import android.content.Context
import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView.OnCalendarSelectListener
import com.hao.heji.R
import com.hao.heji.data.db.Bill
import com.hao.heji.databinding.FragmentCalendarNoteBinding
import com.hao.heji.today
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.adapter.DayBillsNode
import com.hao.heji.ui.adapter.NodeBillsAdapter
import com.hao.heji.ui.base.render
import com.hao.heji.ui.create.ArgAddBill
import com.hao.heji.ui.create.CreateBillFragmentArgs
import com.hao.heji.ui.popup.PopupBillInfo
import com.hao.heji.utils.YearMonth
import com.hao.heji.widget.CardDecoration

class CalendarNoteFragment : BaseFragment() {
    val binding: FragmentCalendarNoteBinding by lazy {
        FragmentCalendarNoteBinding.inflate(layoutInflater)
    }
    private val popupView by lazy {
        PopupBillInfo.create(
            activity = mainActivity,
            delete = { notifyCalendar() },
            update = {})
    }
    private val vm by koinViewModel<CalendarNoteViewModule>()
    lateinit var adapter: NodeBillsAdapter

    override fun layout(): View {
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        vm.selectYearMonth = mainActivity.viewModel.globalYearMonth
    }

    override fun setUpToolBar() {
        super.setUpToolBar()
        super.setUpToolBar()
        showBlack()

        val today = today()
        val selectYearMonth = vm.selectYearMonth

        with(selectYearMonth) {
            showYearMonthTitle(
                year = year, month = month,
                onTabSelected = { year, month ->
                    binding.calendarView.scrollToCalendar(year, month, 1)
                    this.year = year
                    this.month = month
                },
            )

            if (year != today.year || month != today.month) {
                binding.calendarView.scrollToCalendar(year, month, today.day)
            } else {
                notifyCalendar()
            }
        }
    }

    override fun initView(rootView: View) {
        initFab(rootView)
        initCalendarView()
        initAdapter()
        render(vm) {
            when (it) {
                is CalenderUiState.DayBills -> {
                    adapter.setNewInstance(it.data as MutableList<BaseNode>)
                    adapter.notifyDataSetChanged()
                }

                is CalenderUiState.Calender -> {
                    if (it.data.isEmpty()) {
                        binding.calendarView.clearSchemeDate()
                    } else {
                        binding.calendarView.setSchemeDate(it.data)//更新日历视图
                    }
                    notifyBillsList()
                }

                is CalenderUiState.Images -> popupView.setImages(it.data)
            }
        }
    }

    private fun initFab(view: View) {
        binding.addFab.setOnClickListener {
            val year = binding.calendarView.selectedCalendar.year
            val month = binding.calendarView.selectedCalendar.month
            val day = binding.calendarView.selectedCalendar.day
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month - 1, day)
            val args =
                CreateBillFragmentArgs(
                    ArgAddBill(
                        isModify = false,
                        bill = Bill(time = calendar.time)
                    )
                )//选择的日期
            Navigation.findNavController(view).navigate(R.id.nav_bill_add, args.toBundle())
        }
        binding.todayFab.setOnClickListener {
            binding.calendarView.scrollToCurrent()
            binding.todayFab.hide()
        }
        fabShow()
    }

    private fun initAdapter() {
        binding.recycler.layoutManager = LinearLayoutManager(mainActivity)
        adapter = NodeBillsAdapter()
        binding.recycler.adapter = adapter
        binding.recycler.addItemDecoration(CardDecoration(8))
        adapter.setOnItemClickListener { adapter, _, position ->
            if (adapter.getItem(position) is DayBillsNode) {
                val billNode = adapter.getItem(position) as DayBillsNode
                popupView.show(billNode.bill)
                if (billNode.bill.images.isNotEmpty()) {
                    vm.getImages(billNode.bill.id)
                }
            }
        }
    }

    private fun initCalendarView() {
        vm.selectYearMonth.apply {
            binding.calendarView.scrollToCalendar(year, month, day)
        }
        binding.calendarView.apply {
            setOnMonthChangeListener { year, month -> //月份更改侦听
                vm.selectYearMonth = YearMonth(year, month)
                centerTitle.text = "$year.$month"
                notifyCalendar()
                fabShow()
            }
            setOnCalendarSelectListener(object : OnCalendarSelectListener {
                override fun onCalendarOutOfRange(calendar: Calendar) {
                    LogUtils.d(calendar.toString())
                }

                override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {//选择日期事件
                    if (isClick) {
                        notifyBillsList()
                    }
                }
            })
        }
    }

    private fun fabShow() {
        val currentYearMonth = YearMonth(
            java.util.Calendar.getInstance()[java.util.Calendar.YEAR],
            java.util.Calendar.getInstance()[java.util.Calendar.MONTH] + 1
        )
        if (vm.selectYearMonth == currentYearMonth)
            binding.todayFab.hide()
        else
            binding.todayFab.show()
    }

    //更新日历天账单列表
    private fun notifyBillsList() {
        binding.calendarView.post {
            vm.getDayBills(binding.calendarView.selectedCalendar)
        }
    }

    //更新日历
    private fun notifyCalendar() {
        vm.updateYearMonth(
            vm.selectYearMonth.year,
            vm.selectYearMonth.month
        )
    }
}
