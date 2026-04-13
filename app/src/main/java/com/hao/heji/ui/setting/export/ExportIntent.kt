package com.hao.heji.ui.setting.export

import com.hao.heji.ui.base.IUiState

/**
 *
 * @date 2022/10/1
 * @author 锅得铁
 * @since v1.0
 */
internal sealed interface ExportUiState : IUiState {
    class Success(val path: String) : ExportUiState
    class Error(val t: Throwable) : ExportUiState
}