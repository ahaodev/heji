package com.hao.heji.ui.setting

import com.hao.heji.ui.base.IUiState

/**
 * @author 锅得铁
 * @date 2023/4/25
 * @since v1.0
 */
sealed interface SettingUiState : IUiState {
    class InputReading(val title: String) : SettingUiState
    class InputEnd(val title: String) : SettingUiState
    class InputError(val title: String) : SettingUiState
}