package com.hao.heji.widget

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import com.hao.heji.R

class MyMonthView(context: Context) : MonthView(context) {

    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val incomePaint = Paint()

    init {
        mRectPaint.style = Paint.Style.STROKE
        mRectPaint.strokeWidth = dipToPx(context, 0.5f).toFloat()
        mRectPaint.color = 0x88efefef.toInt()

        incomePaint.isAntiAlias = true
        incomePaint.style = Paint.Style.FILL
        incomePaint.textAlign = Paint.Align.CENTER
        incomePaint.isFakeBoldText = true
        incomePaint.textSize = dipToPx(getContext(), 9f).toFloat()
        incomePaint.color = context.getColor(R.color.income)

        setLayerType(View.LAYER_TYPE_SOFTWARE, incomePaint)
        mSelectedPaint.maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.SOLID)
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val oval = RectF(x.toFloat(), y.toFloat(), (x + mItemWidth).toFloat(), (y + mItemHeight).toFloat())
        canvas.drawRoundRect(oval, 20f, 20f, mSelectedPaint)
        return true
    }

    @Suppress("IntegerDivisionInFloatingPointContext")
    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        // no-op
    }

    @Suppress("IntegerDivisionInFloatingPointContext")
    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + mItemWidth).toFloat(), (y + mItemHeight).toFloat(), mRectPaint)
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 6

        val isInRange = isInRange(calendar)

        mCurDayTextPaint.color = context.getColor(R.color.colorPrimary)
        canvas.drawText(
            calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
            if (calendar.isCurrentDay) mCurDayTextPaint
            else if (calendar.isCurrentMonth && isInRange) mCurMonthTextPaint
            else mOtherMonthTextPaint
        )

        if (isSelected) {
            if (hasScheme) {
                drawScheme(canvas, calendar, y, cx, isSelected)
            } else {
                canvas.drawText(calendar.lunar, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, mSelectedLunarTextPaint)
            }
        } else if (hasScheme) {
            drawScheme(canvas, calendar, y, cx, isSelected)
        } else {
            canvas.drawText(
                calendar.lunar, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10,
                if (calendar.isCurrentDay && isInRange) mCurDayLunarTextPaint
                else if (calendar.isCurrentMonth) mCurMonthLunarTextPaint
                else mOtherMonthLunarTextPaint
            )
        }
    }

    private fun drawScheme(canvas: Canvas, calendar: Calendar, y: Int, cx: Int, isSelected: Boolean) {
        val space = dipToPx(context, 2f)
        var indexY = (mTextBaseLine + y + mItemHeight / 10).toInt()
        for (i in calendar.schemes.indices) {
            val scheme = calendar.schemes[i]
            if (scheme.obj == null) return
            if (isSelected) {
                incomePaint.color = mSelectTextPaint.color
            } else {
                incomePaint.color = scheme.shcemeColor
            }
            canvas.drawText(scheme.obj.toString(), cx.toFloat(), indexY.toFloat(), incomePaint)
            indexY = (indexY + incomePaint.textSize).toInt()
        }
    }

    companion object {
        private fun dipToPx(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}
