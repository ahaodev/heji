package com.hao.heji.ui.report

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.R
import com.hao.heji.data.db.dto.IncomeTimeSurplus
import com.hao.heji.databinding.ItemMonthYearBillBinding
import java.math.BigDecimal

/**
 * 月|年 报表 adapter
 *  @date: 2021/5/18
 *  @author: 锅得铁
 *#
 */
class MonthYearBillAdapter(data: MutableList<IncomeTimeSurplus>?) :
    BaseQuickAdapter<IncomeTimeSurplus, BaseViewHolder>(
        layoutResId = R.layout.item_month_year_bill,
        data
    ) {
    lateinit var itemBinding: ItemMonthYearBillBinding
    override fun convert(holder: BaseViewHolder, item: IncomeTimeSurplus) {
        itemBinding = ItemMonthYearBillBinding.bind(holder.itemView)
        itemBinding.tvDate.text = item.time
        itemBinding.tvIncome.text = zeroPadding(item.income.toString())
        itemBinding.tvExpenditure.text = zeroPadding(item.expenditure.toString())
        itemBinding.tvSurplus.text = zeroPadding(item.surplus.toString())
        if (item.surplus.compareTo(BigDecimal.ZERO) == -1) {//surplus<zero
            itemBinding.tvSurplus.setTextColor(ContextCompat.getColor(context, R.color.expenditure))
        } else {
            itemBinding.tvSurplus.setTextColor(ContextCompat.getColor(context, R.color.income))
        }
    }

    private fun zeroPadding(value: String): String {
        return if (!value.contains(".")) "${value}.00" else value
    }
}
