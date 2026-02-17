package com.hao.heji.data.repository

import android.text.TextUtils
import com.hao.heji.App
import com.hao.heji.data.Result
import com.hao.heji.data.db.Category
import com.hao.heji.network.BaseResponse
import com.hao.heji.network.HttpManager
import com.hao.heji.network.request.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CategoryRepository(private val httpManager: HttpManager) {
    private val categoryDao get() = App.dataBase.categoryDao()
    suspend fun addCategory(category: CategoryEntity, bookId: String) {
        val response = httpManager.categoryPush(category)
        response.let {
            val dbCategory = category.toDbCategory()
            categoryDao.update(dbCategory)
        }
    }

    suspend fun deleteCategory(_id: String): Flow<Result<Boolean>> {
        val response = httpManager.categoryDelete(_id)
        response.let {
            categoryDao.deleteById(_id)
        }
        return flow { emit(Result.Success(true)) }
    }

    suspend fun getCategory() {
        val response: BaseResponse<List<CategoryEntity>> = httpManager.categoryPull()
       response.data?.let {
           it.forEach { entity: CategoryEntity ->
               val _id = App.dataBase.categoryDao().findByID(entity.id)
               if (TextUtils.isEmpty(_id)) {
                   val dbCategory = entity.toDbCategory()
                   dbCategory.synced = 1
                   App.dataBase.categoryDao().insert(dbCategory)
               }
           }
       }
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
        val response = httpManager.categoryUpdate()
        if (response.code == 0) {
            category.synced=1
            categoryDao.update(category)
        }
    }
}