package com.hao.heji.ui

import android.util.DisplayMetrics
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.hao.heji.ui.MainActivity

class DrawerListener(private val mainActivity: MainActivity, val offset:(left:Int, top:Int, right:Int, bottom:Int) -> Unit ) :
    DrawerListener {

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        val displayMetrics = DisplayMetrics()
        mainActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        offset(
                drawerView.right,//drawerView右边缘
                drawerView.top,//顶部 0
                (displayMetrics.widthPixels + displayMetrics.widthPixels * slideOffset).toInt(),//drawerView右边缘+屏幕宽度
                drawerView.bottom)//屏幕高度
    }

    override fun onDrawerOpened(drawerView: View) {}
    override fun onDrawerClosed(drawerView: View) {}
    override fun onDrawerStateChanged(newState: Int) {}
}