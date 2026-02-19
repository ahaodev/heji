package com.hao.heji.ui.base

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.navigationBarHeight
import com.lxj.xpopup.XPopup
import com.hao.heji.ui.MainActivity
import com.hao.heji.R
import com.hao.heji.ui.popup.YearSelectPopup
import java.util.*

/**
 * @date: 2020/8/28
 * @author: 锅得铁
 * #
 */
abstract class BaseFragment : Fragment() {
    lateinit var rootView: View
    var enableDrawer = true
        set(value) {
            if (value)
                mainActivity.enableDrawer() else mainActivity.disableDrawer()
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (!this::rootView.isInitialized) {
            rootView = layout()
            initView(layout())
            enableDrawer = true
            setHasOptionsMenu(true)
            LogUtils.d("Fragment onCreateView : ${this.javaClass.name} ")
        }
        return rootView
    }

    abstract fun initView(rootView: View)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            ImmersionBar.with(this)
                .titleBar(toolBar)
                .autoDarkModeEnable(true)
                .transparentStatusBar()
                .hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
                .navigationBarColor(R.color.white)
                .fullScreen(false)
                .init()
        } catch (e: Exception) {
            Log.d("BaseFragment", "ImmersionBar: {${e.message}}")
        }
    }

    override fun onResume() {
        super.onResume()
        setUpToolBar()
    }

    /**
     * 执行在onCreateView的时候
     *
     * @return rootView
     */
    protected abstract fun layout(): View


    /**
     * 重置并设置tool bar
     *
     */
    protected open fun setUpToolBar() {
        try {
            toolBar.menu.clear()
        } catch (e: Exception) {
            Log.d("BaseFragment", "setUpToolBar: {${e.message}}")
        }
    }

    /**
     * 唯一的 activity
     */
    val mainActivity: MainActivity get() = activity as MainActivity

    /**
     * 显示回退按钮
     *
     */
    protected fun showBlack() {
        toolBar.navigationIcon = blackDrawable()
        toolBar.setNavigationOnClickListener { findNavController().navigateUp() }
        KeyboardUtils.hideSoftInput(toolBar)
    }

    val toolBar: Toolbar
        get() = rootView.findViewById(R.id.toolbar)
    val centerTitle: TextView
        get() = rootView.findViewById(R.id.toolbar_center_title)

    /**
     * 屏幕的高度-底部导航栏高度-顶部toolbar高度
     *
     * @return
     */
    fun getRootViewHeight(): Int {
        var height = ScreenUtils.getScreenHeight() - navigationBarHeight
        kotlin.runCatching {
            height -= toolBar.height
        }
        return height //占满一屏
    }

    fun blackDrawable(): Drawable = ResourcesCompat.getDrawable(resources,
        androidx.appcompat.R.drawable.abc_ic_ab_back_material,
        mainActivity.theme
    )!!


    /**
     *   拦截回退直接退出  object : Class 内部类指定owner 仅在该Fragment生命周期下有效
     */
    fun registerBackPressed(block: () -> Unit) = mainActivity.onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                block()
            }
        })

    /**
     * 该Menu属于全局所以在这里控制
     */
    @SuppressLint("SetTextI18n")
    fun showYearMonthTitle(
        year: Int = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
        showAllYear: Boolean = false,
        onTabSelected: YearSelectPopup.OnTabSelected
    ) {
        rootView.post {
            with(centerTitle) {
                visibility = View.VISIBLE
                compoundDrawablePadding = 8
                text = "$year.$month"
                val drawable= ResourcesCompat.getDrawable(resources,R.drawable.ic_baseline_arrow_down_white_32,null)
                setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    drawable,
                    null
                )
                setOnClickListener {
                    val yearSelectPop = YearSelectPopup(
                        requireContext(),
                        { selectYear, selectMonth ->
                            text = "$selectYear.$selectMonth"
                            onTabSelected.selected(selectYear, selectMonth)
                        },
                        showAllYear
                    )
                    XPopup.Builder(requireContext()) //.hasBlurBg(true)//模糊
                        .hasShadowBg(true)
                        .maxHeight(ViewGroup.LayoutParams.WRAP_CONTENT) //.isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                        .asCustom(yearSelectPop) /*.enableDrag(false)*/
                        .show()
                }
            }
        }

    }
}