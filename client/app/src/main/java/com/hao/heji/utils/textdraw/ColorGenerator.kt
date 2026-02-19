package com.hao.heji.utils.textdraw

import java.util.Random
import kotlin.math.abs

class ColorGenerator private constructor(private val mColors: List<Int>) {

    private val mRandom = Random(System.currentTimeMillis())

    fun getRandomColor(): Int = mColors[mRandom.nextInt(mColors.size)]

    fun getColor(key: Any): Int = mColors[abs(key.hashCode()) % mColors.size]

    companion object {
        @JvmField
        val DEFAULT: ColorGenerator = create(
            listOf(
                0xfff16364.toInt(),
                0xfff58559.toInt(),
                0xfff9a43e.toInt(),
                0xffe4c62e.toInt(),
                0xff67bf74.toInt(),
                0xff59a2be.toInt(),
                0xff2093cd.toInt(),
                0xffad62a7.toInt(),
                0xff805781.toInt()
            )
        )

        @JvmField
        val MATERIAL: ColorGenerator = create(
            listOf(
                0xffe57373.toInt(),
                0xfff06292.toInt(),
                0xffba68c8.toInt(),
                0xff9575cd.toInt(),
                0xff7986cb.toInt(),
                0xff64b5f6.toInt(),
                0xff4fc3f7.toInt(),
                0xff4dd0e1.toInt(),
                0xff4db6ac.toInt(),
                0xff81c784.toInt(),
                0xffaed581.toInt(),
                0xffff8a65.toInt(),
                0xffd4e157.toInt(),
                0xffffd54f.toInt(),
                0xffffb74d.toInt(),
                0xffa1887f.toInt(),
                0xff90a4ae.toInt()
            )
        )

        @JvmStatic
        fun create(colorList: List<Int>): ColorGenerator = ColorGenerator(colorList)
    }
}
