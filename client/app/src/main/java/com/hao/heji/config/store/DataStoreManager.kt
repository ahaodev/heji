package com.hao.heji.config.store

import com.hao.heji.BuildConfig
import com.hao.heji.data.db.Book
import com.hao.heji.moshi
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
        mmkv.encode(KEY_CURRENT_BOOK, moshi.adapter(Book::class.java).toJson(book))
    }

    fun getBook(): Book? {
        val bookJsonStr = mmkv.decodeString(KEY_CURRENT_BOOK) ?: return null
        return moshi.adapter(Book::class.java).fromJson(bookJsonStr)
    }

    fun removeBook() {
        mmkv.removeValueForKey(KEY_CURRENT_BOOK)
    }
}