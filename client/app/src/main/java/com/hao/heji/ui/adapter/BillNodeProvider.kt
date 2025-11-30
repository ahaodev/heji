package com.hao.heji.ui.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hao.heji.R
import com.hao.heji.widget.CircleView
import com.squareup.moshi.Moshi

class DayIncomeNodeProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = TYPE_TITLE

    override val layoutId: Int
        get() = R.layout.item_bill_dayincom

    override fun convert(helper: BaseViewHolder, item: BaseNode) {Moshi.Builder()
        // 数据类型需要自己强转
        val entity: DayIncomeNode = item as DayIncomeNode
        helper.setText(R.id.text1, "${entity.dayIncome.month}月${entity.dayIncome.monthDay}日")

        if (entity.dayIncome.weekday is String) {
            helper.setText(R.id.text2, entity.dayIncome.weekday as String)
        } else if (entity.dayIncome.weekday is Int) {
            helper.setText(R.id.text2, context.resources.getStringArray(R.array.week_day_names)[entity.dayIncome.weekday as Int])
        }

        helper.setText(R.id.text3, "支:${entity.dayIncome.expected}")
        helper.setText(R.id.text4, "收:${entity.dayIncome.income}")
    }

}

class DayBillsNodeProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = TYPE_INFO
    override val layoutId: Int
        get() = R.layout.item_bill_daylist

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val entity = item as DayBillsNode
        val bill = entity.bill
        val incomeColor = context.getColor(if (bill.type == -1) R.color.expenditure else R.color.income)
        with(helper) {
            getView<CircleView>(R.id.circleView).setColor(incomeColor)
            setText(R.id.tvCategory, bill.category)
            val moneySign = if (bill.type == -1) "- " else "+ "
            setText(R.id.tvMoney, "$moneySign${bill.money}")
            setTextColor(R.id.tvMoney, incomeColor)
            setText(R.id.tvInfo, bill.remark)
            val tvInfoView = getView<TextView>(R.id.tvInfo)
            tvInfoView.setCompoundDrawablesWithIntrinsicBounds(
                if (bill.images.isNotEmpty()) context.getDrawable(R.drawable.ic_baseline_image_18) else null,
                null,
                null,
                null
            )
            setGone(R.id.tvInfo, TextUtils.isEmpty(bill.remark) && bill.images.isEmpty())
        }
    }
}

const val TYPE_TITLE = 0
const val TYPE_INFO = 1