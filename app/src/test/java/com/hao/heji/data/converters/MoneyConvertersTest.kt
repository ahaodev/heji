package com.hao.heji.data.converters

import com.hao.heji.data.converters.MoneyConverters.ZERO_00
import com.hao.heji.data.converters.MoneyConverters.fromLong
import com.hao.heji.data.converters.MoneyConverters.toLong
import junit.framework.TestCase
import java.math.BigDecimal

/**
 * @date: 2021/6/17
 * @author: 锅得铁
 * #
 */
class MoneyConvertersTest : TestCase() {
    fun testFromLong() {
        val value = fromLong(1000L)
        TestCase.assertEquals(value.toString(), "10.00")

        val value2 = fromLong(1234L)
        TestCase.assertEquals(value2.toString(), "12.34")
    }

    fun testToLong() {
        val value = toLong(BigDecimal("10.00"))
        TestCase.assertEquals(value, 1000L)
        val value2 = toLong(BigDecimal("12.34"))
        TestCase.assertEquals(value2, 1234L)
    }

    fun testZERO_00() {
        TestCase.assertEquals("0.00", ZERO_00().toPlainString())
    }
}