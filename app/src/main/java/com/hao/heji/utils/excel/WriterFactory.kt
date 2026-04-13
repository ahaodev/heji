package com.hao.heji.utils.excel

class WriterFactory {
    fun getWriter(fileName: String): IReader? {
        val suffix = fileName.split(".")
        if (suffix[1] == ("xls")) {
            return XLSFileReader()
        }
        if (suffix[1] == "csv") {
            return CSVFileReader()
        }
        return null
    }
}