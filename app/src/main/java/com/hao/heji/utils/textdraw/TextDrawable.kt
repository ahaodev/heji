package com.hao.heji.utils.textdraw

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape

class TextDrawable private constructor(builder: Builder) : ShapeDrawable(builder.shape) {

    private val textPaint: Paint
    private val borderPaint: Paint
    private val text: String
    private val color: Int
    private val shape: RectShape
    private val height: Int
    private val width: Int
    private val fontSize: Int
    private val radius: Float
    private val borderThickness: Int

    init {
        shape = builder.shape
        height = builder.height
        width = builder.width
        radius = builder.radius

        text = if (builder.toUpperCase) builder.text.uppercase() else builder.text
        color = builder.color

        fontSize = builder.fontSize
        textPaint = Paint().apply {
            color = builder.textColor
            isAntiAlias = true
            isFakeBoldText = builder.isBold
            style = Paint.Style.FILL
            typeface = builder.font
            textAlign = Paint.Align.CENTER
            strokeWidth = builder.borderThickness.toFloat()
        }

        borderThickness = builder.borderThickness
        borderPaint = Paint().apply {
            color = getDarkerShade(this@TextDrawable.color)
            style = Paint.Style.STROKE
            strokeWidth = borderThickness.toFloat()
        }

        paint.color = color
    }

    private fun getDarkerShade(color: Int): Int {
        return Color.rgb(
            (SHADE_FACTOR * Color.red(color)).toInt(),
            (SHADE_FACTOR * Color.green(color)).toInt(),
            (SHADE_FACTOR * Color.blue(color)).toInt()
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val r = bounds

        if (borderThickness > 0) {
            drawBorder(canvas)
        }

        val count = canvas.save()
        canvas.translate(r.left.toFloat(), r.top.toFloat())

        val w = if (width < 0) r.width() else width
        val h = if (height < 0) r.height() else height
        val fs = if (fontSize < 0) (minOf(w, h) / 2) else fontSize
        textPaint.textSize = fs.toFloat()
        canvas.drawText(text, w / 2f, h / 2f - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)

        canvas.restoreToCount(count)
    }

    private fun drawBorder(canvas: Canvas) {
        val rect = RectF(bounds)
        rect.inset(borderThickness / 2f, borderThickness / 2f)

        when (shape) {
            is OvalShape -> canvas.drawOval(rect, borderPaint)
            is RoundRectShape -> canvas.drawRoundRect(rect, radius, radius, borderPaint)
            else -> canvas.drawRect(rect, borderPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        textPaint.colorFilter = cf
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = width

    override fun getIntrinsicHeight(): Int = height

    companion object {
        private const val SHADE_FACTOR = 0.9f

        @JvmStatic
        fun builder(): IShapeBuilder = Builder()
    }

    class Builder internal constructor() : IConfigBuilder, IShapeBuilder, IBuilder {
        var text: String = ""
        var color: Int = Color.GRAY
        var borderThickness: Int = 0
        var width: Int = -1
        var height: Int = -1
        var font: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        var shape: RectShape = RectShape()
        var textColor: Int = Color.WHITE
        var fontSize: Int = -1
        var isBold: Boolean = false
        var toUpperCase: Boolean = false
        var radius: Float = 0f

        override fun width(width: Int): IConfigBuilder {
            this.width = width
            return this
        }

        override fun height(height: Int): IConfigBuilder {
            this.height = height
            return this
        }

        override fun textColor(color: Int): IConfigBuilder {
            this.textColor = color
            return this
        }

        override fun withBorder(thickness: Int): IConfigBuilder {
            this.borderThickness = thickness
            return this
        }

        override fun useFont(font: Typeface): IConfigBuilder {
            this.font = font
            return this
        }

        override fun fontSize(size: Int): IConfigBuilder {
            this.fontSize = size
            return this
        }

        override fun bold(): IConfigBuilder {
            this.isBold = true
            return this
        }

        override fun toUpperCase(): IConfigBuilder {
            this.toUpperCase = true
            return this
        }

        override fun beginConfig(): IConfigBuilder = this

        override fun endConfig(): IShapeBuilder = this

        override fun rect(): IBuilder {
            this.shape = RectShape()
            return this
        }

        override fun round(): IBuilder {
            this.shape = OvalShape()
            return this
        }

        override fun roundRect(radius: Int): IBuilder {
            this.radius = radius.toFloat()
            val radii = floatArrayOf(
                radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(),
                radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat()
            )
            this.shape = RoundRectShape(radii, null, null)
            return this
        }

        override fun buildRect(text: String, color: Int): TextDrawable {
            rect()
            return build(text, color)
        }

        override fun buildRoundRect(text: String, color: Int, radius: Int): TextDrawable {
            roundRect(radius)
            return build(text, color)
        }

        override fun buildRound(text: String, color: Int): TextDrawable {
            round()
            return build(text, color)
        }

        override fun build(text: String, color: Int): TextDrawable {
            this.color = color
            this.text = text
            return TextDrawable(this)
        }
    }

    interface IConfigBuilder {
        fun width(width: Int): IConfigBuilder
        fun height(height: Int): IConfigBuilder
        fun textColor(color: Int): IConfigBuilder
        fun withBorder(thickness: Int): IConfigBuilder
        fun useFont(font: Typeface): IConfigBuilder
        fun fontSize(size: Int): IConfigBuilder
        fun bold(): IConfigBuilder
        fun toUpperCase(): IConfigBuilder
        fun endConfig(): IShapeBuilder
    }

    interface IBuilder {
        fun build(text: String, color: Int): TextDrawable
    }

    interface IShapeBuilder {
        fun beginConfig(): IConfigBuilder
        fun rect(): IBuilder
        fun round(): IBuilder
        fun roundRect(radius: Int): IBuilder
        fun buildRect(text: String, color: Int): TextDrawable
        fun buildRoundRect(text: String, color: Int, radius: Int): TextDrawable
        fun buildRound(text: String, color: Int): TextDrawable
    }
}
