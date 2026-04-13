package com.hao.heji.utils.excel

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import jxl.write.WritableWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class XLSFileWriter : IWriter {
    private val Tag = "XLSFileWriter"
    override fun writerHeji(list: MutableList<Any>, result: (Boolean, mgs: String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun writerQianJi(list: MutableList<Any>, result: (Boolean, mgs: String) -> Unit) {
        TODO("Not yet implemented")
    }

    /**
     * Write object list to excel
     * @param objects
     */
    fun <T> writeObjListToExcel(
        objects: List<T>,
        fileName: String,
        writeResult: (Boolean) -> Unit
    ) {
        if (objects.isEmpty()) {
            writeResult(false)
            val tip = "null list write fail"
            LogUtils.w(tip)
            ToastUtils.showLong(tip)
            return
        }
        val font = WritableFont(WritableFont.ARIAL, 12)
        val cellFormat = WritableCellFormat(font)
        cellFormat.setBorder(
            jxl.format.Border.ALL,
            jxl.format.BorderLineStyle.THIN
        )
        var writableWorkbook: WritableWorkbook? = null
        var inputStream: InputStream? = null
        try {
            val setEncode = WorkbookSettings()
            setEncode.encoding = "UTF-8"
            inputStream = FileInputStream(File(fileName))
            val workbook: Workbook = Workbook.getWorkbook(inputStream)
            writableWorkbook = Workbook.createWorkbook(File(fileName), workbook)
            val sheet = writableWorkbook.getSheet(0)
            for (obj in objects.indices) {
                val list = objects[obj] as ArrayList<String>
                for (i in 0 until list.size) {
                    sheet.addCell(Label(i, obj + 1, list[i], cellFormat))
                }
            }
            writableWorkbook.write()
            val tip = "导出到手机存储中文件夹Family成功"
            ToastUtils.showLong(tip)
            LogUtils.d(tip)
            writeResult(true)
        } catch (e: Exception) {
            e.printStackTrace()
            writeResult(false)
        } finally {
            try {
                writableWorkbook?.close()
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}