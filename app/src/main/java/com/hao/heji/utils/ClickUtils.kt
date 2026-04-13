package com.hao.heji.utils

/**
 *
 * @date 2022/10/1
 * @author 锅得铁
 * @since v1.0
 */
object ClickUtils {
    private const val FAST_CLICK_DELAY_TIME = 1000
    private var lastClickTime = 0L

    fun debouncing(doAction: () -> Unit) {
        if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
            lastClickTime = System.currentTimeMillis()
            doAction()
        }
    }

}