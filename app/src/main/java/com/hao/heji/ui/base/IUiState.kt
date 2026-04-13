package com.hao.heji.ui.base

/**
 * IUiState
 * @date 2022/9/29
 * @author 锅得铁
 * @since v1.0
 */
interface IUiState {
    fun id() = "${javaClass.simpleName}${hashCode()}"
}