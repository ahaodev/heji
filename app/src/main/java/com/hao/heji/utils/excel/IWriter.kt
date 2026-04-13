package com.hao.heji.utils.excel

interface IWriter {
    fun writerHeji(list: MutableList<Any>, result: (Boolean, mgs: String) -> Unit)
    fun writerQianJi(list: MutableList<Any>, result: (Boolean, mgs: String) -> Unit)
}