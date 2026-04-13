package com.hao.heji.utils

import android.text.TextUtils
import com.hao.heji.data.db.Image
import java.util.stream.Collectors

/**
 * @date: 2022/3/29
 * @author: 锅得铁
 * #
 */
object ImageUtils {
    /**
     * 获取图片路径，优先获取本地图片路径
     *
     * @param image
     * @return
     */
    fun getImagePath(image: Image): String? {
        val path: String?
        val isLocalFileExists = !TextUtils.isEmpty(image.localPath)
        path = if (isLocalFileExists) {
            image.localPath
        } else {
            image.onlinePath
        }
        return path
    }

    fun getPaths(images: List<Image>): List<Any?> {
        return images.stream().map { image: Image -> getImagePath(image) }
            .collect(
                Collectors.toList()
            )
    }
}