package com.hao.heji.network

import com.hao.heji.data.db.Bill
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
    fun login(@Body map: Map<String,String>): Call<BaseResponse<String>>

    //----------------------BOOK---------------------------//
    @POST("/api/v1/CreateBook")
    fun createBook(@Body book: Book): Call<BaseResponse<String>>

    @POST("book/findBook")
    fun findBook(@Query("bookId") bookId: String): Call<BaseResponse<Book>>

    @POST("/api/v1/SharedBook/{book_id}")
    fun sharedBook(@Path("book_id") bookId: String): Call<BaseResponse<String>>

    @POST("/api/v1/JoinBook/{code}")
    fun joinBook(@Path("code") code: String): Call<BaseResponse<String>>

    @POST("/api/v1/UpdateBook")
    fun updateBook(@Query("bookId")  bookId:String,
                   @Query("bookName") bookName:String,
                   @Query("bookType")  bookType:String):Call<BaseResponse<String>>

    @POST("/api/v1/DeleteBook/{book_id}")
    fun deleteBook(@Path("book_id") book: String): Call<BaseResponse<String>>

    @POST("/api/v1/BookList")
    fun bookList(): Call<BaseResponse<MutableList<Book>>>

    @POST("bill/export")
    fun exportBills(@Query("year") year: String?, @Query("month") month: String?): Call<ResponseBody>
    //----------------------CATEGORY---------------------------//
    @POST("category/addCategories")
    fun addCategories(@Body categories: List<CategoryEntity>): Call<BaseResponse<String>>

    @POST("category/add")
    fun addCategory(@Body categories: CategoryEntity): Call<BaseResponse<String>>

    @DELETE("category/delete")
    fun deleteCategoryById(@Query("_id") _id: String): Call<BaseResponse<String>>

    @GET("category/getByBookId")//获取基础的类别
    fun getCategories(@Query("book_id") book_id: String): Call<BaseResponse<List<CategoryEntity>>>

    @GET("category/update")//获取基础的类别
    fun categoryUpdate(@Query("book_id") book_id: String): Call<BaseResponse<List<CategoryEntity>>>
    //----------------------FILE IMAGE---------------------------//
    /**
     * @param part
     * @param billId 账单ID
     * @return
     */
    @Multipart
    @POST("image/uploadImage")
    fun uploadImg(@Part part: MultipartBody.Part,
                  @Query("_id") _id: String,
                  @Query("billId") billId: String,
                  @Query("time") time: Long): Call<BaseResponse<ImageEntity>>

    /**
     * 多个文件上传
     *
     * @param parts
     * @return
     */
    @Multipart
    @POST("image/uploadFiles")
    fun uploadImgs(@Part parts: List<MultipartBody.Part>): Call<BaseResponse<List<String>>>

    @Streaming
    @GET("image/downloadFile/{fileName}")
    fun downloadFile(@Path("fileName") fileName: String): Call<ResponseBody>

    @Streaming
    @GET("image/getBillImages")
    fun getBillImages(@Query("bill_id") bill_id: String): Call<BaseResponse<List<ImageEntity>>>

    @Streaming
    @GET("image/{imageId}")
    fun getImage(@Path("imageId") _id: String): Call<ResponseBody>

    @Streaming
    @POST("image/delete")
    fun imageDelete(@Query("billId") billId: String,@Query("imageId") imageId :String): Call<BaseResponse<String>>
}