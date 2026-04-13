package com.hao.heji.ui.create

import androidx.lifecycle.viewModelScope
import com.hao.heji.config.Config
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Image
import com.hao.heji.data.repository.BillRepository
import com.hao.heji.data.repository.CategoryRepository
import com.hao.heji.data.repository.ImageRepository
import com.github.shamil.Xid
import com.hao.heji.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import java.util.Stack

/**
 * 账单添加页ViewModel 不要在其他页面应用该ViewModel
 */
@PublishedApi
internal class CreateBillViewModel(
    private val billRepository: BillRepository,
    private val categoryRepository: CategoryRepository,
    private val imageRepository: ImageRepository,
) : BaseViewModel<CreateBillUIState>() {
    var keyBoardStack: Stack<String>? = null

    fun getCategories(type: Int) {
        val book = Config.bookOrNull ?: return
        val categories = categoryRepository.findParentCategories(book.id, type)
        send(CreateBillUIState.Categories(type, categories))
    }

    fun getChildCategories(type: Int, parentId: String) {
        val children = categoryRepository.findChildCategories(parentId)
        send(CreateBillUIState.SubCategories(type, parentId, children))
    }

    fun deleteImage(id: String) {
        imageRepository.preDelete(id)
    }

    fun getImages(ids: MutableList<String>) {
        val images = imageRepository.findImages(ids)
        send(CreateBillUIState.Images(images))
    }

    suspend fun getBill(it: String) {
        val bill = billRepository.findBillAndImage(it)
        send(CreateBillUIState.BillChange(bill = bill))
    }

    /**
     * 保存账单到本地
     * @param billId
     * @param money
     * @param billType
     * @return
     */
    fun save(bill: Bill, again: Boolean) {
        viewModelScope.launch {
            val images = mutableListOf<Image>()
            if (bill.images.isNotEmpty()) {
                val selectImages = bill.images.map { s: String? ->
                    val image = Image(Xid.string(), bill.id)
                    image.localPath = s
                    image
                }.toMutableList()
                images.addAll(selectImages)
            }
            billRepository.saveBill(bill, images)
            if (again) {
                send(CreateBillUIState.SaveAgain)
            } else {
                send(CreateBillUIState.Finish)
            }
        }

    }
}
