package com.hao.heji.ui.setting.export


/**
 * @date: 2020/12/5
 * @author: 锅得铁
 * #
 */
class QianJiExcel {
    //    时间	分类	类型	金额	账户1	账户2	备注	账单图片
    var time: String? = null
    var category: String? = null
    var type: String? = null
    var money: String? = null
    var account1: String? = null
    var account2: String? = null
    var remark: String? = null
    var urls: String? = null

    constructor(
        time: String?,
        category: String?,
        type: String?,
        money: String?,
        account1: String?,
        account2: String?,
        remark: String?,
        urls: String?
    ) {
        this.time = time
        this.category = category
        this.type = type
        this.money = money
        this.account1 = account1
        this.account2 = account2
        this.remark = remark
        this.urls = urls
    }

    constructor()

    override fun toString(): String {
        return "QianJiExcel{" +
                "time='" + time + '\'' +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", money='" + money + '\'' +
                ", account1='" + account1 + '\'' +
                ", account2='" + account2 + '\'' +
                ", remark='" + remark + '\'' +
                ", urls='" + urls + '\'' +
                '}'
    }
}
