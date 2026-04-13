package com.hao.heji.ui.user.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.hao.heji.App
import com.hao.heji.R
import com.hao.heji.config.Config
import com.hao.heji.ui.MainActivity
import com.hao.heji.utils.checkPermissions

/**
 * 用户登录注册
 *
 * @author 锅得铁
 * @date 2022/5/10
 * @since v1.0
 * @see LoginFragment
 */
class LoginActivity : AppCompatActivity() {
    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions(this) { _: Boolean, _: List<String?>?, _: List<String?>? ->
            //Toast.makeText(this, "已同意权限", Toast.LENGTH_SHORT).show()
        }
        setContentView(R.layout.activity_login)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FragmentContainerView>(R.id.nav_user_host_fragment)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                //finish()
            }
        })
    }
    override fun onResume() {
        super.onResume()
        immersionBar {
            statusBarColor(R.color.colorPrimary)
            navigationBarColor(R.color.white)
            titleBar(findViewById(R.id.toolbar))
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
        }
    }

    fun startMainActivity() {
        initUserBaseData()
        MainActivity.start(this)
        this.finish()
    }

    private fun initUserBaseData() {
        App.switchDataBase(Config.user.id)
    }
}