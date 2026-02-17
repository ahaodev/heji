package com.hao.heji.ui.create

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
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
                createBillFragment.selectedCategory(type.value, category)
                createBillFragment.viewModel.getChildCategories(type.value, category.id)
            }
            onSubCategoryClick = { subCategory ->
                createBillFragment.selectedCategory(type.value, subCategory)
            }
            setOnItemClickListener { _, _, position ->
                val item = getItem(position)
                if (item is CategoryWithSubAdapter.CategoryItem.ParentItem) {
                    selectParent(item.category)
                }
            }
        }
    }

    private val createBillFragment by lazy {
        (parentFragment) as CreateBillFragment
    }

    //选中的标签、默认选择第一个、没有时为空
    private var selectCategory: Category? = null

    //类型 支出 或 收入
    lateinit var type: BillType

    override fun layout() = binding.root

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onAttach(context: Context) {
        super.onAttach(context)
       binding.root.post {
            arguments?.let {
                type = CategoryFragmentArgs.fromBundle(it).type
                if (type == BillType.INCOME)//预加载一次
                    createBillFragment.viewModel.getCategories(type.value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.post {
            with(createBillFragment) {
                viewModel.getCategories(type.value)
            }
            createBillFragment.selectedCategory(type.value, selectCategory)
            LogUtils.d(selectCategory)
            LogUtils.d(type)
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


    /**
     *
     *  @see TypeTabFragment.setCategories
     * @param categories
     */
    fun setCategories(categories: MutableList<Category>) {
        LogUtils.d(
            "TimeTest", type,
            TimeUtils.millis2String(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss")
        )
        categoryAdapter.setParentCategories(categories)
        defSelected()
    }

    fun setSubCategories(children: MutableList<Category>) {
        categoryAdapter.showSubCategories(children)
    }

    /**
     * 默认第一个为选中
     */
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

    fun setSelectCategory(category: String? = null) {
        if (!category.isNullOrEmpty()) {
            binding.root.post {
                categoryAdapter.setSelectCategoryByName(category)
            }
        }
    }

    companion object {
        /**
         * 收\支
         *
         * @param type Income : Expenditure
         * @return
         */
        @JvmStatic
        fun newInstance(type: BillType): CategoryFragment {
            LogUtils.d(type)
            val categoryFragment = CategoryFragment()
            categoryFragment.arguments =
                CategoryFragmentArgs(type).toBundle()
            return categoryFragment
        }
    }
}
