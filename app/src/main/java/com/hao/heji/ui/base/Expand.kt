package com.hao.heji.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

internal fun <O : IUiState> Fragment.render(
    vm: BaseViewModel<O>,
    fn: (o: O) -> Unit
) {
    // Start a coroutine in the lifecycle scope
    lifecycleScope.launch {
        // repeatOnLifecycle launches the block in a new coroutine every time the
        // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.uiState().collect {
                fn(it)
            }
        }
    }
}
