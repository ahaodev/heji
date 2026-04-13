package com.hao.heji.widget

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.chad.library.adapter.base.BaseNodeAdapter
import com.lxj.xpopup.util.XPopupUtils
import com.hao.heji.R
import com.hao.heji.ui.adapter.NodeBillsAdapter
import com.hao.heji.ui.adapter.TYPE_TITLE

/**
 * 账单按日期ItemDecoration
 */
class CardDecoration(val padding: Int = 8) : ItemDecoration() {
    private fun drawCardBackground(c: Canvas, parent: RecyclerView) {
        if (adapterEmpty(parent)) return
        val adapter = parent.adapter as BaseNodeAdapter
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i) // Item View
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.viewAdapterPosition
            //val currentItem = adapter.getItem(position)//当前的

            if (isLast(parent.adapter, position)) {//最后一个
                child.background = XPopupUtils.createDrawable(ContextCompat.getColor(parent.context, R.color._xpopup_light_color), 0f, 0f, 15f, 15f)
                return //可能会越界最后一个绘制完不再搜选下一个
            }
            //val nextItem = adapter.getItem(position + 1)//可能会越界
            when {
                adapter.getItemViewType(position) == TYPE_TITLE -> {
                    child.background = XPopupUtils.createDrawable(ContextCompat.getColor(parent.context, R.color._xpopup_light_color), 15f, 15f, 0f, 0f)
                }
                isItemLast(parent, position) -> {
                    child.background = XPopupUtils.createDrawable(ContextCompat.getColor(parent.context, R.color._xpopup_light_color), 0f, 0f, 15f, 15f)
                }
                else -> {
                    child.background =ColorDrawable(Color.WHITE)//每次度设置BG避免Item视图重用导致的圆角
                }
            }
        }
    }


    private fun adapterEmpty(parent: RecyclerView): Boolean {
        val adapter: NodeBillsAdapter = parent.adapter as NodeBillsAdapter
        if (adapter.data.isEmpty() || adapter.data.size <= 0) return true
        return false
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        //super.onDrawOver(c, parent, state);
        drawCardBackground(c, parent)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (adapterEmpty(parent)) return
        val resources = parent.context.resources
        val padding = getPadding(resources)
        val params = view.layoutParams as RecyclerView.LayoutParams
        val position = params.bindingAdapterPosition
        val viewType = parent.adapter!!.getItemViewType(position)
        if (viewType == TYPE_TITLE) {
            // header
            outRect[0, (padding / 2), 0] = 0
        } else {
            if (isItemLast(parent, position) || isLast(parent.adapter, position)) {
                // last item before next header
                outRect[0, 0, 0] = (padding / 2)
            }
        }
        //        outRect.inset((int) size16dp, 0);
        outRect.left = padding
        outRect.right = padding
        if (isLast(parent.adapter, position)) { //最后一个
            outRect.bottom = padding / 2
        }
    }


    private fun isItemLast(parent: RecyclerView, position: Int): Boolean {
        return parent.adapter!!.getItemViewType(position + 1) == TYPE_TITLE
    }
    private fun isLast(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?, position: Int) =
            (position == adapter!!.itemCount - 1)

    private fun getPadding(resources: Resources): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  padding * 1f, resources.displayMetrics).toInt()
    }
}