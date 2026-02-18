package com.hao.heji.ui.setting

import android.app.Activity
import android.content.Intent
import android.view.View
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.lxj.xpopup.XPopup
import com.hao.heji.R
import com.hao.heji.databinding.FragmentSettingBinding
import com.hao.heji.ui.base.render
import com.hao.heji.ui.base.BaseFragment

class SettingFragment : BaseFragment() {
    private val REQ_CODE_ALIPAY = 90001
    private val REQ_CODE_WEIXINPAY = 90002
    private val binding: FragmentSettingBinding by lazy {
        FragmentSettingBinding.inflate(
            layoutInflater
        )
    }
    private val viewModel by koinViewModel<SettingViewModel>()

    override fun initView(rootView: View) {
        binding.inputETC.setOnClickListener { findNavController().navigate(R.id.nav_input_etc) }
        binding.exportQianJi.setOnClickListener { findNavController().navigate(R.id.nav_export) }
        binding.inputAliPay.setOnClickListener {
            selectInputFile(REQ_CODE_ALIPAY)
        }
        binding.inputWeiXinPay.setOnClickListener {
            selectInputFile(REQ_CODE_WEIXINPAY)
        }
        render()
    }

    private fun selectInputFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, requestCode)
    }

    private val loadingDialog by lazy {
        XPopup.Builder(this.context).asLoading()
    }

    private fun render() {
        render(viewModel) {
            when (it) {
                is SettingUiState.InputEnd -> {
                    loadingDialog.setTitle(it.title)
                    loadingDialog.show()
                    rootView.postDelayed({ loadingDialog.dismiss() }, 1000)
                }
                is SettingUiState.InputError -> {
                    loadingDialog.setTitle(it.title)
                    loadingDialog.show()
                    rootView.postDelayed({ loadingDialog.dismiss() }, 1000)
                }
                is SettingUiState.InputReading -> {
                    loadingDialog.setTitle(it.title)
                    loadingDialog.show()
                }
            }

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data?.data == null) return
        val uri = data.data!!
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            if (inputStream == null) {
                LogUtils.e("无法打开文件: $uri")
                return
            }
            val fileName = getFileName(uri)
            when (requestCode) {
                REQ_CODE_ALIPAY -> viewModel.inputAlipayData(fileName, inputStream)
                REQ_CODE_WEIXINPAY -> viewModel.inputWeixinData(fileName, inputStream)
            }
        } catch (e: Exception) {
            LogUtils.e("打开文件失败: ${e.message}")
        }
    }

    private fun getFileName(uri: android.net.Uri): String {
        var name = ""
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name.ifEmpty { uri.lastPathSegment ?: "unknown" }
    }

    override fun layout() = binding.root

    override fun setUpToolBar() {
        super.setUpToolBar()
        toolBar.title = "设置"
        toolBar.navigationIcon = blackDrawable()
        toolBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

}