package com.hao.heji.ui.user.register

import java.io.Serializable
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
data class RegisterUser(
    var name: String,
    var password: String,
    var tel: String,
) : Serializable