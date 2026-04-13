package com.hao.heji.utils.excel.entity

import com.blankj.utilcode.util.GsonUtils

class WeiXinPayEntity(

    //交易时间
    var transactionTime: String = "",

    //交易类型
    var transactionType: String = "",

    //交易对方
    var counterparty: String = "",

    //商品
    var commodity: String = "",

    //收/支
    var receiptOrExpenditure: String = "",

    //金额(元)
    var money: String = "",

    //支付方式
    var paymentMethod: String = "",

    //当前状态
    var currentStatus: String = "",

    //交易单号
    var transactionNumber: String = "",

    //商户单号
    var merchantTrackingNumber: String = "",

    //备注
    var remark: String = ""
) {
    override fun toString(): String {
        return GsonUtils.toJson(this)
    }
}