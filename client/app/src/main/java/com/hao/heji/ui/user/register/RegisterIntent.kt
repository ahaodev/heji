package com.hao.heji.ui.user.register

import com.hao.heji.ui.base.IUiState

/**
 * @date 2022/9/29
 * @author 锅得铁
 * @since v1.0
 */
internal sealed interface RegisterUiState : IUiState {
    class Success(val user: RegisterUser) : RegisterUiState
    class Error(val throwable: Throwable) : RegisterUiState
}