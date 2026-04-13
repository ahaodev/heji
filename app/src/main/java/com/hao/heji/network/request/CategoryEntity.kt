package com.hao.heji.network.request

import com.hao.heji.data.db.Category
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @date: 2020/9/24
 * @author: 锅得铁
 * #标签
 */
@Serializable
data class CategoryEntity(@Transient val category: Category = Category(bookId = "")) {
    var id: String = category.id
    var bookId: String = category.bookId
    var name: String = category.name
    var type: Int = category.type
    var level: Int = category.level
    fun toDbCategory(): Category {
        return Category(bookId, name).apply {
            id = category.id
            level = category.level
            type = category.type
        }
    }

}