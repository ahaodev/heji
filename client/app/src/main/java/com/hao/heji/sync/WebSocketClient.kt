package com.hao.heji.sync

import android.util.Log
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class WebSocketClient {
    private var wsUrl = ""
    private val okhttpClient: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    enum class Status {
        OPEN, CLOSE, ERROR, CONNECTING
    }

    private var status = Status.CLOSE
    private var webSocket: WebSocket? = null
    private var request: Request? = null

    private val syncJob = Job()
    private val syncScope = CoroutineScope(Dispatchers.Main + syncJob)

    private val syncReceiver by lazy { SyncReceiver() }
    private val syncTrigger by lazy { SyncTrigger(syncScope) }

    companion object {
        @Volatile
        private var instance: WebSocketClient? = null
        fun getInstance(): WebSocketClient {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = WebSocketClient()
                    }
                }
            }
            return instance!!
        }
    }

    fun send(message: SyncMessage): Boolean {
        val json = message.toJson()
        LogUtils.d(json)
        webSocket?.send(json)
        return true
    }

    fun close() {
        LogUtils.d("close websocket")
        webSocket?.close(1000, "主动关闭")
        okhttpClient.dispatcher.cancelAll()
        status = Status.CLOSE
        okhttpClient.dispatcher.executorService.shutdown()
        syncJob.cancel()
    }

    fun isOpen(): Boolean {
        return status == Status.OPEN
    }

    fun isReconnect(): Boolean {
        return status == Status.CONNECTING
    }

    fun isClose(): Boolean {
        return status == Status.CLOSE
    }

    fun isError(): Boolean {
        return status == Status.ERROR
    }

    private fun reconnect(token: String) {
        status = Status.CONNECTING
        close()
        connect(wsUrl, token)
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            LogUtils.d("WebSocket onOpen:${response}")
            status = Status.OPEN
            syncReceiver.register()
            syncTrigger.register()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            LogUtils.d("onMessage", text)
            syncScope.launch(Dispatchers.IO) {
                syncReceiver.onReceiver(webSocket, text)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            status = Status.CLOSE
            super.onClosed(webSocket, code, reason)
            LogUtils.d("WebSocket onClosed: $code $reason")
            syncReceiver.unregister()
            syncTrigger.unregister()
            syncJob.cancel()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            status = Status.ERROR
            super.onFailure(webSocket, t, response)
            LogUtils.e(webSocket, t, response)
            syncJob.cancel()
        }
    }

    fun connect(
        wsUrl: String,
        token: String
    ) {
        LogUtils.d(wsUrl, token)
        if (status == Status.OPEN) {
            webSocket?.close(1000, "关闭旧的连接")
        }
        this.wsUrl = wsUrl
        request = Request.Builder()
            .addHeader("Authorization", " Bearer $token")
            .url(wsUrl)
            .build()
        LogUtils.d("run: connect url=${wsUrl}")
        if (webSocket == null) {
            request?.let {
                webSocket = okhttpClient.newWebSocket(it, webSocketListener)
            }
        }
    }
}