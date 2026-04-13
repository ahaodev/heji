package com.hao.heji.data

/**
 * 账本类型及其默认收支类别
 */
enum class BookType(
    val label: String,
    val expenditureCategories: List<String>,
    val incomeCategories: List<String>
) {
    DAILY(
        label = "日常生活",
        expenditureCategories = listOf(
            "旅行", "三餐", "衣服", "交通", "零食", "孩子", "话费网费",
            "学习", "日用品", "住房", "美妆", "医疗", "发红包", "电器数码",
            "运动", "水电煤", "娱乐"
        ),
        incomeCategories = listOf("工资", "生活费", "收红包", "外快", "股票基金", "其他")
    ),
    SOCIAL(
        label = "人情账本",
        expenditureCategories = listOf("发红包", "婚嫁随礼", "寿辰", "乔迁", "其他"),
        incomeCategories = listOf("收红包", "结婚收礼", "寿辰收礼", "乔迁收礼", "其他")
    ),
    BUSINESS(
        label = "经营账本",
        expenditureCategories = listOf(
            "员工工资", "进货", "其他材料", "水电费", "场地租金",
            "维修费", "清洁费", "物流费", "其他"
        ),
        incomeCategories = listOf("店铺收入", "物品转卖", "其他")
    ),
    CAR(
        label = "汽车账本",
        expenditureCategories = listOf("能源", "维修", "保险", "其他"),
        incomeCategories = listOf("货运", "其他")
    );

    companion object {
        val labels: Array<String> get() = entries.map { it.label }.toTypedArray()

        fun fromLabel(label: String): BookType? = entries.find { it.label == label }
    }
}
