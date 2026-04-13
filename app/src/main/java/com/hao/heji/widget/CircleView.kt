package com.hao.heji.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * @date: 2021/3/2
 * @author: 锅得铁
 * # 圆 点
 */
class CircleView : View {
    private var mColor = Color.RED
    private var paint: Paint? = null
    constructor(paramContext: Context?) : super(paramContext)
    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?) : super(
        paramContext,
        paramAttributeSet
    )
    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?, paramInt: Int) : super(
        paramContext,
        paramAttributeSet,
        paramInt
    )
    override fun onDraw(paramCanvas: Canvas) {
        super.onDraw(paramCanvas)
        paint ?: Paint(1).apply {
            style = Paint.Style.FILL
            paint = this
        }
        paint!!.color=mColor
        val cx = (width / 2).toFloat()
        val cy = (height / 2).toFloat()
        val radius = (min(width, height) / 2).toFloat()
        paramCanvas.drawCircle(cx, cy, radius, paint!!)
    }

    override fun setBackground(paramDrawable: Drawable) {
        super.setBackground(null)
    }

    fun setColor(color: Int) {
        this.mColor = color
        invalidate()
    }
}