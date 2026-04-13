package com.hao.heji.ui.category.manager

import android.os.Bundle
import android.view.View
import android.widget.EditText
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.google.android.material.tabs.TabLayout
import com.hao.heji.data.BillType
import com.hao.heji.data.db.Category
import com.hao.heji.databinding.FragmentCategoryManagerBinding
import com.hao.heji.ui.base.BaseFragment
import com.hao.heji.ui.base.render

import com.lxj.xpopup.XPopup

/**
 * 类别标签管理
 * @date: 2020/10/10
 * @author: 锅得铁
 *
 */
class CategoryManagerFragment : BaseFragment() {
    val binding: FragmentCategoryManagerBinding by lazy {
        FragmentCategoryManagerBinding.inflate(layoutInflater)
    }

    private val viewModel: CategoryManagerViewModel by koinViewModel()
    private var currentType: Int = BillType.EXPENDITURE.value
    private lateinit var adapter: CategoryManagerAdapter
    override fun layout() = binding.root

    override fun initView(rootView: View) {
        val ieType = arguments?.getInt("ieType", BillType.EXPENDITURE.value) ?: BillType.EXPENDITURE.value
        currentType = ieType

        setupTabLayout()
        setupRecyclerView()
        setupFab()

        viewModel.getParentCategories(currentType)
    }

    private fun setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("支出"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("收入"))

        // 选中对应tab
        val tabIndex = if (currentType == BillType.INCOME.value) 1 else 0
        binding.tabLayout.getTabAt(tabIndex)?.select()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = if (tab.position == 0) BillType.EXPENDITURE.value else BillType.INCOME.value
                viewModel.getParentCategories(currentType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        binding.categoryRecycler.layoutManager = LinearLayoutManager(context)
        adapter = CategoryManagerAdapter()
        adapter.onMoreClick = { category -> showMoreMenu(category) }
        adapter.onExpandClick = { category, expanded ->
            if (expanded) {
                viewModel.getChildCategories(category.id)
            }
        }
        adapter.onAddChildClick = { parentCategory ->
            showAddChildDialog(parentCategory)
        }
        adapter.onItemClick = { category ->
            showEditDialog(category)
        }
        binding.categoryRecycler.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(viewModel) {
            when (it) {
                is CategoryManagerUiState.Categories -> adapter.setNewInstance(it.data)
                is CategoryManagerUiState.ParentCategories -> adapter.setNewInstance(it.data)
                is CategoryManagerUiState.ChildCategories -> {
                    adapter.updateChildren(it.parentId, it.data)
                }
                is CategoryManagerUiState.SaveSuccess -> {
                    viewModel.getParentCategories(currentType)
                    if (!it.parentId.isNullOrEmpty()) {
                        viewModel.getChildCategories(it.parentId)
                    }
                }
            }
        }
    }

    override fun setUpToolBar() {
        super.setUpToolBar()
        toolBar.apply {
            title = ""
            navigationIcon = blackDrawable()
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun showMoreMenu(category: Category) {
        val isDefault = category.name == "其他" || category.name == "其它"
        if (isDefault) return

        XPopup.Builder(requireContext())
            .atView(null)
            .asBottomList("操作", arrayOf("修改名称", "删除")) { position, _ ->
                when (position) {
                    0 -> showEditDialog(category)
                    1 -> alertDeleteTip(category)
                }
            }.show()
    }

    private fun showAddCategoryDialog() {
        XPopup.Builder(requireContext())
            .hasStatusBarShadow(false)
            .asInputConfirm("添加一级分类", "请输入分类名称") { name ->
                viewModel.saveCategory(name, currentType)
            }.show()
    }

    private fun showAddChildDialog(parentCategory: Category) {
        XPopup.Builder(requireContext())
            .hasStatusBarShadow(false)
            .asInputConfirm("添加子类 - ${parentCategory.name}", "请输入子类名称") { name ->
                viewModel.saveCategory(name, currentType, parentCategory.id)
            }.show()
    }

    private fun showEditDialog(category: Category) {
        val isDefault = category.name == "其他" || category.name == "其它"
        if (isDefault) return

        XPopup.Builder(requireContext())
            .hasStatusBarShadow(false)
            .asInputConfirm("修改分类", "请输入新名称", category.name, null) { newName ->
                if (newName.isNotBlank() && newName != category.name) {
                    category.name = newName
                    category.hashValue = category.hashCode()
                    viewModel.updateCategory(category)
                }
            }.show()
    }

    private fun alertDeleteTip(category: Category) {
        XPopup.Builder(requireContext()).asConfirm("提示", "确认删除该标签？") {
            viewModel.deleteCategory(category)
        }.show()
    }

}