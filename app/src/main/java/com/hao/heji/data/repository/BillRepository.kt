package com.hao.heji.data.repository

import com.hao.heji.App
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Image

class BillRepository {
    private val billDao get() = App.dataBase.billDao()
    private val billImageDao get() = App.dataBase.billImageDao()

    suspend fun saveBill(bill: Bill, images: MutableList<Image>) {
        if (images.isEmpty()) {
            billDao.insert(bill)
            return
        }
        billImageDao.installBillAndImage(bill, images)
    }

    suspend fun findBillAndImage(billId: String): Bill = billImageDao.findBillAndImage(billId)

    fun deleteByBookId(bookId: String): Int = billDao.deleteByBookId(bookId)

    fun countByBookId(bookId: String): Int = billDao.countByBookId(bookId)

    fun findEveryDayIncomeByMonth(bookId: String, yearMonth: String) =
        billDao.findEveryDayIncomeByMonth(bookId, yearMonth)

    fun findByDay(time: String, bookId: String) = billDao.findByDay(time, bookId)

    fun sumIncome(yearMonth: String, bookId: String) = billDao.sumIncome(yearMonth, bookId)
}
