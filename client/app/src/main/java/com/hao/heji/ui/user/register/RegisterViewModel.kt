package com.hao.heji.ui.user.register

import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.data.repository.UserRepository
import com.hao.heji.utils.launch

internal class RegisterViewModel(private val userRepository: UserRepository) : BaseViewModel<RegisterUiState>() {
    fun register(
        username: String,
        tel: String,
        password: String
    ) {
        val user = RegisterUser(
            name = username,
            tel = tel,
            password = password,
        )
        launch({
            val response = userRepository.register(user)
            if (response.success()) {
                send(RegisterUiState.Success(user))
            }
        }, {
            ToastUtils.showLong(it.message)
        })

    }
}

