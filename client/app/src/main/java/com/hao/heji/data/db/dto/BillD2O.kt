package com.hao.heji.data.db.dto

import java.math.BigDecimal

data class BillTotal(val money: BigDecimal, val time: String, val type: Int)

/**
 * 收入支出
 */
data class Income(
    var income: BigDecimal = BigDecimal.ZERO,
    var expenditure: BigDecimal = BigDecimal.ZERO,
)


/**
 * 收入支出在某时间
 */
data class IncomeTime(
    var income: BigDecimal = BigDecimal.ZERO,
    var expenditure: BigDecimal = BigDecimal.ZERO,
    var time: String?
)

/**
 * 收入|支出|结余|时间
 */
data class IncomeTimeSurplus(
    var income: BigDecimal = BigDecimal.ZERO,
    var expenditure: BigDecimal = BigDecimal.ZERO,
    var surplus: BigDecimal = BigDecimal.ZERO,
    var time: String?
)