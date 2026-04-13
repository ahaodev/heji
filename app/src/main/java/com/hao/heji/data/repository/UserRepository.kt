package com.hao.heji.data.repository

import com.hao.heji.network.HttpManager
import com.hao.heji.ui.user.register.RegisterUser
import retrofit2.await

class UserRepository(private val httpManager: HttpManager) {
    suspend fun register(registerUser: RegisterUser) =
        httpManager.register(registerUser)

    suspend fun login(username: String, password: String) =
        httpManager.login(username, password)
}