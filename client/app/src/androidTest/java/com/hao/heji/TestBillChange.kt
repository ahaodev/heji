package com.hao.heji

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hao.heji.data.db.Bill
import com.github.shamil.Xid
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

/**
 * 测试账单变更
 * @date 2022/5/12
 * @author 锅得铁
 * @since v1.0
 */
@RunWith(AndroidJUnit4::class)
class TestBillChange {
    @Test
    fun billChange() {

        var bill = Bill().apply {
            money = BigDecimal.ONE
            type = 1
        }

        val newID = Xid.string()

        bill.apply {
            id = newID
            money = BigDecimal.ZERO
        }
        TestCase.assertEquals(BigDecimal.ZERO, bill.money)
        TestCase.assertEquals(newID, bill.id)
    }
}