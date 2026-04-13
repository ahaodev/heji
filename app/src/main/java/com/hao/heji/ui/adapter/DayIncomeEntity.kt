package com.hao.heji.ui.adapter

import com.chad.library.adapter.base.entity.node.BaseNode
import com.hao.heji.data.db.Bill


/**
 * 日收益Object
 */
data class DayIncome(var expected: String,
                     var income: String,
                     var year: Int,
                     var month: Int,
                     var monthDay: Int,
                     var weekday: Any)


/**
 * 日收益Node,子节点为账单List
 */
data class DayIncomeNode(private val dayListNodes: MutableList<BaseNode>, val dayIncome: DayIncome) : BaseNode() {
    override val childNode: MutableList<BaseNode>
        get() = dayListNodes
}

/**
 * 当日账单List
 */
data class DayBillsNode(var bill: Bill) : BaseNode() {
    override val childNode: MutableList<BaseNode>?
        get() = null//没有子集
}
