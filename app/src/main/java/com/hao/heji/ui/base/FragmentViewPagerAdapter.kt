package com.hao.heji.ui.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentViewPagerAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val fragmentList: List<Fragment>,
    val textList: List<String>
) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    fun getFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}