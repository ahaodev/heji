package com.hao.heji.ui.category.manager

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.R
import com.hao.heji.data.db.Category
import com.hao.heji.utils.textdraw.TextDrawable

/**
 * @date: 2020/9/16
 * @author: 锅得铁
 * #分类管理适配器 - 支持展开/折叠子分类
 */
open class CategoryManagerAdapter :
    BaseQuickAdapter<Category, BaseViewHolder>(R.layout.item_category_manager) {

    private val expandedSet = mutableSetOf<String>()
    private val childrenMap = mutableMapOf<String, MutableList<Category>>()

    var onMoreClick: ((Category) -> Unit)? = null
    var onExpandClick: ((Category, Boolean) -> Unit)? = null
    var onAddChildClick: ((Category) -> Unit)? = null
    var onItemClick: ((Category) -> Unit)? = null

    override fun convert(holder: BaseViewHolder, item: Category) {
        if (item.name.isEmpty()) return

        val imgIcon = holder.getView<ImageView>(R.id.roundImageView)
        val tvName = holder.getView<TextView>(R.id.tvName)
        val tvDefault = holder.getView<TextView>(R.id.tvDefault)
        val btnMore = holder.getView<ImageButton>(R.id.btnMore)
        val btnExpand = holder.getView<ImageButton>(R.id.btnExpand)
        val layoutChildren = holder.getView<LinearLayout>(R.id.layoutChildren)
        val btnAddChild = holder.getView<LinearLayout>(R.id.btnAddChild)
        val layoutParent = holder.getView<View>(R.id.layoutParent)

        val bgColor = context.getColor(R.color.category_ico)
        val drawable = TextDrawable.builder().buildRound(item.name[0].toString(), bgColor)
        imgIcon.setImageDrawable(drawable)
        tvName.text = item.name

        val isDefault = item.name == "其他" || item.name == "其它"
        tvDefault.visibility = if (isDefault) View.VISIBLE else View.GONE

        val isExpanded = expandedSet.contains(item.id)
        btnExpand.setImageResource(
            if (isExpanded) R.drawable.ic_expand_more_24 else R.drawable.ic_chevron_right_24
        )
        layoutChildren.visibility = if (isExpanded) View.VISIBLE else View.GONE

        // 渲染子分类
        if (isExpanded) {
            renderChildren(layoutChildren, btnAddChild, item)
        }

        btnMore.setOnClickListener { onMoreClick?.invoke(item) }
        btnExpand.setOnClickListener {
            toggleExpand(item.id)
            notifyItemChanged(holder.adapterPosition)
            onExpandClick?.invoke(item, expandedSet.contains(item.id))
        }
        layoutParent.setOnClickListener { onItemClick?.invoke(item) }
        btnAddChild.setOnClickListener { onAddChildClick?.invoke(item) }
    }

    private fun renderChildren(
        layoutChildren: LinearLayout,
        btnAddChild: LinearLayout,
        parentCategory: Category
    ) {
        // 移除除了btnAddChild之外的子视图
        val childCount = layoutChildren.childCount
        for (i in childCount - 1 downTo 0) {
            val child = layoutChildren.getChildAt(i)
            if (child.id != R.id.btnAddChild) {
                layoutChildren.removeViewAt(i)
            }
        }

        val children = childrenMap[parentCategory.id]
        children?.forEach { childCategory ->
            val childView = createChildView(childCategory)
            layoutChildren.addView(childView, layoutChildren.childCount - 1)
        }
    }

    private fun createChildView(category: Category): View {
        val view = View.inflate(context, R.layout.item_category_child, null)
        val imgIcon = view.findViewById<ImageView>(R.id.childIcon)
        val tvName = view.findViewById<TextView>(R.id.childName)
        val btnMore = view.findViewById<ImageButton>(R.id.childBtnMore)

        val bgColor = context.getColor(R.color.category_ico)
        val drawable = TextDrawable.builder().buildRound(category.name[0].toString(), bgColor)
        imgIcon.setImageDrawable(drawable)
        tvName.text = category.name

        btnMore.setOnClickListener { onMoreClick?.invoke(category) }
        view.setOnClickListener { onItemClick?.invoke(category) }
        return view
    }

    fun toggleExpand(categoryId: String) {
        if (expandedSet.contains(categoryId)) {
            expandedSet.remove(categoryId)
        } else {
            expandedSet.add(categoryId)
        }
    }

    fun isExpanded(categoryId: String) = expandedSet.contains(categoryId)

    fun updateChildren(parentId: String, children: MutableList<Category>) {
        childrenMap[parentId] = children
        val index = data.indexOfFirst { it.id == parentId }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }
}