package com.hao.heji.data.db

import androidx.room.*
import com.blankj.utilcode.util.GsonUtils
import com.github.shamil.Xid
import java.util.*

/**
 * @date: 2020/11/19
 * @author: 锅得铁
 * primaryKeys image_id
 * ForeignKey bill_id -> image.bill_id
 */
@Entity(
    tableName = Image.TAB_NAME,
    primaryKeys = [Image.COLUMN_ID],
    foreignKeys = [ForeignKey(
        entity = Bill::class,
        parentColumns = [Bill.COLUMN_ID],
        childColumns = [Image.COLUMN_BILL_ID],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION
    )], indices = [Index(value = [Image.COLUMN_ID, Image.COLUMN_BILL_ID], unique = true)]
)
data class Image(
    @ColumnInfo(name = COLUMN_ID)
    var id: String = Xid.string(),
    @ColumnInfo(name = Bill.COLUMN_ID, index = true)
    var billID: String
) {


    var md5: String? = null
    var ext: String? = null

    @ColumnInfo(name = COLUMN_PATH)
    var localPath: String? = null

    @ColumnInfo(name = COLUMN_ONLINE_PATH)
    var onlinePath: String? = null

    @ColumnInfo(name = "synced")
    var synced = 0

    @ColumnInfo(name = "deleted")
    var deleted = 0

    override fun hashCode(): Int {
        return Objects.hash(id, billID, localPath, onlinePath)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val image = o as Image
        return id === image.id && billID == image.billID &&
                localPath == image.localPath &&
                onlinePath == image.onlinePath
    }

    companion object {
        const val COLUMN_ID = "image_id"
        const val COLUMN_BILL_ID = Bill.COLUMN_ID
        const val TAB_NAME = "image"
        const val COLUMN_PATH = "local_path"
        const val COLUMN_ONLINE_PATH = "online_path"
    }

    override fun toString(): String {
        return GsonUtils.toJson(this)
    }
}