package com.hao.heji.ui.create

import androidx.lifecycle.*
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.db.*
import com.github.shamil.Xid
import com.hao.heji.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import java.util.*

/**
 * 账单添加页ViewModel 不要在其他页面应用该ViewModel
 */
@PublishedApi
internal class CreateBillViewModel :
    BaseViewModel<CreateBillUIState>() {

    var keyBoardStack: Stack<String>? = null//用于保存栈
    private fun error(it: Throwable) {
        send(CreateBillUIState.Error(it))
        ToastUtils.showLong(it.message)
    }

    fun getCategories(type: Int) {
        LogUtils.d(
            "TimeTest",
            TimeUtils.millis2String(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss")
        )
        val categories = App.dataBase.categoryDao()
            .findParentCategories(Config.book.id, type)
        send(CreateBillUIState.Categories(type, categories))
        LogUtils.d(
            "TimeTest",
            categories,
            TimeUtils.millis2String(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss")
        )
    }

    fun getChildCategories(type: Int, parentId: String) {
        val children = App.dataBase.categoryDao().findChildCategories(parentId)
        send(CreateBillUIState.SubCategories(type, parentId, children))
    }

    fun deleteImage(id: String) {
        App.dataBase.imageDao().preDelete(id)
    }

    fun getImages(ids: MutableList<String>) {
        val images = App.dataBase.imageDao().findImage(ids)
        send(CreateBillUIState.Images(images))
    }

    suspend fun getBill(it: String) {
        val bill = App.dataBase.billImageDao().findBillAndImage(it)
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
                var count: Long =
                    App.dataBase.billImageDao().installBillAndImage(bill, images)
            } else {
                App.dataBase.billDao().insert(bill)
            }
            if (again) {
                send(CreateBillUIState.SaveAgain)
            } else {
                send(CreateBillUIState.Finish)
            }
        }

    }
}