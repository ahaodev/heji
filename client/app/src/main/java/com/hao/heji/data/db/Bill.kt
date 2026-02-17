package com.hao.heji.data.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.TypeConverters
import com.blankj.utilcode.util.GsonUtils
import com.hao.heji.data.BillType
import com.hao.heji.data.Status
import com.hao.heji.data.converters.BigDecimalSerializer
import com.hao.heji.data.converters.DateConverters
import com.hao.heji.data.converters.DateSerializer
import com.hao.heji.data.converters.MoneyConverters
import com.github.shamil.Xid
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal
import java.util.Date

/**
 * @date: 2020/8/28
 * @author: 锅得铁
 * # onDelete ：当账本创建人删除了账本，联动删除账本账单
 * # onUpdate ：当账本更新，账单不联动
 */
@Parcelize
@Entity(
    tableName = Bill.TAB_NAME, primaryKeys = [Bill.COLUMN_ID], foreignKeys = [ForeignKey(
        entity = Book::class,
        parentColumns = [Book.COLUMN_ID],
        childColumns = [Bill.COLUMN_BOOK_ID],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )], indices = [Index(value = [Bill.COLUMN_ID, Bill.COLUMN_BOOK_ID], unique = true)]
)
@TypeConverters(DateConverters::class, MoneyConverters::class)
@Serializable
data class Bill(
    @SerialName("_id")
    @ColumnInfo(name = COLUMN_ID)
    var id: String = Xid.string(),

    @SerialName("book_id")
    @ColumnInfo(name = COLUMN_BOOK_ID, index = true)
    var bookId: String = "",
    /**
     * 钱
     */
    @SerialName("money")
    @Serializable(with = BigDecimalSerializer::class)
    @param:TypeConverters(MoneyConverters::class)
    var money: BigDecimal = MoneyConverters.ZERO_00(),

    /**
     * 收|支类型 s|z
     */
    @SerialName("type")
    var type: Int = BillType.EXPENDITURE.value,

    /**
     * 类别
     */
    @SerialName("category")
    var category: String? = null,

    /**
     * 账单时间-产生费用的日期-以这个为主
     */
    @SerialName("time")
    @Serializable(with = DateSerializer::class)
    @ColumnInfo(name = "time")
    var time: Date = Date(),

    /**
     * 更新时间
     */
    @SerialName("upd_time")
    @ColumnInfo(name = "upd_time")
    var updTime: Long? = 0,

    @SerialName("crt_user")
    @ColumnInfo(name = "crt_user")
    var crtUser: String = "",

    @SerialName("crt_time")
    @ColumnInfo(name = "crt_time")
    var crtTime: Long = System.currentTimeMillis(),

    /**
     * 备注
     */
    @SerialName("remark")
    var remark: String? = null,

    @Transient
    @Ignore
    @ColumnInfo(name = "images")
    var images: MutableList<String> = mutableListOf(),

    @ColumnInfo(name = "synced")
    var synced: Int = Status.NOT_SYNCED,

    @ColumnInfo(name = "deleted")
    var deleted: Int = Status.NOT_DELETED,
) : Parcelable {

    @Transient
    @ColumnInfo(name = "hash")
    var hashValue: Int = hashCode()

    @Ignore
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val bill = o as Bill
        return id == bill.id
    }

    @Ignore
    override fun hashCode(): Int {
        return "${bookId}${time}${money}".hashCode()
    }

    @Ignore
    override fun toString(): String {
        return GsonUtils.toJson(this)
    }

    companion object {
        const val TAB_NAME = "bill"
        const val COLUMN_ID = "bill_id"
        const val COLUMN_BOOK_ID = Book.COLUMN_ID
    }
}