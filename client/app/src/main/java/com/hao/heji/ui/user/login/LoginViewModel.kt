package com.hao.heji.ui.user.login

import android.os.Build
import androidx.annotation.RequiresApi
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.config.LocalUser
import com.hao.heji.config.store.DataStoreManager
import com.hao.heji.data.repository.UserRepository
import com.hao.heji.network.HttpManager
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.ui.user.JWTParse
import com.hao.heji.utils.launch


internal class LoginViewModel(
    private val userRepository: UserRepository,
    private val httpManager: HttpManager
) : BaseViewModel<LoginUiState>() {


    @RequiresApi(Build.VERSION_CODES.O)
    fun login(tel: String, password: String) {
        launch({
            var resp = userRepository.login(
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
        }, {
            send(LoginUiState.LoginError(it))
        })

    }

    private suspend fun fetchMqttBroker() {
        try {
            val resp = httpManager.getMqttBroker()
            resp.data?.let { broker ->
                Config.setMqttBrokerUrl(broker.toTcpUrl())
            }
        } catch (e: Exception) {
            com.blankj.utilcode.util.LogUtils.w("Failed to fetch MQTT broker: ${e.message}")
        }
    }

   fun getServerUrl() {
       launch({
           send(LoginUiState.ShowServerSetting(DataStoreManager.getServerUrl()))
       })
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
        launch({
            send(LoginUiState.OfflineRun)
        })
    }
}






