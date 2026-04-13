package com.hao.heji.ui.user.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lxj.xpopup.XPopup
import com.hao.heji.*
import com.hao.heji.databinding.FragmentLoginBinding
import com.hao.heji.ui.MainActivity
import com.hao.heji.ui.base.render
import com.hao.heji.ui.user.register.RegisterUser
import com.lxj.xpopup.impl.InputConfirmPopupView
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private val binding: FragmentLoginBinding by lazy { FragmentLoginBinding.inflate(layoutInflater) }
    private val viewModel by koinViewModel<LoginViewModel>()
    private val serverUrlInputConfirm: InputConfirmPopupView by lazy {
        XPopup.Builder(context).asInputConfirm(
            "服务", "请输入服务地址", "http://192.168.8.68:8080"
        ) {
            LogUtils.d(it)
            lifecycleScope.launch {
                viewModel.saveServerUrl(it)
            }
            serverUrlInputConfirm.dismiss()
        }
    }
    private val asConfirm by lazy {
        XPopup.Builder(requireActivity()).asConfirm(
            "仅离线使用说明",
            "1.不支持合伙记账   \n" +
                    "2.数据仅存储在本地"
        ) {
            viewModel.enableOfflineMode()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initView()
        renderView()
        return binding.root
    }

    private fun renderView() {
        render(viewModel) { state ->
            when (state) {
                is LoginUiState.LoginSuccess -> {
                    findNavController().popBackStack()
                    (activity as LoginActivity).startMainActivity()
                    LogUtils.d(state.token)
                }

                is LoginUiState.LoginError -> {
                    ToastUtils.showLong("登陆错误:${state.t.message}")
                }

                is LoginUiState.OfflineRun -> {
                    MainActivity.start(requireActivity())
                    activity?.finish()
                }

                is LoginUiState.ShowServerSetting -> {
                    with(serverUrlInputConfirm) {
                        show()
                        post {
                            editText.setText(state.url)
                        }
                    }
                }
            }
        }
    }

    fun initView() {
        with(binding) {
            tvRegister.setOnClickListener {
                findNavController().navigate(R.id.nav_register)
            }
            btnLogin.setOnClickListener {
                val username = binding.editUser.text.toString()
                val password = binding.editPassword.text.toString()
                viewModel.login(username, password)
            }
            tvNetSetting.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.getServerUrl()
                }
            }
            tvOnlyLocalUse.setOnClickListener {
                asConfirm.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTitle()
        autoInputUserInfo()
    }

    private fun autoInputUserInfo() {
        arguments?.let {
            var user: RegisterUser = it.getSerializable("user") as RegisterUser
            binding.editUser.setText(user.tel)
            binding.editPassword.setText(user.password)
            ToastUtils.showLong(user.name)
        }
    }

    private fun setTitle() {
        with(activity as LoginActivity) {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            toolbar.title = getString(R.string.login)
            toolbar.navigationIcon = null
        }
    }

}