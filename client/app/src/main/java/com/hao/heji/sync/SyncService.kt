package com.hao.heji.sync

import NetworkMonitor
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.blankj.utilcode.util.LogUtils
import com.hao.heji.App
import com.hao.heji.config.Config

/**
 * 同步服务
 * @date 2022/6/20
 * @author 锅得铁
 * @since v1.0
 */
class SyncService : Service(), Observer<Config> {
    private val binder = SyncBinder()
    private val mqttClient = MqttSyncClient.getInstance()

    private var networkMonitor: NetworkMonitor? = null
    private val configLiveData = App.viewModel.configChange.asLiveData()
    override fun onCreate() {
        super.onCreate()
        LogUtils.d("onCreate")
        networkMonitor = NetworkMonitor(this) {
            if (it) {
                if (mqttClient.isDisconnected() || mqttClient.isError()) {
                    connectSync()
                }
            }
        }
        networkMonitor?.startNetworkCallback()
        configLiveData.observeForever(this)
    }

    private fun connectSync() {
        val brokerUrl = Config.mqttBrokerUrl
        if (brokerUrl.isEmpty()) {
            LogUtils.w("MQTT broker URL not configured")
            return
        }
        val token = Config.user.token
        mqttClient.connect(context = this, brokerUrl = brokerUrl, token = token)
    }

    private fun closeSync() {
        mqttClient.close()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtils.d("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtils.d("onDestroy")
        closeSync()
        networkMonitor?.stopNetworkCallback()
        configLiveData.removeObserver(this)
    }

    inner class SyncBinder : Binder() {
        fun getService(): SyncService = this@SyncService
    }

    override fun onChanged(value: Config) {
        if (mqttClient.isDisconnected()) {
            connectSync()
        }
    }
}