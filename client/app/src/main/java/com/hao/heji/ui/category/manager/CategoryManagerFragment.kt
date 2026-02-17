package com.hao.heji.ui.category.manager

import android.os.Bundle
import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
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
        FragmentCategoryManagerBinding.inflate(layoutInflater).apply {
            btnAdd.setOnClickListener { v: View ->
                val name = binding.editCategoryValue.text.toString().trim { it <= ' ' }
                viewModel.saveCategory(name, args.ieType)
                KeyboardUtils.hideSoftInput(v) //隐藏键盘
                binding.editCategoryValue.setText("")
                binding.editCategoryValue.clearFocus() //清除聚焦
            }
        }
    }

    private val viewModel: CategoryManagerViewModel by koinViewModel()
    lateinit var args: CategoryManagerFragmentArgs
    private lateinit var adapter: CategoryManagerAdapter
    override fun layout() = binding.root

    override fun initView(rootView: View) {
        args = CategoryManagerFragmentArgs.fromBundle(requireArguments())
        viewModel.getCategories(args.ieType)

        binding.categoryRecycler.layoutManager = LinearLayoutManager(context)
        adapter = object : CategoryManagerAdapter() {
            override fun convert(holder: BaseViewHolder, category: Category) {
                super.convert(holder, category)
                getItemBinding(holder).btnDelete.setOnClickListener {
                    alertDeleteTip(category)
                }
            }
        }
        binding.categoryRecycler.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(viewModel) {
            when (it) {
                is CategoryManagerUiState.Categories -> adapter.setNewInstance(it.data)
            }
        }
    }

    override fun setUpToolBar() {
        super.setUpToolBar()
        toolBar.apply {
            title = "分类管理(${BillType.transform(args.ieType).valueString})"
            navigationIcon = blackDrawable()
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun alertDeleteTip(label: Category) {
        XPopup.Builder(requireContext()).asConfirm("提示", "确认删除该标签？") {
            viewModel.deleteCategory(label)
        }.show()
    }

}