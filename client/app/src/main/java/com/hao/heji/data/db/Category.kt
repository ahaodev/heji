package com.hao.heji.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hao.heji.data.BillType
import com.github.shamil.Xid
import kotlinx.serialization.Serializable
import java.util.*

/**
 * @date: 2020/8/28
 * @author: 锅得铁
 * #收入/支出 类型标签
 */
@Serializable
@Entity(tableName = "category")
data class Category(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    var id: String = Xid.string(),
    /**
     * 账本ID
     */
    @JvmField
    @ColumnInfo(name = "book_id")
    val bookId: String,

    @ColumnInfo(name = "name")
    var name: String = "其他",
    /**
     * 收入、支出
     */
    @ColumnInfo(name = "type")
    var type: Int = BillType.EXPENDITURE.valueInt
) {


    @ColumnInfo(name = "level")
    var level: Int = 0


    /**
     * 在账本下排序
     */
    @ColumnInfo(name = "index")
    var index: Int = 0

    /**
     * 父ID
     */
    @JvmField
    var parentId: String? = null

    /**
     * 用户ID
     */
    @JvmField
    @ColumnInfo(name = "create_user")
    var createUser: String? = null

    /**
     * 同步状态
     */
    @ColumnInfo(name = "synced", defaultValue = "0")
    var synced: Int =0

    @ColumnInfo(name = "deleted", defaultValue = "0")
    var deleted: Int =0

    /**
     * 是否在记账页面显示
     */
    @Ignore
    @ColumnInfo(name = "visibility", defaultValue = "1")
    var visibility: Int = 0

    @Ignore
    var isSelected: Boolean = false

    var hashValue = hashCode()

    override fun hashCode(): Int {
        return Objects.hash(bookId, name, type)
    }
}