package com.hao.heji

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hao.heji.config.Config
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val loginEvent = MutableSharedFlow<Event<Any>>()

    private val _configChange = MutableSharedFlow<Config>(extraBufferCapacity = 1)
    val configChange: SharedFlow<Config> = _configChange

    fun notifyConfigChanged(config: Config) {
        _configChange.tryEmit(config)
    }
}