package com.hao.heji.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hao.heji.data.AppDatabase
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Popup基础
 * @date 2022/8/27
 * @author 锅得铁
 * @since v1.0
 */
@RunWith(AndroidJUnit4::class)
class BillWithImageDaoTest : TestCase() {
    @Test
    fun findTest() {
        // Context of the app under test.
        var appContext = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            val mBill: Bill = AppDatabase.getInstance("1").billImageDao()
                .findBillAndImage("630a10e6e421d777c90fb723")
            Assert.assertEquals(3, mBill.images.size)
        }

    }
}