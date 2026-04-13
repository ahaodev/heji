package com.hao.heji.ui.user.login

import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.config.LocalUser
import com.hao.heji.config.store.DataStoreManager
import com.hao.heji.data.repository.UserRepository
import com.hao.heji.network.HttpManager
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.ui.user.JWTParse
import kotlinx.coroutines.launch


internal class LoginViewModel(
    private val userRepository: UserRepository,
    private val httpManager: HttpManager
) : BaseViewModel<LoginUiState>() {


    fun login(tel: String, password: String) {
        viewModelScope.launch {
            try {
                val resp = userRepository.login(
                    tel,
                    password
                )
                resp.data?.let {
                    val newUser = JWTParse.getUser(it)
                    Config.setUser(newUser)
                    Config.enableOfflineMode(false)
                    App.switchDataBase(newUser.id)
                    fetchMqttBroker()
                    send(LoginUiState.LoginSuccess(it))
                }
            } catch (e: Throwable) {
                send(LoginUiState.LoginError(e))
            }
        }
    }

    private suspend fun fetchMqttBroker() {
        try {
            val resp = httpManager.getMqttBroker()
            resp.data?.let { broker ->
                Config.setMqttBrokerUrl(broker.toTcpUrl())
            }
        } catch (e: Exception) {
            LogUtils.w("Failed to fetch MQTT broker: ${e.message}")
        }
    }

   fun getServerUrl() {
           send(LoginUiState.ShowServerSetting(DataStoreManager.getServerUrl()))
    }
    fun saveServerUrl(address:String) {
        Config.setServerUrl(address)
        httpManager.redirectServer()
    }

    /**
     * 开启离线使用模式
     */
    fun enableOfflineMode() {
        Config.enableOfflineMode(true)
        Config.setUser(LocalUser)
        send(LoginUiState.OfflineRun)
    }
}






