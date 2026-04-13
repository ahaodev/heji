package com.hao.heji.ui.create

import android.os.Parcelable
import com.hao.heji.data.db.Bill
import kotlinx.parcelize.Parcelize

/**
 * 账单操作参数类型
 * @date 2022/5/10
 * @author 锅得铁
 * @since v1.0
 * @param isModify 是否是修改 默认新增
 * @param bill 当没有Bill时传入一个新的Bill,在选择某日时代入日期
 */
@Parcelize
data class ArgAddBill(val isModify: Boolean = false, val bill: Bill?) : Parcelable
