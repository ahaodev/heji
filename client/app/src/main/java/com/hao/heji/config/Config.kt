package com.hao.heji.config

import com.hao.heji.App
import com.hao.heji.BuildConfig
import com.hao.heji.config.store.DataStoreManager
import com.hao.heji.data.db.Book
import com.hao.heji.ui.user.JWTParse

/**
 *Date: 2022/11/13
 *Author: 锅得铁
 *#
 */
internal val LocalUser = JWTParse.User("LocalUser", "user0", "")
internal val InitBook = Book(
    name = "个人账本",
    crtUserId = LocalUser.id,
    isInitial = true,
    type = "离线账本",
)

object Config {

    private var _serverUrl = BuildConfig.HTTP_URL
    private var _user = LocalUser
    private var _book: Book = InitBook
    private var _enableOfflineMode = false

    val serverUrl: String get() = _serverUrl
    val book: Book get() = _book
    val user get() = _user
    val enableOfflineMode: Boolean get() = _enableOfflineMode

    fun isInitUser() = (user == LocalUser)

    suspend fun setBook(book: Book) {
        this._book = book
        DataStoreManager.saveBook(book)
        App.viewModel.notifyConfigChanged(this)
    }

    suspend fun setUser(user: JWTParse.User) {
        this._user = user
        DataStoreManager.saveToken(user.token)
        App.viewModel.notifyConfigChanged(this)
    }

    fun setServerUrl(url: String) {
        this._serverUrl = url
        DataStoreManager.saveServerUrl(url)
    }

    suspend fun enableOfflineMode(enable: Boolean) {
        _enableOfflineMode = enable
        DataStoreManager.saveUseMode(enableOfflineMode)
        App.viewModel.notifyConfigChanged(this)
    }

    fun load() {
        with(DataStoreManager) {
            _enableOfflineMode = getUseMode()
            getBook()?.let { _book = it }
            getToken().takeIf { it.isNotEmpty() }?.let { _user = JWTParse.getUser(jwt = it) }
            _serverUrl = getServerUrl()
        }
    }

    suspend fun remove() {
        with(DataStoreManager) {
            removeUseMode()
            removeToken()
            removeBook()
        }
        _enableOfflineMode = false
        _user = LocalUser
        _book = InitBook
        App.viewModel.notifyConfigChanged(this)
    }
}