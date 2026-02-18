package com.hao.heji.ui.setting

import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.utils.excel.ReaderFactory
import com.hao.heji.utils.launchIO
import java.io.InputStream

/**
 *
 * @date 2023/4/25
 * @author 锅得铁
 * @since v1.0
 */
class SettingViewModel : BaseViewModel<SettingUiState>() {
    fun inputAlipayData(fileName: String, inputStream: InputStream) {
        reading()
        launchIO({
            ReaderFactory.getReader(fileName)?.readAliPay(inputStream, result = { success, msg ->
                if (success) {
                    readEnd(msg)
                } else {
                    readError(msg)
                }
            }) ?: readError("不支持的文件格式: $fileName")
        }, {
            readError(it.message.toString())
        })
    }

    fun inputWeixinData(fileName: String, inputStream: InputStream) {
        reading()
        launchIO({
            ReaderFactory.getReader(fileName)?.readWeiXinPay(inputStream, result = { success, msg ->
                if (success) {
                    readEnd(msg)
                } else {
                    readError(msg)
                }
            }) ?: readError("不支持的文件格式: $fileName")
        }, { readError(it.message.toString()) })
    }

    private fun reading() {
        send(SettingUiState.InputReading("正在导入.."))
    }

    private fun readError(it: String) {
        send(SettingUiState.InputError("导入失败:${it}"))
    }

    private fun readEnd(msg: String = "导入完成") {
        send(SettingUiState.InputEnd(msg))
    }

}