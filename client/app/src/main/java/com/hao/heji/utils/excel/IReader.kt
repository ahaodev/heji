package com.hao.heji.utils.excel

import java.io.InputStream

interface IReader {
    fun readAliPay(
        inputStream: InputStream, result: (Boolean, msg: String) -> Unit
    )

    fun readWeiXinPay(
        inputStream: InputStream, result: (Boolean, msg: String) -> Unit
    )

    fun readQianJi(
        inputStream: InputStream, result: (Boolean, msg: String) -> Unit
    )
}