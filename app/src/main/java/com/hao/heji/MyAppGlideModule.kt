package com.hao.heji

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.hao.heji.network.HttpRetrofit
import java.io.File
import java.io.InputStream

@GlideModule
class MyAppGlideModule : AppGlideModule() {

    companion object {
        @JvmStatic
        fun loadImageFile(context: Context, pathName: String, imgView: ImageView) {
            val file = File(pathName)
            Glide.with(context).load(file).into(imgView)
        }

        @JvmStatic
        fun loadImageRes(context: Context, resource: Int, imgView: ImageView) {
            Glide.with(context).load(resource).into(imgView)
        }

        @JvmStatic
        fun loadImageBytes(context: Context, image: ByteArray, imgView: ImageView) {
            Glide.with(context).load(image).into(imgView)
        }

        @JvmStatic
        fun loadImageUri(context: Context, imageUri: Uri, imgView: ImageView) {
            Glide.with(context).load(imageUri).into(imgView)
        }

        @JvmStatic
        fun loadImageUrl(context: Context, imageUri: String, imgView: ImageView) {
            Glide.with(context).load(imageUri).into(imgView)
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val builder = HttpRetrofit.okHttpClient(15, 15, 15).newBuilder()
        registry.append(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(builder.build()))
    }
}
