package com.hao.heji.utils

import android.graphics.Color
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

/**
 * @date: 2020/9/16
 * @author: 锅得铁
 * #
 */
object ColorUtils {
    fun randomColor(): Int {
        //随机种子
        val mRandom = Random(System.currentTimeMillis())
        val baseRed = Color.red(randomBaseColor())
        val baseGreen = Color.green(randomBaseColor())
        val baseBlue = Color.blue(randomBaseColor())
        val red = (baseRed + mRandom.nextInt(256)) / 2
        val green = (baseGreen + mRandom.nextInt(256)) / 2
        val blue = (baseBlue + mRandom.nextInt(256)) / 2
        return Color.rgb(red, green, blue)
    }

    fun randomBaseColor(): Int {
        //随机种子
        val mRandom = Random(System.currentTimeMillis())
        val baseColos = intArrayOf(
            Color.BLUE,
            Color.RED,
            Color.YELLOW,
            Color.YELLOW,
            Color.GREEN
        )
        // 基础色
        return baseColos[mRandom.nextInt(baseColos.size)]
    }

    /**
     * 25个
     */
    fun groupColors(): ArrayList<Int> {
        // add a lot of colors
        val colors = ArrayList<Int>()

        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        return colors
    }
}