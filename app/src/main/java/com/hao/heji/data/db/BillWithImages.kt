package com.hao.heji.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class BillWithImages (@Embedded val bill: Bill,@Relation(parentColumn = Bill.COLUMN_ID, entityColumn = Image.COLUMN_ID) val images: List<Image>)