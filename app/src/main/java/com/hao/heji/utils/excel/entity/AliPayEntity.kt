package com.hao.heji.utils.excel.entity

import com.blankj.utilcode.util.GsonUtils

/**
 * 支付宝导出的CSV格式
 * 字段按照顺序排列：交易时间,交易分类,交易对方,对方账号,商品说明,收/支,金额,收/付款方式,交易状态,交易订单号,商家订单号,备注
 */
data class AliPayEntity(
    // 交易时间
    var transactionTime: String = "",

    // 交易分类
    var transactionCategory: String = "",

    // 交易对方
    var counterparty: String = "",

    // 对方账号
    var counterpartyAccount: String = "",

    // 商品说明
    var productName: String = "",

    // 收/支
    var receiptOrExpenditure: String = "",

    // 金额
    var money: String = "",

    // 收/付款方式
    var paymentMethod: String = "",

    // 交易状态
    var tradingStatus: String = "",

    // 交易订单号
    var transactionOrderNumber: String = "",

    // 商家订单号
    var merchantOrderNumber: String = "",

    // 备注
    var remark: String = ""
) {
    override fun toString(): String {
        return GsonUtils.toJson(this)
    }
}
