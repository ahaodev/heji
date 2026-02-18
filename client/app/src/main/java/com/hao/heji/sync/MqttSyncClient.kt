package com.hao.heji.sync

import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.hao.heji.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import com.github.shamil.Xid

/**
 * MQTT 客户端 (v2 — 纯订阅模式)
 * 只订阅服务端推送的通知主题 heji/notify/{userId}/，不再发布任何消息。
 * 数据同步通过 HTTP API 完成（SyncTrigger 独立运行）。
 */
class MqttSyncClient {
    private var mqttClient: MqttAndroidClient? = null
    private var brokerUrl = ""

    enum class Status {
        CONNECTED, DISCONNECTED, ERROR, CONNECTING
    }

    private var status = Status.DISCONNECTED

    private val syncJob = Job()
    private val syncScope = CoroutineScope(Dispatchers.Main + syncJob)

    private val syncReceiver by lazy { SyncReceiver() }

    companion object {
        // 下行通知主题：服务端 → 客户端
        private const val TOPIC_NOTIFY_BOOK  = "heji/notify/%s/book"
        private const val TOPIC_NOTIFY_BILL  = "heji/notify/%s/bill"
        private const val TOPIC_NOTIFY_IMAGE = "heji/notify/%s/image"

        @Volatile
        private var instance: MqttSyncClient? = null
        fun getInstance(): MqttSyncClient {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = MqttSyncClient()
                    }
                }
            }
            return instance!!
        }
    }

    fun close() {
        LogUtils.d("close mqtt")
        try {
            mqttClient?.disconnect()
        } catch (e: Exception) {
            LogUtils.e("MQTT disconnect error", e)
        }
        status = Status.DISCONNECTED
        syncJob.cancel()
    }

    fun isConnected(): Boolean = status == Status.CONNECTED
    fun isDisconnected(): Boolean = status == Status.DISCONNECTED
    fun isError(): Boolean = status == Status.ERROR

    fun connect(context: Context, brokerUrl: String, token: String) {
        LogUtils.d("MQTT connect: $brokerUrl")
        if (status == Status.CONNECTED) {
            close()
        }
        this.brokerUrl = brokerUrl
        status = Status.CONNECTING

        try {
            val clientId = "heji-android-${Xid.string()}"
            mqttClient = MqttAndroidClient(context, brokerUrl, clientId)

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    LogUtils.d("MQTT connected: $serverURI, reconnect=$reconnect")
                    status = Status.CONNECTED
                    subscribeTopics()
                    syncReceiver.register()
                }

                override fun connectionLost(cause: Throwable?) {
                    LogUtils.w("MQTT connection lost: ${cause?.message}")
                    status = Status.DISCONNECTED
                    syncReceiver.unregister()
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val text = String(it.payload)
                        LogUtils.d("MQTT notify from $topic: $text")
                        syncScope.launch(Dispatchers.IO) {
                            syncReceiver.onReceiver(text)
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = false
                connectionTimeout = 30
                keepAliveInterval = 60
                userName = Config.user.id
                password = token.toCharArray()
            }

            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    LogUtils.d("MQTT connect success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    LogUtils.e("MQTT connect failed", exception)
                    status = Status.ERROR
                }
            })
        } catch (e: Exception) {
            LogUtils.e("MQTT connect error", e)
            status = Status.ERROR
        }
    }

    private fun subscribeTopics() {
        try {
            val userId = Config.user.id
            val bookTopic = String.format(TOPIC_NOTIFY_BOOK, userId)
            val billTopic = String.format(TOPIC_NOTIFY_BILL, userId)
            val imageTopic = String.format(TOPIC_NOTIFY_IMAGE, userId)
            mqttClient?.subscribe(bookTopic, 1)
            mqttClient?.subscribe(billTopic, 1)
            mqttClient?.subscribe(imageTopic, 1)
            LogUtils.d("MQTT subscribed: $bookTopic, $billTopic, $imageTopic")
        } catch (e: Exception) {
            LogUtils.e("MQTT subscribe error", e)
        }
    }
}

