package com.hao.heji.ui.user

import android.util.Base64
import com.blankj.utilcode.util.StringUtils
import com.hao.heji.config.LocalUser
import org.json.JSONObject

object JWTParse {
    //JWT User
    data class User(val name: String, val id: String, val token: String)

    //解析JWT用户信息
    fun getUser(jwt: String): User {
        if (jwt == "") return LocalUser
        val token = resolveToken(jwt)
        val splits = token.split(".")
//        val header = splits[0]
        val payload = splits[1]
        var userJsonInfo = payload.replace('_', '/').replace('-', '+')
        userJsonInfo = String(Base64.decode(userJsonInfo, Base64.DEFAULT))
        val jsonObject = JSONObject(userJsonInfo)
        val name = jsonObject.opt("name") as String
        val id: String = jsonObject.opt("id") as String
//        val exp: String = jsonObject.opt("exp") as String
        return User(name, id, jwt)
    }

    private fun resolveToken(token: String): String {
        val bearer = "Bearer "
        return if (!StringUtils.isEmpty(token) && token.startsWith(bearer)) {
            token.substring(bearer.length)
        } else token
    }
}