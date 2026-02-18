package com.hao.heji.config.store

import com.hao.heji.BuildConfig
import com.hao.heji.data.db.Book
import com.hao.heji.json
import com.tencent.mmkv.MMKV

/**
 * DataStoreManager
 * @date 2022/5/9
 * @author 锅得铁
 * @since v1.0
 */
internal object DataStoreManager {

    private val mmkv: MMKV by lazy { MMKV.defaultMMKV() }

    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_USE_MODE = "use_mode"
    private const val KEY_CURRENT_BOOK = "current_book"
    private const val KEY_MQTT_BROKER_URL = "mqtt_broker_url"

    fun saveServerUrl(url: String) {
        mmkv.encode(KEY_SERVER_URL, url)
    }

    fun getServerUrl(): String {
        return mmkv.decodeString(KEY_SERVER_URL, BuildConfig.HTTP_URL) ?: BuildConfig.HTTP_URL
    }

    fun saveUseMode(enableOffline: Boolean) {
        mmkv.encode(KEY_USE_MODE, enableOffline)
    }

    fun getUseMode(): Boolean {
        return mmkv.decodeBool(KEY_USE_MODE, false)
    }

    fun removeUseMode() {
        mmkv.removeValueForKey(KEY_USE_MODE)
    }

    fun saveToken(token: String) {
        mmkv.encode(KEY_JWT_TOKEN, token)
    }

    fun getToken(): String {
        return mmkv.decodeString(KEY_JWT_TOKEN, "") ?: ""
    }

    fun removeToken() {
        mmkv.removeValueForKey(KEY_JWT_TOKEN)
    }

    fun saveBook(book: Book) {
        mmkv.encode(KEY_CURRENT_BOOK, json.encodeToString(Book.serializer(), book))
    }

    fun getBook(): Book? {
        val bookJsonStr = mmkv.decodeString(KEY_CURRENT_BOOK) ?: return null
        return json.decodeFromString(Book.serializer(), bookJsonStr)
    }

    fun removeBook() {
        mmkv.removeValueForKey(KEY_CURRENT_BOOK)
    }

    fun saveMqttBrokerUrl(url: String) {
        mmkv.encode(KEY_MQTT_BROKER_URL, url)
    }

    fun getMqttBrokerUrl(): String {
        return mmkv.decodeString(KEY_MQTT_BROKER_URL, "") ?: ""
    }

    fun removeMqttBrokerUrl() {
        mmkv.removeValueForKey(KEY_MQTT_BROKER_URL)
    }
}