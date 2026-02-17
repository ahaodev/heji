package com.hao.heji.ui.create

import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Category
import com.hao.heji.data.db.Image
import com.hao.heji.ui.base.IUiState

/**
 * Popup基础
 * @date 2022/8/26
 * @author 锅得铁
 * @since v1.0
 */
internal sealed class CreateBillUIState : IUiState {
//    class Remark(remark: String?) : AddBillUIState()
//    class Money(money: BigDecimal) : AddBillUIState()


//    class Time(time: Date) : AddBillUIState()

    data object Finish : CreateBillUIState()
    data object SaveAgain : CreateBillUIState()
    class BillChange(val bill: Bill) : CreateBillUIState()
    class Error(val throws: Throwable) : CreateBillUIState()
    class Images(val images: MutableList<Image>) : CreateBillUIState()
    class Categories(val type: Int, val categories: MutableList<Category>) : CreateBillUIState()
    class SubCategories(val type: Int, val parentId: String, val children: MutableList<Category>) : CreateBillUIState()
}