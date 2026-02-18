package com.hao.heji.utils.excel

object ReaderFactory {

    fun getReader(fileName: String): IReader? {
        val lowerName = fileName.lowercase()
        return when {
            lowerName.endsWith(".csv") -> CSVFileReader()
            lowerName.endsWith(".xlsx") -> XLSXFileReader()
            lowerName.endsWith(".xls") -> XLSFileReader()
            else -> null
        }
    }
}