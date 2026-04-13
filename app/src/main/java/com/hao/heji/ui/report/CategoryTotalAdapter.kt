package com.hao.heji.ui.report

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.hao.heji.R
import com.hao.heji.databinding.ItemCategoryTotalBinding
import com.hao.heji.utils.ColorUtils
import com.hao.heji.utils.textdraw.TextDrawable


/**
 *@date: 2021/5/18
 *@author: 锅得铁
 *#
 */
internal class CategoryTotalAdapter(
    data: MutableList<PieEntry>?
) :
    BaseQuickAdapter<PieEntry, BaseViewHolder>(layoutResId = R.layout.item_category_total, data) {
    private val colors = ColorUtils.groupColors()
    private lateinit var itemBinding: ItemCategoryTotalBinding
    private val percentFormatter: PercentFormatter = PercentFormatter()
    override fun convert(holder: BaseViewHolder, item: PieEntry) {
        itemBinding = ItemCategoryTotalBinding.bind(holder.itemView)

        val position =if(holder.layoutPosition<colors.size) holder.layoutPosition else holder.layoutPosition/colors.size
        val textIconColor = colors[position]
        val textIconDrawable = TextDrawable.builder()
            .buildRound(item.label.substring(0, 1), textIconColor)

        itemBinding.imgCategory.setImageDrawable(textIconDrawable)
        itemBinding.tvCategory.text = item.label
        itemBinding.tvCategoryIncome.text = item.data.toString()
        itemBinding.tvCategoryPercentage.text = percentFormatter.getFormattedValue(item.value)
        val progress = item.value.toInt()
        itemBinding.progressBarCategory.progress = progress
    }
}