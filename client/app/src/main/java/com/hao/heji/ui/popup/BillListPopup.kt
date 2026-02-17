package com.hao.heji.ui.popup

import android.text.TextUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.lxj.xpopup.widget.VerticalRecyclerView
import com.hao.heji.ui.MainActivity
import com.hao.heji.R
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Image
import com.hao.heji.widget.CircleView

/**
 * 统计底部
 *@date: 2021/6/16
 *@author: 锅得铁
 *#
 */
class BillListPopup(
    val activity: MainActivity,
    layoutResId: Int = R.layout.item_bill_daylist,
    val itemClickListener: (Bill) -> Unit
) :
    BottomPopupView(activity) {

    init {
        addInnerContent()
    }

    companion object {
        fun create(
            activity: MainActivity, maxHeight: Int,
            itemClickListener: (Bill) -> Unit
        ): BillListPopup {
            return XPopup.Builder(activity)
                .hasNavigationBar(false)
                .maxHeight(maxHeight)
                .asCustom(BillListPopup(activity, itemClickListener = itemClickListener)) as BillListPopup
        }
    }

    private var adapter = ReportBillsAdapter(layoutResId, mutableListOf())
    private val recyclerView: VerticalRecyclerView by lazy { findViewById(R.id.recycler) }
    private val title: TextView by lazy { findViewById(R.id.tvTitle) }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_layout_bills
    }

    private lateinit var billInfoPop: PopupBillInfo

    override fun onCreate() {
        super.onCreate()
        LogUtils.d("${this.javaClass.name} create")
        //设置圆角背景
        popupImplView.background = XPopupUtils.createDrawable(
            ContextCompat.getColor(context, R.color._xpopup_light_color),
            popupInfo.borderRadius, popupInfo.borderRadius, 0f, 0f
        )
        adapter.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as Bill
            itemClickListener(item)
            billInfoPop = PopupBillInfo.create(activity = activity, delete = {

                adapter.removeAt(position)
                if (adapter.data.size <= 0) {
                    dismiss()
                } else {
                    adapter.notifyItemRemoved(position)
                }
            },
                update = {
                    this.dismiss()
                    this@BillListPopup.dismiss()
                })
            billInfoPop.show(bill = item)
        }
        recyclerView.adapter = adapter
    }

    fun show(label: String, bills: List<Bill>) {
        adapter.setNewInstance(bills.toMutableList())
        title.text = "${label}(${bills.size}条)"
        show()
    }

    fun setImages(images: MutableList<Image>) {
        if (this::billInfoPop.isInitialized) {
            billInfoPop.setImages(images)
        }
    }

    private class ReportBillsAdapter(layoutResId: Int, data: MutableList<Bill>) :
        BaseQuickAdapter<Bill, BaseViewHolder>(layoutResId, data) {
        override fun convert(holder: BaseViewHolder, item: Bill) {
            val bill = item
            val incomeColor =
                if (bill.type == -1) context.getColor(R.color.expenditure) else context.getColor(R.color.income)

            holder.getView<CircleView>(R.id.circleView).setColor(incomeColor)
            holder.setText(R.id.tvCategory, bill.category)
            holder.setText(R.id.tvMoney, "${if (bill.type == -1) "- " else "+ "}${bill.money}")
            holder.setTextColor(R.id.tvMoney, incomeColor)
            holder.setText(R.id.tvInfo, bill.remark)
            if (bill.images.isNotEmpty()) {
                holder.getView<TextView>(R.id.tvInfo).setCompoundDrawablesWithIntrinsicBounds(
                    context.getDrawable(R.drawable.ic_baseline_image_18),
                    null,
                    null,
                    null
                )
            } else {
                holder.getView<TextView>(R.id.tvInfo)
                    .setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
            holder.setGone(R.id.tvInfo, TextUtils.isEmpty(bill.remark) && bill.images.isEmpty())

        }

    }
}

