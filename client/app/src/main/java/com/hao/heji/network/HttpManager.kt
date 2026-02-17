package com.hao.heji.network

import com.hao.heji.config.Config
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Book
import com.hao.heji.network.request.CategoryEntity
import com.hao.heji.ui.user.register.RegisterUser
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import retrofit2.http.Part
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

    suspend fun findBook(bid: String) = server().findBook(bid).await()
    suspend fun createBook(book: Book) = server().createBook(book).await()
    suspend fun bookList() = server().bookList().await()
    suspend fun sharedBook(bid: String) = server().sharedBook(bid).await()
    suspend fun deleteBook(bid: String) = server().deleteBook(bid).await()
    suspend fun updateBook(bid: String, bookName: String, bookType: String) =
        server().updateBook(bid, bookName, bookType).await()

    suspend fun joinBook(code: String) = server().joinBook(code).await()

    suspend fun imageUpload(
        @Part part: MultipartBody.Part,
        _id: String,
        _bid: String,
        time: Long,
    ) = server().uploadImg(part, _id, _bid, time).await()

    suspend fun imageDownload(_id: String) = server().getBillImages(_id).await()
    suspend fun imageDelete(billId: String, imageId: String) =
        server().imageDelete(billId, imageId).await()

    suspend fun billExport(year: String = "0", month: String = "0"): Response<ResponseBody> =
        server().exportBills(year, month).execute()

    suspend fun categoryPush(category: CategoryEntity) = server().addCategory(category).await()
    suspend fun categoryDelete(_id: String) = server().deleteCategoryById(_id).await()
    suspend fun categoryPull(_id: String = "0") = server().getCategories(_id).await()
    suspend fun categoryUpdate(_id: String = "0") = server().getCategories(_id).await()


    suspend fun <T> Call<T>.await(): T {
        //调用suspendCoroutine 挂起
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)//异常恢复
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    val errorBody = response.errorBody()
                    when {
                        body != null && response.code() == 200 -> {
                            continuation.resume(body)//正常恢复
                        }

                        errorBody != null -> {//error body 仅仅适用于服务器统一返回的错误格式，在服务端错误信息同样返回{code,msg,data}格式
                            continuation.resumeWithException(RuntimeException(errorBody.string()))
                        }

                        else -> {
                            continuation.resumeWithException(RuntimeException("response body is null"))
                        }
                    }
                }
            })
        }
    }

    fun redirectServer() {
        apiServer = HttpRetrofit.create(Config.serverUrl, ApiServer::class.java)
    }
}
