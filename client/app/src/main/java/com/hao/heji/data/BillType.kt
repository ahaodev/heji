package com.hao.heji.data

/**
 * @date: 2020/11/20
 * @author: 锅得铁
 * #
 */
enum class BillType(val value: Int, val label: String) {
    INCOME(+1, "收入"), EXPENDITURE(-1, "支出"), ALL(0, "收支");

    companion object {
        fun fromValue(value: Int) = when (value) {
            INCOME.value -> INCOME
            EXPENDITURE.value -> EXPENDITURE
            else -> ALL
        }

        fun fromLabel(label: String) = when (label) {
            INCOME.label -> INCOME
            EXPENDITURE.label -> EXPENDITURE
            else -> ALL
        }
    }

    override fun toString(): String {
        return "BillType(name=$name,value=$value,label=$label,ordinal=$ordinal)"
    }
}