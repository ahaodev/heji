package com.hao.heji.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author 锅得铁
 * @param I  intent action
 * @param O  data
 */
abstract class BaseViewModel< O : IUiState> : ViewModel() {
    /**
     * Ui state launcher
     */
    private var _uiState = MutableSharedFlow<O>()

    /**
     *
     * Provide to view
     * @return LiveData<O>
     */
    fun uiState(): SharedFlow<O> = _uiState

    fun send(o: O) {
        viewModelScope.launch {
            _uiState.emit(o)
        }
    }
}