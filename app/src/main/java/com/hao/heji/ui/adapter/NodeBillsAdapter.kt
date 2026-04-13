package com.hao.heji.ui.adapter

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.module.LoadMoreModule

/**
 * @author hao
 */
class NodeBillsAdapter : BaseNodeAdapter() , LoadMoreModule {
    init {
        addNodeProvider(DayIncomeNodeProvider())
        addNodeProvider(DayBillsNodeProvider())
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        if (position < 0) return -1
        val node = data[position]
        if (node is DayIncomeNode) {
            return 0
        } else if (node is DayBillsNode) {
            return 1
        }
        return -1
    }
}