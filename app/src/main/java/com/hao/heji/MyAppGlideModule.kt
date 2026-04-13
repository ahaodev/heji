package com.hao.heji;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.hao.heji.network.HttpRetrofit;

import java.io.File;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    /**
     * @param pathName getExternalCacheDir() + "/image.jpg"
     */
    public static void loadImageFile(Context context, String pathName, ImageView imgView) {
        // 加载本地图片
        File file = new File(pathName);
        Glide.with(App.Companion.getContext()).load(file).into(imgView);
    }

    public static void loadImageRes(Context context, int resource, ImageView imgView) {
        // 加载应用资源
        Glide.with(context).load(resource).into(imgView);
    }

    public static void loadImageBytes(Context context, byte[] image, ImageView imgView) {
        // 加载二进制流
        Glide.with(context).load(image).into(imgView);
    }

    public static void loadImageUri(Context context, Uri imageUri, ImageView imgView) {
        // 加载Uri对象
        Glide.with(context).load(imageUri).into(imgView);
    }

    public static void loadImageUrl(Context context, String imageUri, ImageView imgView) {
        // 加载Uri对象
        Glide.with(context).load(imageUri).into(imgView);
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        final OkHttpClient.Builder builder = HttpRetrofit.INSTANCE.okHttpClient(15, 15, 15).newBuilder();
        registry.append(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory) builder.build()));
    }

}
