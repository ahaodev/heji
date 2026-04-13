package com.hao.heji.data.db.dto

import androidx.room.Ignore
import java.math.BigDecimal

/**
 * 分类所占比例
 * 收入|支出|占比
 */
data class CategoryPercentage(
    var category: String?,
    var money: BigDecimal?,
    var percentage: Float
) {
    @Ignore
    constructor() : this(category = null, money = BigDecimal.ZERO, percentage = 0.00f)
}