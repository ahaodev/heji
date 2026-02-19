package com.hao.heji

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.hao.heji.config.Config
import com.hao.heji.ui.MainActivity
import com.hao.heji.ui.user.login.LoginActivity

/**
 * @date 2022/11/13
 * @author hao
 * app start page
 */
class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Config.enableOfflineMode) {
            startMainActivity()
        } else if (!Config.isInitUser()) {
            startMainActivity()
        } else {
            startLoginActivity()
        }
    }

    private fun startLoginActivity() {
        LoginActivity.start(this)
        this.finish()
    }

    private fun startMainActivity() {
        MainActivity.start(this)
        this.finish()
    }

    override fun onResume() {
        super.onResume()
        immersionBar {
            statusBarColor(R.color.colorPrimary)
            navigationBarColor(R.color.colorPrimary)
        }
    }
}