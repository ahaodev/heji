package com.hao.heji.utils

import android.Manifest
import android.os.StrictMode
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback
import com.permissionx.guolindev.request.ExplainScope
import com.permissionx.guolindev.request.ForwardScope
import com.hao.heji.BuildConfig

/**
 @date: 2021/7/14
 *@author: 锅得铁
 *#
 */
fun FragmentActivity.checkPermissions(
    activity: FragmentActivity,
    requestCallback: RequestCallback
) {
    PermissionX.init(activity).permissions(
        //Manifest.permission.READ_PHONE_STATE,
        //Manifest.permission.CAMERA,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    ).explainReasonBeforeRequest()
        .onExplainRequestReason { scope: ExplainScope, deniedList: List<String> ->
            scope.showRequestReasonDialog(
                deniedList,
                "为了正常使用你需要同意以下权限:",
                "我已明白"
            )
        }
        .onForwardToSettings { scope: ForwardScope, deniedList: List<String> ->
            scope.showForwardToSettingsDialog(
                deniedList,
                "您需要去应用程序设置当中手动开启权限",
                "我已明白"
            )
        }
        .request { allGranted: Boolean, grantedList: List<String>, deniedList: List<String> ->
            requestCallback.onResult(
                allGranted,
                grantedList,
                deniedList
            )
        }
}

fun FragmentActivity.permitDiskReads(func: () -> Any): Any {
    return if (BuildConfig.DEBUG) {
        val oldThreadPolicy = StrictMode.getThreadPolicy()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder(oldThreadPolicy)
                .permitDiskReads().build()
        )
        val anyValue = func()
        StrictMode.setThreadPolicy(oldThreadPolicy)

        anyValue
    } else {
        func()
    }
}