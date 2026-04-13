package com.hao.heji.ui.setting.input.etc

import com.hao.heji.ui.base.IUiState

/**
 *
 * @date 2022/9/30
 * @author 锅得铁
 * @since v1.0
 */

internal sealed interface ETCUiState : IUiState {
    data object InputSuccess : ETCUiState
    class InputError(val t: Throwable) : ETCUiState
}