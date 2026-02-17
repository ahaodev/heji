package com.hao.heji.ui.book

import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.XPopup
import com.hao.heji.data.BookType
import com.hao.heji.databinding.FragmentBookAddBinding
import com.hao.heji.ui.base.BaseFragment

class CreateBookFragment : BaseFragment() {
    private val viewModel: BookViewModel by koinViewModel()
    private val binding: FragmentBookAddBinding by lazy {
        FragmentBookAddBinding.inflate(layoutInflater)
    }
    override fun setUpToolBar() {
        super.setUpToolBar()
        showBlack()
    }

    override fun layout(): View {
        return binding.root
    }

    override fun initView(rootView: View) {
        with(binding){
            banner.setOnClickListener { }
            layoutType.setOnClickListener {
                XPopup.Builder(requireContext()).asBottomList(
                    "选择账单类型", BookType.labels
                ) { _, text ->
                    tvBookType.text = text
                }.show()
            }
            btnCreate.setOnClickListener {
                val name = textInputEdit.text.toString()
                val type = tvBookType.text.toString()
                if (name.isEmpty()) {
                    ToastUtils.showShort("请选择填写账本名称")
                    return@setOnClickListener
                }
                if (type.isEmpty() || type == "未设置") {
                    ToastUtils.showShort("请选择账本类型")
                    return@setOnClickListener
                }
                viewModel.createNewBook(name, type)
            }
        }

        viewModel.bookCreate().observe(this) {
            findNavController().popBackStack()
        }
    }

}