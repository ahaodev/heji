package com.hao.heji.utils.excel

import com.blankj.utilcode.util.GsonUtils

/**
 * 支付宝下载的excel格式
 *  字段按照顺序排列
 */
data class AliPayEntity(
    // 交易号
    var transactionNumber: String = "",

    // 商家订单号
    var merchantOrderNumber: String = "",

    // 交易创建时间
    var transactionCreationTime: String = "",

    // 付款时间
    var paymentTime: String = "",

    // 最近修改时间
    var lastModifiedTime: String = "",

    // 交易来源地
    var transactionSource: String = "",

    // 类型
    var type: String = "",

    // 交易对方
    var counterparty: String = "",

    // 商品名称
    var productName: String = "",

    // 金额（元）
    var amount: String = "",

    // 收/支
    var receiptOrExpenditure: String = "",

    // 交易状态
    var tradingStatus: String = "",

    // 服务费（元）
    var serviceFee: String = "",

    // 成功退款（元）
    var successfulRefund: String = "",

    // 备注
    var remark: String = "",

    // 资金状态
    var fundStatus: String = ""
) {
    override fun toString(): String {
        return GsonUtils.toJson(this)
    }
}
