package com.hao.heji.ui.create.adapter

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.R
import com.hao.heji.data.db.Category
import com.hao.heji.databinding.ItemCategoryBinding
import com.hao.heji.utils.textdraw.TextDrawable

/**
 * 分类适配器 - 支持在选中行下方展开子分类
 */
internal class CategoryWithSubAdapter :
    BaseQuickAdapter<CategoryWithSubAdapter.CategoryItem, BaseViewHolder>(0) {

    companion object {
        const val TYPE_CATEGORY = 0
        const val TYPE_SUB_ROW = 1
        const val SPAN_COUNT = 5
    }

    sealed class CategoryItem {
        data class ParentItem(val category: Category) : CategoryItem()
        data class SubRow(val children: MutableList<Category>) : CategoryItem()
    }

    private var parentCategories = mutableListOf<Category>()
    private var currentSubChildren = mutableListOf<Category>()
    private var selectedParentId: String? = null
    private var subRowInsertIndex = -1

    var onParentClick: ((Category) -> Unit)? = null
    var onSubCategoryClick: ((Category) -> Unit)? = null

    override fun getDefItemViewType(position: Int): Int {
        return when (data[position]) {
            is CategoryItem.ParentItem -> TYPE_CATEGORY
            is CategoryItem.SubRow -> TYPE_SUB_ROW
        }
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_SUB_ROW -> createBaseViewHolder(parent, R.layout.item_sub_category_row)
            else -> createBaseViewHolder(parent, R.layout.item_category)
        }
    }

    override fun convert(holder: BaseViewHolder, item: CategoryItem) {
        when (item) {
            is CategoryItem.ParentItem -> bindParent(holder, item.category)
            is CategoryItem.SubRow -> bindSubRow(holder, item.children)
        }
    }

    private fun bindParent(holder: BaseViewHolder, item: Category) {
        val itemBinding = ItemCategoryBinding.bind(holder.itemView)
        val bgColor = context.getColor(
            if (item.isSelected) R.color.category_ico_selected else R.color.category_ico
        )
        if (TextUtils.isEmpty(item.name)) return
        val drawable = TextDrawable.builder().buildRound(item.name.substring(0, 1), bgColor)
        itemBinding.roundImageView.setImageDrawable(drawable)
        itemBinding.tvLabel.text = item.name

        if (item.name == SelectCategoryAdapter.SETTING) {
            itemBinding.roundImageView.setImageDrawable(
                context.getDrawable(R.drawable.ic_baseline_settings_24)
            )
            itemBinding.tvLabel.text = SelectCategoryAdapter.SETTING
        }
    }

    private fun bindSubRow(holder: BaseViewHolder, children: MutableList<Category>) {
        val recyclerView = holder.getView<RecyclerView>(R.id.subCategoryGrid)
        if (recyclerView.adapter == null) {
            recyclerView.layoutManager = GridLayoutManager(context, 4)
            val subAdapter = SelectCategoryAdapter(ArrayList())
            subAdapter.setOnItemClickListener { _: BaseQuickAdapter<*, *>?, _: View?, position: Int ->
                val sub = subAdapter.getItem(position)
                subAdapter.data.forEach { it.isSelected = it.name == sub.name }
                subAdapter.notifyDataSetChanged()
                onSubCategoryClick?.invoke(sub)
            }
            recyclerView.adapter = subAdapter
        }
        (recyclerView.adapter as SelectCategoryAdapter).setNewInstance(children)
    }

    fun setParentCategories(categories: MutableList<Category>) {
        parentCategories = categories
        selectedParentId = null
        subRowInsertIndex = -1
        currentSubChildren.clear()
        rebuildData()
    }

    fun selectParent(category: Category) {
        selectedParentId = category.id
        parentCategories.forEach { it.isSelected = it.id == category.id }
        // 子分类先清空，等外部查询后调用 showSubCategories
        currentSubChildren.clear()
        rebuildData()
        onParentClick?.invoke(category)
    }

    fun showSubCategories(children: MutableList<Category>) {
        currentSubChildren = children
        rebuildData()
    }

    fun getSelectedParent(): Category? {
        return parentCategories.find { it.id == selectedParentId }
    }

    private fun rebuildData() {
        val items = mutableListOf<CategoryItem>()
        val selectedIndex = parentCategories.indexOfFirst { it.id == selectedParentId }

        // 计算选中项所在行的最后一个位置
        val endOfRow = if (selectedIndex >= 0) {
            ((selectedIndex / SPAN_COUNT) + 1) * SPAN_COUNT
        } else -1

        for (i in parentCategories.indices) {
            items.add(CategoryItem.ParentItem(parentCategories[i]))
            // 在选中行末尾插入子分类行
            if (i + 1 == endOfRow.coerceAtMost(parentCategories.size) &&
                currentSubChildren.isNotEmpty()
            ) {
                items.add(CategoryItem.SubRow(currentSubChildren))
            }
        }
        // 如果选中的是最后一行且还没插入
        if (selectedIndex >= 0 && currentSubChildren.isNotEmpty() &&
            items.none { it is CategoryItem.SubRow }
        ) {
            items.add(CategoryItem.SubRow(currentSubChildren))
        }

        setNewInstance(items)
    }

    fun setupSpanSizeLookup(layoutManager: GridLayoutManager) {
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < data.size && data[position] is CategoryItem.SubRow) {
                    SPAN_COUNT
                } else 1
            }
        }
    }

    fun setSelectCategoryByName(name: String) {
        val found = parentCategories.find { it.name == name }
        if (found != null) {
            selectParent(found)
        }
    }
}
