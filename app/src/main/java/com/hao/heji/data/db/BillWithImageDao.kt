package com.hao.heji.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class BillWithImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBill(bill: Bill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertImages(images: MutableList<Image>)

    @Query("SELECT * FROM bill WHERE bill_id=:billId")
    abstract fun findBillById(billId: String): Bill

    @Query("SELECT * FROM image WHERE bill_id =:billId AND deleted !=1")
    abstract fun findImagesByBillId(billId: String): MutableList<Image>

    @Transaction
    open suspend fun installBillAndImage(bill: Bill, images: MutableList<Image>): Long {
        val count = insertBill(bill)
        if (images.isNotEmpty()) {
            insertImages(images)
        }
        return count
    }

    @Transaction
    open suspend fun findBillAndImage(bill_id: String): Bill {
        val bill = findBillById(bill_id)
        bill.images = findImagesByBillId(bill_id).map { it.id }.toMutableList()
        return bill
    }
}
