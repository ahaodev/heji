package com.hao.heji.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.navigation.NavigationView
import com.lxj.xpopup.XPopup
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.R
import com.hao.heji.config.LocalUser
import com.hao.heji.databinding.HeaderMainNavBinding
import com.hao.heji.sync.SyncService
import com.hao.heji.ui.DrawerListener
import com.hao.heji.ui.user.JWTParse
import com.hao.heji.ui.user.login.LoginActivity
import com.hao.heji.utils.permitDiskReads
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController private set
    private lateinit var drawerLayout: DrawerLayout
    val viewModel: MainViewModel by koinViewModel()
    private lateinit var navHeaderMainBinding: HeaderMainNavBinding//侧拉头像
    private lateinit var navigationView: NavigationView

    lateinit var mService: SyncService
    lateinit var mServiceBinder: SyncService.SyncBinder
    private var mIsBound: Boolean = false

    companion object {
        private const val TAG = "MainActivity"
        fun start(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
        }
    }

    /**
     * Connection service
     */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mServiceBinder = service as SyncService.SyncBinder
            mService = mServiceBinder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }

    override fun onStart() {
        viewModel.switchModelAndBook()
        super.onStart()
        if (!Config.enableOfflineMode) {
            doBindService()
        }
    }

    private fun doBindService() {
        if (Config.user != LocalUser) {
            Intent(this, SyncService::class.java).also {
                startService(it)
                mIsBound = bindService(it, connection, BIND_AUTO_CREATE)
            }
        }

    }

    private fun doUnbindService() {
        if (mIsBound) {
            Intent(this, SyncService::class.java).also {
                unbindService(connection)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        permitDiskReads { super.onCreate(savedInstanceState) }//StrictMode policy violation; ~duration=127 ms: android.os.strictmode.DiskReadViolation by XiaoMi
        setContentView(R.layout.activity_main)
        initDrawerLayout()
        checkLogin()
        setDrawerLayout(Config.user)
    }

    private fun checkLogin() {
        if (!Config.enableOfflineMode) {
            if (Config.user == LocalUser) {
                toLogin()
            }
        }
        lifecycleScope.launch {
            //观察拦截器发出的登录消息
            App.viewModel.loginEvent.filterNotNull().collect {
                navController.currentBackStackEntry?.let {
                    if (it.destination.label != resources.getString(R.string.login)) {
                        if (!Config.isInitUser())
                            toLogin()
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            @SuppressLint("WrongConstant")
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.nav_home) { //主页
                    if (drawerLayout.isDrawerOpen(Gravity.START)) {
                        drawerLayout.closeDrawer(Gravity.START)
                    } else {
                        finish()
                    }
                } else {
                    if (drawerLayout.isDrawerOpen(Gravity.START)) {
                        drawerLayout.closeDrawer(Gravity.START)
                    } else {
                        navController.popBackStack()
                    }
                }
            }
        })
    }

    /**
     * 侧滑菜单相关
     */
    private fun initDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        val navHostFragmentRootView: View = findViewById(R.id.nav_host_fragment)
        drawerLayout.addDrawerListener(DrawerListener(this) { left: Int, top: Int, right: Int, bottom: Int ->
            navHostFragmentRootView.layout(left, top, right, bottom)
        })
        //Logout Menu
        val navMenu = navigationView.menu
        navMenu.findItem(R.id.menu_logout).setOnMenuItemClickListener {
            XPopup.Builder(this@MainActivity).asConfirm("退出确认", "确认退出当前用户吗?") {
                Config.remove()
                finish()
                LoginActivity.start(this)
            }.show()
            false
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        //系统默认侧滑控制
        //NavigationUI.setupWithNavController(navigationView, navController);
        //自定义控制
        navigationDrawerController()
        val navHeaderView = navigationView.getHeaderView(0)
        navHeaderMainBinding = HeaderMainNavBinding.bind(navHeaderView)
        navHeaderView.setOnClickListener {
            ToastUtils.showLong("")
            navController.navigate(R.id.nav_user_info)
            drawerLayout.closeDrawers()
        }
        setCurrentBook(Config.book.name)
    }

    private fun navigationDrawerController() {
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            val handled = item.isChecked //已选中
            val parent = navigationView.parent
            if (parent is DrawerLayout) {
                parent.closeDrawer(navigationView)
            }
            if (!handled) {
                NavigationUI.onNavDestinationSelected(item, navController)
                if (navController.currentDestination?.id != R.id.nav_home) navController.popBackStack()
                try {
                    navController.navigate(item.itemId)
                    return@setNavigationItemSelectedListener true
                } catch (e: IllegalArgumentException) {
                    return@setNavigationItemSelectedListener false
                }
            }
            true
        }
        val weakReference = WeakReference(navigationView)
        navController.addOnDestinationChangedListener(
            object : OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination, arguments: Bundle?
                ) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }
                    val menu = view.menu
                    var h = 0
                    val size = menu.size()
                    while (h < size) {
                        val item = menu.getItem(h)
                        var currentDestination: NavDestination = destination
                        while (currentDestination.id != item.itemId && currentDestination.parent != null) {
                            currentDestination = currentDestination.parent!!
                        }
                        val isChecked = currentDestination.id == item.itemId
                        item.isChecked = isChecked
                        h++
                    }
                    val home = resources.getString(R.string.menu_home)
                    val report = resources.getString(R.string.menu_report)
                    val setting = resources.getString(R.string.menu_setting)
                    when (destination.label.toString()) {
                        home -> {
                            title = "记账"
                        }

                        report -> {

                        }

                        setting -> {

                        }
                    }

                    LogUtils.d(TAG, destination.label.toString())
                }
            })
    }

    private fun setDrawerLayout(user: JWTParse.User) {
        navHeaderMainBinding.tvTitle.text = user.name
        navHeaderMainBinding.tvNice.text = user.id
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = fragments
        for (i in fragments.indices) {
            fragments[i].onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragments = fragments
        for (i in fragments.indices) {
            fragments[i].onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val fragments = fragments
        for (i in fragments.indices) {
            fragments[i].onContextItemSelected(item)
        }
        return super.onContextItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val fragments = fragments
        for (i in fragments.indices) {
            fragments[i].onPrepareOptionsMenu(menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private val fragments: List<Fragment>
        get() {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            return navHostFragment.childFragmentManager.fragments
        }


    /**
     * 隐藏键盘
     */
    fun hideInput() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = window.peekDecorView()
        if (null != v) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun disableDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun enableDrawer() {
        if (this::drawerLayout.isInitialized)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun setCurrentBook(bookName: String) {
        val bookItem = navigationView.menu.findItem(R.id.nav_book_list)
        val str1 = getString(R.string.menu_book)
        val str2 = "[$bookName]"
        val spannableString = SpannableString("$str1$str2")
        spannableString.setSpan(
            ForegroundColorSpan(getColor(R.color.textRemark)),
            str1.length,
            spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        bookItem.title = spannableString
    }

    private fun toLogin() {
        LoginActivity.start(this)
        finish()
        ToastUtils.showLong("用户凭证已失效，请重新登录")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Config.enableOfflineMode) {
            doUnbindService()
        }
    }
}