package com.hao.heji.network

import com.hao.heji.data.db.Book
import com.hao.heji.network.request.CategoryEntity
import com.hao.heji.network.response.ImageEntity
import com.hao.heji.ui.user.register.RegisterUser
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * @date: 2020/9/23
 * @author: 锅得铁
 * #
 */
interface ApiServer {
    //----------------------USER---------------------------//
    @POST("/api/v1/Register")
    fun register(@Body user: RegisterUser): Call<BaseResponse<String>>

    @POST("/api/v1/Login")
    fun login(@Body map: Map<String, String>): Call<BaseResponse<String>>

    @GET("/api/v1/mqtt/broker")
    fun getMqttBroker(): Call<BaseResponse<MqttBrokerInfo>>

    //----------------------BOOK---------------------------//
    @POST("/api/v1/book/")
    fun createBook(@Body book: Book): Call<BaseResponse<String>>

    @GET("/api/v1/book/{id}")
    fun findBook(@Path("id") bookId: String): Call<BaseResponse<Book>>

    @POST("/api/v1/book/{id}/share")
    fun sharedBook(@Path("id") bookId: String): Call<BaseResponse<String>>

    @POST("/api/v1/book/{id}/join")
    fun joinBook(@Path("id") bookId: String): Call<BaseResponse<String>>

    @PUT("/api/v1/book/{id}")
    fun updateBook(@Path("id") bookId: String,
                   @Body body: Map<String, String>): Call<BaseResponse<String>>

    @DELETE("/api/v1/book/{id}")
    fun deleteBook(@Path("id") bookId: String): Call<BaseResponse<String>>

    @GET("/api/v1/book/")
    fun bookList(): Call<BaseResponse<MutableList<Book>>>

    //----------------------BILL---------------------------//
    @Streaming
    @GET("/api/v1/bill/export")
    fun exportBills(@Query("book_id") bookId: String,
                    @Query("year") year: String?,
                    @Query("month") month: String?): Call<ResponseBody>

    //----------------------CATEGORY---------------------------//
    @POST("/api/v1/category/batch")
    fun addCategories(@Body categories: List<CategoryEntity>): Call<BaseResponse<String>>

    @POST("/api/v1/category/")
    fun addCategory(@Body category: CategoryEntity): Call<BaseResponse<String>>

    @DELETE("/api/v1/category/{id}")
    fun deleteCategoryById(@Path("id") id: String): Call<BaseResponse<String>>

    @GET("/api/v1/category/")
    fun getCategories(@Query("book_id") bookId: String): Call<BaseResponse<List<CategoryEntity>>>

    //----------------------FILE IMAGE---------------------------//
    @Multipart
    @POST("/api/v1/image/upload")
    fun uploadImg(@Part part: MultipartBody.Part,
                  @Query("_id") _id: String,
                  @Query("billId") billId: String): Call<BaseResponse<ImageEntity>>

    @Streaming
    @GET("/api/v1/image/list")
    fun getBillImages(@Query("bill_id") billId: String): Call<BaseResponse<List<ImageEntity>>>

    @Streaming
    @GET("/api/v1/image/{imageId}")
    fun getImage(@Path("imageId") id: String): Call<ResponseBody>

    @DELETE("/api/v1/image")
    fun imageDelete(@Query("billId") billId: String, @Query("imageId") imageId: String): Call<BaseResponse<String>>
}
