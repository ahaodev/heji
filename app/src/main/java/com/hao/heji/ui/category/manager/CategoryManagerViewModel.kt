package com.hao.heji.ui.category.manager

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.config.Config
import com.hao.heji.data.Status
import com.hao.heji.data.db.Category
import com.hao.heji.data.repository.CategoryRepository
import com.hao.heji.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @date: 2020/10/11
 * @author: 锅得铁
 * # 分类
 */
internal class CategoryManagerViewModel(
    private val categoryRepository: CategoryRepository,
) :
    BaseViewModel<CategoryManagerUiState>() {

    fun getCategories(type: Int) {
        val book = Config.bookOrNull ?: return
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.observeIncomeOrExpenditure(book.id, type).collect {
                send(CategoryManagerUiState.Categories(it))
            }
        }
    }

    fun getParentCategories(type: Int) {
        val book = Config.bookOrNull ?: return
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.observeParentCategories(book.id, type).collect {
                send(CategoryManagerUiState.ParentCategories(it))
            }
        }
    }

    fun getChildCategories(parentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val children = categoryRepository.findChildCategories(parentId)
            send(CategoryManagerUiState.ChildCategories(parentId, children))
        }
    }

    fun saveCategory(name: String, type: Int, parentId: String? = null) {
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showShort("您必须填写分类名称")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val category = Category(name = name, bookId = Config.book.id).apply {
                    this.type = type
                    this.parentId = parentId
                    level = if (parentId.isNullOrEmpty()) 0 else 1
                }
                if (categoryRepository.exists(category)) {
                    ToastUtils.showShort("标签已经存在")
                } else {
                    categoryRepository.insert(category)
                    send(CategoryManagerUiState.SaveSuccess("保存成功", parentId))
                    ToastUtils.showShort("保存成功")
                }
            } catch (e: Throwable) {
                ToastUtils.showLong(e.message)
            }
        }
    }

    /**
     * 删除标签，通过Flow更新页面
     *
     * @param category
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                category.deleted = Status.DELETED
                categoryRepository.update(category)
            } catch (e: Throwable) {
                ToastUtils.showLong(e.message)
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoryRepository.update(category)
                ToastUtils.showShort("修改成功")
            } catch (e: Throwable) {
                ToastUtils.showLong(e.message)
            }
        }
    }

}
