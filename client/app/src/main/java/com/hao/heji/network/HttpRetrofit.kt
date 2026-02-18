package com.hao.heji.network

import com.hao.heji.BuildConfig
import com.hao.heji.json
import com.hao.heji.network.interceptor.AuthorizedInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


/**
 * @date: 2019-10-15
 * @author: 锅得铁
 * -Retrofit实例化
 */
object HttpRetrofit {


    /**
     * 初始化OkHttpClient
     * @param client
     */
    fun okHttpClient(
        connectTimeout: Long = 15L,
        readTimeout: Long = 120L,
        writeTimeout: Long = 120L
    ): OkHttpClient {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        if (!BuildConfig.DEBUG) {
            logging.redactHeader("Authorization")
        }

        val authorizedInterceptor = AuthorizedInterceptor()

        return OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .addInterceptor(authorizedInterceptor)
            .addInterceptor(logging)
            .build()
    }

    /**
     * 创建服务实例
     * @param url 服务地址
     * @param service 服务接口
     * @param <T>
     * @return 返回服务实例
    </T> */
    fun <T> create(url: String?, service: Class<T>?): T {
        val contentType = "application/json".toMediaType()
        val rt = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        return rt.create(service)
    }
}