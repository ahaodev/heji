package com.hao.heji.utils.excel

import com.blankj.utilcode.util.LogUtils
import com.hao.heji.utils.excel.entity.AliPayEntity
import jxl.Cell
import jxl.Workbook
import java.io.InputStream


internal class XLSFileReader : IReader {
    private val Tag = "XLSFileReader"

    override fun readAliPay(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {
        val book = Workbook.getWorkbook(inputStream)
        val sheet = book.getSheet(0)
        val rows = sheet.rows
        var call: Cell
        var startLine = 4
        for (row in startLine until rows) {
            call = sheet.getCell(0, row)
            LogUtils.d(call.contents)
            val alipay = AliPayEntity(
                transactionTime = sheet.getCell(0, row).contents,
                transactionCategory = sheet.getCell(1, row).contents,
                counterparty = sheet.getCell(2, row).contents,
                counterpartyAccount = sheet.getCell(3, row).contents,
                productName = sheet.getCell(4, row).contents,
                receiptOrExpenditure = sheet.getCell(5, row).contents,
                money = sheet.getCell(6, row).contents,
                paymentMethod = sheet.getCell(7, row).contents,
                tradingStatus = sheet.getCell(8, row).contents,
                transactionOrderNumber = sheet.getCell(9, row).contents,
                merchantOrderNumber = sheet.getCell(10, row).contents,
                remark = sheet.getCell(11, row).contents,
            )
        }
        result(true, "导入完成")
        book.close()
    }

    override fun readWeiXinPay(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {

    }

    override fun readQianJi(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {

    }
}