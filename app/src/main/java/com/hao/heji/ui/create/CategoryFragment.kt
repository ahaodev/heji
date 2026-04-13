package com.hao.heji.ui.create

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.hao.heji.data.BillType
import com.hao.heji.data.db.Category
import com.hao.heji.databinding.FragmentCategoryContentBinding
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.create.adapter.CategoryWithSubAdapter

/**
 * @date: 2020/10/11
 * @author: 锅得铁
 * # 收入/支出标签 复用该Fragment
 */
internal class CategoryFragment : BaseFragment() {
    val binding: FragmentCategoryContentBinding by lazy {
        FragmentCategoryContentBinding.inflate(layoutInflater)
    }
    private val categoryAdapter by lazy {
        CategoryWithSubAdapter().apply {
            onParentClick = { category ->
                createBillFragment?.selectedCategory(type.value, category)
                createBillFragment?.viewModel?.getChildCategories(type.value, category.id)
            }
            onSubCategoryClick = { subCategory ->
                createBillFragment?.selectedCategory(type.value, subCategory)
            }
            setOnItemClickListener { _, _, position ->
                val item = getItem(position)
                if (item is CategoryWithSubAdapter.CategoryItem.ParentItem) {
                    selectParent(item.category)
                }
            }
        }
    }

    private val createBillFragment: CreateBillFragment?
        get() = parentFragment as? CreateBillFragment

    private var selectCategory: Category? = null
    private var pendingCategoryName: String? = null

    lateinit var type: BillType

    override fun layout() = binding.root

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            type = CategoryFragmentArgs.fromBundle(it).type
        }
    }

    override fun onResume() {
        super.onResume()
        createBillFragment?.let { parent ->
            parent.viewModel.getCategories(type.value)
            parent.selectedCategory(type.value, selectCategory)
        }
    }

    override fun initView(view: View) {
        val gridLayoutManager = GridLayoutManager(mainActivity, CategoryWithSubAdapter.SPAN_COUNT)
        binding.categoryRecycler.apply {
            layoutManager = gridLayoutManager
            adapter = categoryAdapter
        }
        categoryAdapter.setupSpanSizeLookup(gridLayoutManager)
    }

    fun setCategories(categories: MutableList<Category>) {
        categoryAdapter.setParentCategories(categories)
        val pending = pendingCategoryName
        if (pending != null) {
            categoryAdapter.setSelectCategoryByName(pending)
            pendingCategoryName = null
        } else {
            defSelected()
        }
    }

    fun setSubCategories(children: MutableList<Category>) {
        categoryAdapter.showSubCategories(children)
    }

    fun setSelectCategory(category: String? = null) {
        if (category.isNullOrEmpty()) return
        pendingCategoryName = category
        if (isAdded) {
            binding.root.post {
                categoryAdapter.setSelectCategoryByName(category)
            }
        }
    }

    private fun defSelected() {
        if (selectCategory != null) return
        val parents = categoryAdapter.data.filterIsInstance<CategoryWithSubAdapter.CategoryItem.ParentItem>()
        if (parents.isNotEmpty()) {
            val count = parents.count { it.category.isSelected }
            if (count <= 0) {
                val first = parents.first().category
                selectCategory = first
                categoryAdapter.selectParent(first)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type: BillType): CategoryFragment {
            val categoryFragment = CategoryFragment()
            categoryFragment.arguments = CategoryFragmentArgs(type).toBundle()
            return categoryFragment
        }
    }
}
