package com.hao.heji.ui.create.adapter

import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.R
import com.hao.heji.data.db.Category
import com.hao.heji.databinding.ItemCategoryBinding
import com.hao.heji.utils.textdraw.ColorGenerator
import com.hao.heji.utils.textdraw.TextDrawable

/**
 * @date: 2020/9/16
 * @author: 锅得铁
 * #分类适配器
 */
internal class SelectCategoryAdapter(data: MutableList<Category>) :
    BaseQuickAdapter<Category, BaseViewHolder>(R.layout.item_category, data) {
    private lateinit var itemBinding: ItemCategoryBinding
    override fun convert(holder: BaseViewHolder, item: Category) {
        itemBinding = ItemCategoryBinding.bind(holder.itemView)
        val bgColor =
            context.getColor(if (item.isSelected) R.color.category_ico_selected else R.color.category_ico)
//        val bgColor =if (!label.selected) ColorUtils.groupColors()[holder.adapterPosition] else context.getColor( R.color.category_ico)
        if (TextUtils.isEmpty(item.name)) return
        val drawable = TextDrawable.builder().buildRound(item.name.substring(0, 1), bgColor)
        itemBinding.roundImageView.setImageDrawable(drawable)
        itemBinding.tvLabel.text = item.name
        if (item.name == SETTING) {
            itemBinding.roundImageView.setImageDrawable(
                TextDrawable.builder().buildRound(
                    SETTING,
                    ColorGenerator.MATERIAL.getRandomColor()
                )
            )
            itemBinding.tvLabel.text = SETTING
            itemBinding.roundImageView.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_settings_24))
        }
    }

    fun setSelectCategory(any: Any) {
        data.filter {
            if (it.isSelected) {
                it.isSelected = false
            }
            if (any is String) {
                if (it.name == any) {
                    it.isSelected=true
                }
            } else if (any is Category) {
                if (it == any) {
                    it.isSelected=true
                }
            }

            return@filter true
        }.toMutableList()
        notifyDataSetChanged()
    }

    companion object {
        const val SETTING = "管理"
    }
}