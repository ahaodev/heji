package com.hao.heji.ui.category.manager

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.R
import com.hao.heji.data.db.Category
import com.hao.heji.databinding.ItemCategoryManagerBinding
import com.hao.heji.utils.textdraw.TextDrawable

/**
 * @date: 2020/9/16
 * @author: 锅得铁
 * #分类适配器
 */
open class CategoryManagerAdapter :
    BaseQuickAdapter<Category, BaseViewHolder>(R.layout.item_category_manager) {

    override fun convert(holder: BaseViewHolder, item: Category) {
        if (item.name.isEmpty()) return

        val itemBinding = ItemCategoryManagerBinding.bind(holder.itemView)
        val bgColor = context.getColor(
            if (item.isSelected) R.color.category_ico_selected else R.color.category_ico
        )
        val drawable = TextDrawable.builder().buildRound(item.name[0].toString(), bgColor)
        itemBinding.roundImageView.setImageDrawable(drawable)
        itemBinding.tvName.text = item.name
        addChildClickViewIds(itemBinding.btnDelete.id)
        val isOther = item.name == "其他"
        itemBinding.btnEdit.visibility = if (isOther) View.INVISIBLE else View.VISIBLE
        itemBinding.btnDelete.visibility = if (isOther) View.INVISIBLE else View.VISIBLE
    }

    protected fun getItemBinding(holder: BaseViewHolder): ItemCategoryManagerBinding {
        return ItemCategoryManagerBinding.bind(holder.itemView)
    }
}