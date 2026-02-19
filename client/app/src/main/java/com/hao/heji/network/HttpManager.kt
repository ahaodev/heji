package com.hao.heji.network

import com.hao.heji.config.Config
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Book
import com.hao.heji.network.request.CategoryEntity
import com.hao.heji.ui.user.register.RegisterUser
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.await
import retrofit2.http.Part

class HttpManager {
    private var apiServer: ApiServer? = null

    fun server(): ApiServer {
        if (apiServer != null) {
            return apiServer as ApiServer
        }
        return HttpRetrofit.create(Config.serverUrl, ApiServer::class.java)
    }

    suspend fun register(registerUser: RegisterUser) =
        server().register(registerUser).await()

    suspend fun login(username: String, password: String) =
        server().login(mapOf("tel" to username, "password" to password))
            .await()

    suspend fun getMqttBroker() = server().getMqttBroker().await()

    suspend fun findBook(bid: String) = server().findBook(bid).await()
    suspend fun createBook(book: Book) = server().createBook(book).await()
    suspend fun bookList() = server().bookList().await()
    suspend fun sharedBook(bid: String) = server().sharedBook(bid).await()
    suspend fun deleteBook(bid: String) = server().deleteBook(bid).await()
    suspend fun updateBook(bid: String, bookName: String, bookType: String) =
        server().updateBook(bid, mapOf("name" to bookName, "type" to bookType)).await()

    suspend fun joinBook(code: String) = server().joinBook(code).await()

    // Bill CRUD
    suspend fun createBill(bill: Bill) = server().createBill(bill).await()
    suspend fun updateBill(billId: String, bill: Bill) = server().updateBill(billId, bill).await()
    suspend fun deleteBill(billId: String) = server().deleteBill(billId).await()

    // Sync
    suspend fun syncChanges(since: Long, limit: Int = 100) =
        server().syncChanges(since, limit).await()

    suspend fun imageUpload(
        @Part part: MultipartBody.Part,
        _id: String,
        _bid: String,
    ) = server().uploadImg(part, _id, _bid).await()

    suspend fun imageDownload(_id: String) = server().getBillImages(_id).await()
    suspend fun imageDelete(billId: String, imageId: String) =
        server().imageDelete(billId, imageId).await()

    suspend fun billExport(bookId: String, year: String = "0", month: String = "0"): Response<ResponseBody> =
        server().exportBills(bookId, year, month).execute()

    suspend fun categoryPush(category: CategoryEntity) = server().addCategory(category).await()
    suspend fun categoryDelete(_id: String) = server().deleteCategoryById(_id).await()
    suspend fun categoryPull(_id: String = "0") = server().getCategories(_id).await()

    fun redirectServer() {
        apiServer = HttpRetrofit.create(Config.serverUrl, ApiServer::class.java)
    }
}
