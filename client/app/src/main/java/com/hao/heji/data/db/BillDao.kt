package com.hao.heji.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import com.hao.heji.data.Status
import com.hao.heji.data.converters.MoneyConverters
import com.hao.heji.data.db.dto.BillTotal
import com.hao.heji.data.db.dto.CategoryPercentage
import com.hao.heji.data.db.dto.Income
import com.hao.heji.data.db.dto.IncomeTime
import com.hao.heji.data.db.dto.IncomeTimeSurplus
import kotlinx.coroutines.flow.Flow

/**
 * @date: 2020/8/28
 * @author: 锅得铁
 */
@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(billTab: Bill): Long

    @Query("SELECT COUNT(1) FROM bill WHERE hash =:hasCode")
    fun exist(hasCode: Int): Int

    /**
     * 当有子表时慎用
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(billTab: Bill): Long

    @Update(entity = Bill::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(bill: Bill): Int

    @Query("SELECT * FROM bill WHERE bill_id=:billId")
    fun findById(billId: String): Bill

    @Query("SELECT synced FROM bill WHERE bill_id=:billId")
    fun getSyncStatus(billId: String): Int

    @Query("UPDATE bill SET deleted =:deleted, synced = 0 WHERE bill_id=:billId AND crt_user=:uid")
    fun preDelete(billId: String, uid: String, deleted: Int = Status.DELETED): Int

    @Query("UPDATE bill SET synced = :status WHERE bill_id=:billId")
    fun updateSyncStatus(billId: String, status: Int): Int

    @Query("DELETE FROM bill WHERE bill_id=:billId")
    fun deleteById(billId: String): Int

    @Query("DELETE FROM bill WHERE book_id=:bookId")
    fun deleteByBookId(bookId: String): Int

    @Query("SELECT count(*)  FROM bill WHERE bill_id =:id")
    fun countById(id: String): Int

    @Query("SELECT count(*)  FROM bill WHERE book_id =:bookId")
    fun countByBookId(bookId: String): Int

    @Query("SELECT * FROM bill WHERE book_id=:bookId AND synced != 1 LIMIT 100")
    fun flowNotSynced(bookId: String): Flow<List<Bill>>

    /**
     * 按日期查询账单
     */
    @Query("SELECT * FROM bill WHERE date(time) =:time AND book_id=:bookId AND deleted!=1")
    fun findByDay(time: String, bookId: String): List<Bill>

    /**
     * 按日期+类型查询账单
     */
    @Query("SELECT * FROM bill WHERE strftime('%Y-%m-%d',time) ==:date AND book_id=:bookId AND deleted!=1 AND type=:type ORDER BY date(time)")
    fun findByDayAndType(
        date: String,
        type: Int,
        bookId: String,
    ): List<Bill>

    /**
     * 按月份查询账单
     */
    @Query("SELECT * FROM bill WHERE strftime('%Y-%m',time) ==:yearMonth AND book_id=:bookId AND type=:type AND deleted!=1")
    fun findByMonth(
        yearMonth: String,
        type: Int?,
        bookId: String,
    ): List<Bill>

    /**
     * 按分类和月份查询账单
     */
    @Query("SELECT * FROM bill WHERE strftime('%Y-%m',time) ==:yearMonth AND book_id=:bookId AND category=:category AND type =:type AND deleted!=1")
    fun findByCategoryAndMonth(
        category: String,
        yearMonth: String,
        type: Int,
        bookId: String,
    ): List<Bill>

    /**
     * 查询每日收支汇总（按月）
     */
    @TypeConverters(MoneyConverters::class)
    @Query("SELECT sum(case when type=-1 then money else 0 end)AS expenditure ,sum(case  when  type=1 then money else 0 end)AS income ,date(time) AS time FROM bill  WHERE deleted!=1 AND book_id=:bookId AND strftime('%Y-%m',time)=:yearMonth GROUP BY date(time) ORDER BY time DESC ,bill_id DESC")
    fun findEveryDayIncomeByMonth(
        bookId: String,
        yearMonth: String,
    ): List<IncomeTime>

    /**
     * 月度收支汇总（Flow）
     */
    @TypeConverters(MoneyConverters::class)
    @Query("SELECT sum(case when type=-1 then money else 0 end)AS expenditure ,sum(case  when  type=1 then money else 0 end)AS income FROM bill  WHERE deleted!=1 AND book_id=:bookId AND ( strftime('%Y-%m',time)=:yearMonth)")
    fun sumIncome(
        yearMonth: String,
        bookId: String,
    ): Flow<Income>

    /**
     * 月度收支汇总（同步）
     */
    @TypeConverters(MoneyConverters::class)
    @Query("SELECT sum(case when type=-1 then money else 0 end)AS expenditure ,sum(case  when  type=1 then money else 0 end)AS income FROM bill  WHERE deleted!=1 AND book_id=:bookId AND ( strftime('%Y-%m',time)=:yearMonth)")
    fun sumMonthIncome(
        yearMonth: String,
        bookId: String,
    ): Income

    /**
     * 按月按日汇总金额
     */
    @TypeConverters(MoneyConverters::class)
    @Transaction
    @Query("SELECT sum(money) AS money,type,date(time) AS time FROM bill WHERE strftime('%Y-%m',time) ==:yearMonth AND book_id=:bookId AND type=:type AND deleted!=1 GROUP by date(time)")
    fun sumByMonth(
        yearMonth: String,
        type: Int,
        bookId: String,
    ): List<BillTotal>

    //---------------统计----------------//

    /**
     * 月度每日收支结余
     */
    @TypeConverters(MoneyConverters::class)
    @Query(
        "SELECT strftime('%m-%d',time) AS time ," +
                "sum(case when type =-1 then money else 0 end) AS expenditure ," +
                " sum(case when type =1 then money else 0 end) AS income ," +
                " sum(case when type =1 then money else 0 end) - sum(case when type =-1 then money else 0 end) AS surplus" +
                " FROM bill WHERE strftime('%Y-%m',time) =:yearMonth AND book_id=:bookId AND deleted!=1 GROUP BY strftime('%Y-%m-%d',time)"
    )
    fun listIncomeExpSurplusByMonth(
        yearMonth: String,
        bookId: String,
    ): List<IncomeTimeSurplus>

    /**
     * 年度每月收支结余
     */
    @TypeConverters(MoneyConverters::class)
    @Query(
        "SELECT strftime('%Y-%m',time) AS time ," +
                " sum(case when type =-1 then money else 0 end) AS expenditure ," +
                " sum(case when type =1 then money else 0 end) AS income ," +
                " sum(case when type =1 then money else 0 end) - sum(case when type =-1 then money else 0 end) AS surplus" +
                " FROM bill WHERE strftime('%Y',time) =:year AND book_id=:bookId AND deleted!=1 GROUP BY strftime('%Y-%m',time)"
    )
    fun listIncomeExpSurplusByYear(
        year: String,
        bookId: String,
    ): List<IncomeTimeSurplus>

    /**
     * 按分类统计占比
     */
    @TypeConverters(MoneyConverters::class)
    @Query(
        "SELECT category AS category,sum(money)AS money," +
                "round(sum(money)*100.0 / (select sum(money)  from bill where book_id=:bookId and type =:type and deleted!=1 and strftime('%Y-%m',time) ==:yearMonth),2)AS percentage " +
                "FROM bill WHERE type =:type AND deleted!=1 AND book_id=:bookId AND strftime('%Y-%m',time) ==:yearMonth GROUP BY category ORDER BY money DESC"
    )
    fun reportCategory(
        type: Int,
        yearMonth: String,
        bookId: String,
    ): List<CategoryPercentage>

    @Delete
    fun delete(bill: Bill)
}
