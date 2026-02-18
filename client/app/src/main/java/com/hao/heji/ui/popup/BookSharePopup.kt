package com.hao.heji.ui.popup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.hao.heji.R
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

/**
 * 邀请码 Popup
 */
class BookSharePopup(context: Context, val code: String) : BottomPopupView(context) {

    init {
        addInnerContent()
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_layout_share
    }

    override fun onCreate() {
        super.onCreate()
        val colorDrawable = XPopupUtils.createDrawable(
            ContextCompat.getColor(context, R.color._xpopup_light_color),
            10f,
            10f,
            0f,
            0f
        )
        popupImplView.apply {
            background = colorDrawable
            findViewById<TextView>(R.id.tvContext).text = code
            findViewById<ImageView>(R.id.ivQrCode).setImageBitmap(generateQrCode(code))
            setOnClickListener {
                ClipboardUtils.copyText(code)
                ToastUtils.showLong("邀请码已经复制到粘贴板")
            }
        }

    }

    private fun generateQrCode(content: String, size: Int = 512): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bitmap
    }
}