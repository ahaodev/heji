package com.hao.heji.config

import com.hao.heji.App
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

    val serverUrl: String get() = DataStoreManager.getServerUrl()
    val book: Book get() = DataStoreManager.getBook() ?: InitBook
    val user: JWTParse.User
        get() = DataStoreManager.getToken()
            .takeIf { it.isNotEmpty() }
            ?.let { JWTParse.getUser(jwt = it) }
            ?: LocalUser
    val enableOfflineMode: Boolean get() = DataStoreManager.getUseMode()

    fun isInitUser() = (user == LocalUser)

    fun setBook(book: Book) {
        DataStoreManager.saveBook(book)
        App.viewModel.notifyConfigChanged(this)
    }

    fun setUser(user: JWTParse.User) {
        DataStoreManager.saveToken(user.token)
        App.viewModel.notifyConfigChanged(this)
    }

    fun setServerUrl(url: String) {
        DataStoreManager.saveServerUrl(url)
    }

    fun enableOfflineMode(enable: Boolean) {
        DataStoreManager.saveUseMode(enable)
        App.viewModel.notifyConfigChanged(this)
    }

    fun remove() {
        with(DataStoreManager) {
            removeUseMode()
            removeToken()
            removeBook()
        }
        App.viewModel.notifyConfigChanged(this)
    }
}