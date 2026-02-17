package com.hao.heji.ui.category.manager

import android.text.TextUtils
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.Status
import com.hao.heji.data.db.Category
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.utils.launchIO

/**
 * @date: 2020/10/11
 * @author: 锅得铁
 * # 分类
 */
internal class CategoryManagerViewModel :
    BaseViewModel<CategoryManagerUiState>() {
    private val categoryDao = App.dataBase.categoryDao()

    fun getCategories(type: Int) {
        launchIO({
            categoryDao.observeIncomeOrExpenditure(
                Config.book.id,
                type
            ).collect {
                send(CategoryManagerUiState.Categories(it))
            }
        })
    }

    fun getParentCategories(type: Int) {
        launchIO({
            categoryDao.observeParentCategories(
                Config.book.id,
                type
            ).collect {
                send(CategoryManagerUiState.ParentCategories(it))
            }
        })
    }

    fun getChildCategories(parentId: String) {
        launchIO({
            val children = categoryDao.findChildCategories(parentId)
            send(CategoryManagerUiState.ChildCategories(parentId, children))
        })
    }

    fun saveCategory(name: String, type: Int, parentId: String? = null) {
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showShort("您必须填写分类名称")
            return
        }
        launchIO({
            val category = Category(name = name, bookId = Config.book.id).apply {
                this.type = type
                this.parentId = parentId
                level = if (parentId.isNullOrEmpty()) 0 else 1
            }
            val exist = categoryDao.exist(category.hashCode())
            if (exist > 0) {
                ToastUtils.showShort("标签已经存在")
            } else {
                categoryDao.insert(category)
                send(CategoryManagerUiState.SaveSuccess("保存成功"))
                ToastUtils.showShort("保存成功")
            }
        }, {
            ToastUtils.showLong(it.message)
        })
    }

    /**
     * 删除标签，通过Flow更新页面
     *
     * @param category
     */
    fun deleteCategory(category: Category) {
        launchIO({
            category.deleted = Status.DELETED
            categoryDao.update(category)
        }, {
            ToastUtils.showLong(it.message)
        })
    }

    fun updateCategory(category: Category) {
        launchIO({
            categoryDao.update(category)
            ToastUtils.showShort("修改成功")
        }, {
            ToastUtils.showLong(it.message)
        })
    }

}