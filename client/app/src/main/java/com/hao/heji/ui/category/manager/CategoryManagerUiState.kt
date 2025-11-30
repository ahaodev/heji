package com.hao.heji.ui.category.manager

import com.hao.heji.data.db.Category
import com.hao.heji.ui.base.IUiState

/**
 *@date: 2022/10/15
 *Author: 锅得铁
 *#
 */

internal sealed interface CategoryManagerUiState : IUiState {
    class Categories(val data: MutableList<Category>) : CategoryManagerUiState
}