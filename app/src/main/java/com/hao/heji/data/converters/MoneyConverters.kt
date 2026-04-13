package com.hao.heji.data.converters

import androidx.room.Ignore
import androidx.room.TypeConverter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @date: 2020/9/20
 * @author: 锅得铁
 * #货币转换
 */
object MoneyConverters {
    @JvmStatic
    @TypeConverter
    fun fromLong(value: Long?): BigDecimal {
        return if (value == null) ZERO_00() else BigDecimal(value).divide(
            BigDecimal(100),
            2,
            RoundingMode.DOWN
        )
    }

    @JvmStatic
    @TypeConverter
    fun toLong(bigDecimal: BigDecimal): Long {
        return bigDecimal.multiply(BigDecimal(100)).toLong()
    }

    @JvmStatic
    @Ignore
    fun ZERO_00(): BigDecimal {
        return BigDecimal.ZERO.divide(BigDecimal.ONE, 2,  RoundingMode.DOWN)
    }

    @JvmStatic
    fun toString(bigDecimal: BigDecimal): String {
        return bigDecimal.multiply(BigDecimal(100)).toPlainString()
    }

}