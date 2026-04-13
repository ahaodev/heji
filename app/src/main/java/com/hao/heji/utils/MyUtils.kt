package com.hao.heji.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.CrashUtils
import java.io.File

object MyUtils {
    /**
     * 把文件公开。添加到扫描中
     *
     * @param currentPhotoPath
     */
    fun galleryAddPic(context: Context, currentPhotoPath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(currentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    private fun storage(context: Context, path: String?): String {
        val headDir = context.getExternalFilesDir(path)
        if (!headDir!!.exists()) headDir.mkdir()
        return headDir.path
    }

    fun initCrashTool(context: Context, listener: CrashUtils.OnCrashListener) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val storage = storage(context, "Crash")
            CrashUtils.init(storage, listener)
        }
    }



}