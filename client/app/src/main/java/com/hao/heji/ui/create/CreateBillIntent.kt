package com.hao.heji.ui.create

import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Category
import com.hao.heji.data.db.Image
import com.hao.heji.ui.base.IUiState

/**
 * 创建账单页 UI 状态
 * @date 2022/8/26
 * @author 锅得铁
 */
internal sealed class CreateBillUIState : IUiState {
    data object Finish : CreateBillUIState()
    data object SaveAgain : CreateBillUIState()
    class BillChange(val bill: Bill) : CreateBillUIState()
    class Error(val throws: Throwable) : CreateBillUIState()
    class Images(val images: MutableList<Image>) : CreateBillUIState()
    class Categories(val type: Int, val categories: MutableList<Category>) : CreateBillUIState()
    class SubCategories(val type: Int, val parentId: String, val children: MutableList<Category>) : CreateBillUIState()
}