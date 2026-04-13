package com.hao.heji

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hao.heji.config.Config
import com.hao.heji.ui.user.JWTParse
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith


/**
 * @date: 2022/11/19
 * @author: 锅得铁
 * #
 */
@RunWith(AndroidJUnit4::class)
class ConfigTest: TestCase(){
    @Test
    fun readWrite(){
        runBlocking {
            val user = JWTParse.User("localUser", "user0","")
            assert(user== Config.user)
        }

    }
}