package com.hao.heji.ui.popup

import android.content.Context
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.tabs.TabLayout
import com.lxj.xpopup.core.CenterPopupView
import com.hao.heji.R
import com.hao.heji.databinding.PopLayoutYearMonthBinding
import java.util.Calendar

class YearSelectPopup(
    context: Context,
    private val onTabSelected: OnTabSelected,
    private val showAllYear: Boolean
) : CenterPopupView(context), View.OnClickListener {

    val years: List<Int>
    private var earliestYear = 2016
    private lateinit var binding: PopLayoutYearMonthBinding
    private var selectYear: Int = 0

    init {
        val calendar = Calendar.getInstance()
        years = (calendar.get(Calendar.YEAR) downTo earliestYear).toList()
        LogUtils.d(years)
    }

    override fun onCreate() {
        super.onCreate()
        binding = PopLayoutYearMonthBinding.bind(popupContentView.findViewById(R.id.pop_card_year_month))
        binding.y1.setOnClickListener(this)
        binding.y2.setOnClickListener(this)
        binding.y3.setOnClickListener(this)
        binding.y4.setOnClickListener(this)
        binding.y5.setOnClickListener(this)
        binding.y6.setOnClickListener(this)
        binding.y7.setOnClickListener(this)
        binding.y8.setOnClickListener(this)
        binding.y9.setOnClickListener(this)
        binding.y10.setOnClickListener(this)
        binding.y11.setOnClickListener(this)
        binding.y12.setOnClickListener(this)
        binding.btnYearAll.setOnClickListener {
            onTabSelected.selected(selectYear, 0)
            dismiss()
        }
        binding.btnYearAll.visibility = if (showAllYear) View.VISIBLE else View.GONE
        initYearsTab()
    }

    private fun initYearsTab() {
        years.forEach { s ->
            val yearTab = binding.tabYears.newTab()
            yearTab.setText(s.toString())
            binding.tabYears.addTab(yearTab)
        }
        binding.tabYears.getTabAt(0)?.select()
        selectYear = binding.tabYears.getTabAt(0)?.text.toString().toInt()
        binding.tabYears.tabMode = TabLayout.MODE_AUTO
        binding.tabYears.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                ToastUtils.showLong(tab.text)
                selectYear = tab.text.toString().toInt()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onClick(v: View) {
        onTabSelected.selected(selectYear, (v.tag as String).toInt())
        dismiss()
    }

    fun interface OnTabSelected {
        fun selected(year: Int, month: Int)
    }

    override fun getImplLayoutId(): Int = R.layout.pop_layout_year_month
}
