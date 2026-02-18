package com.hao.heji.utils.excel

import android.util.Log
import com.blankj.utilcode.util.TimeUtils
import com.github.shamil.Xid
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.BillType
import com.hao.heji.data.db.Bill
import com.hao.heji.utils.excel.entity.WeiXinPayEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal
import java.util.zip.ZipInputStream

/**
 * 读取 .xlsx 格式文件（无第三方依赖，手动解析ZIP+XML）
 */
internal class XLSXFileReader : IReader {
    private val TAG = "XLSXFileReader"

    override fun readAliPay(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {
        result(false, "支付宝账单请使用CSV格式导入")
    }

    /**
     * 微信支付账单 .xlsx 格式（第17行为标题行，第18行起为数据）:
     * 交易时间 | 交易类型 | 交易对方 | 商品 | 收/支 | 金额(元) | 支付方式 | 当前状态 | 交易单号 | 商户单号 | 备注
     */
    override fun readWeiXinPay(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {
        try {
            val xlsxData = parseXlsx(inputStream)
            val dataStartRow = 17 // 第18行 (0-based index = 17)

            var notIECount = 0
            var incomeCount = 0
            var expenditureCount = 0
            var zeroCount = 0
            var existCount = 0
            var inputCount = 0
            val startTime = System.currentTimeMillis()

            for (rowIndex in dataStartRow until xlsxData.size) {
                val columns = xlsxData[rowIndex]
                if (columns.size < 6) {
                    Log.d(TAG, "跳过列数不足的行: $rowIndex")
                    continue
                }

                val weiPay = WeiXinPayEntity(
                    transactionTime = columns.getOrElse(0) { "" }.trim(),
                    transactionType = columns.getOrElse(1) { "" }.trim(),
                    counterparty = columns.getOrElse(2) { "" }.trim(),
                    commodity = columns.getOrElse(3) { "" }.trim(),
                    receiptOrExpenditure = columns.getOrElse(4) { "" }.trim(),
                    money = columns.getOrElse(5) { "" }.trim(),
                    paymentMethod = columns.getOrElse(6) { "" }.trim(),
                    currentStatus = columns.getOrElse(7) { "" }.trim(),
                    transactionNumber = columns.getOrElse(8) { "" }.trim(),
                    merchantTrackingNumber = columns.getOrElse(9) { "" }.trim(),
                    remark = columns.getOrElse(10) { "" }.trim(),
                )
                Log.d(TAG, weiPay.toString())

                val weiPayType: Int = when (weiPay.receiptOrExpenditure) {
                    "支出" -> {
                        expenditureCount++
                        BillType.EXPENDITURE.value
                    }
                    "收入" -> {
                        incomeCount++
                        BillType.INCOME.value
                    }
                    else -> {
                        notIECount++
                        continue
                    }
                }

                try {
                    val moneyStr = weiPay.money.replace("¥", "").trim()
                    val bill = Bill().apply {
                        id = Xid.string()
                        bookId = Config.book.id
                        money = moneyStr.toBigDecimal()
                        type = weiPayType
                        time = TimeUtils.string2Date(weiPay.transactionTime, "yyyy-MM-dd HH:mm:ss")
                        category = weiPay.transactionType.ifEmpty { "微信" }
                        remark = "${weiPay.counterparty} ${weiPay.commodity}".trim()
                    }.also {
                        it.hashValue = it.hashCode()
                    }
                    if (bill.money.compareTo(BigDecimal.ZERO) == 0) {
                        zeroCount++
                        continue
                    }
                    App.dataBase.billDao().let {
                        val exist = it.exist(bill.hashCode()) > 0
                        if (!exist) {
                            it.insert(bill)
                            inputCount++
                        } else {
                            existCount++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Log.d(TAG, "不计入收支:$notIECount")
            Log.d(TAG, "收入:$incomeCount")
            Log.d(TAG, "支出:$expenditureCount")
            Log.d(TAG, "金额为0的:$zeroCount")
            Log.d(TAG, "重复导入:$existCount")
            Log.d(TAG, "完成导入:$inputCount")
            Log.d(TAG, "耗时:${System.currentTimeMillis() - startTime}毫秒")
            result(true, "导入完成：新增${inputCount}笔，重复${existCount}笔，不计收支${notIECount}笔")
        } catch (e: Exception) {
            e.printStackTrace()
            result(false, "${e.message}")
        }
    }

    override fun readQianJi(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {
    }

    /**
     * 解析xlsx文件（ZIP包含xl/sharedStrings.xml和xl/worksheets/sheet1.xml）
     * 返回二维字符串列表，每行为一个List<String>
     */
    private fun parseXlsx(inputStream: InputStream): List<List<String>> {
        val bytes = inputStream.readBytes()
        val sharedStrings = mutableListOf<String>()
        val rows = mutableListOf<List<String>>()

        // 第一遍：读取共享字符串表
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml") {
                    sharedStrings.addAll(parseSharedStrings(zis))
                    break
                }
                entry = zis.nextEntry
            }
        }

        // 第二遍：读取sheet1数据
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "xl/worksheets/sheet1.xml") {
                    rows.addAll(parseSheet(zis, sharedStrings))
                    break
                }
                entry = zis.nextEntry
            }
        }

        return rows
    }

    private fun parseSharedStrings(inputStream: InputStream): List<String> {
        val strings = mutableListOf<String>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var inT = false
        val sb = StringBuilder()
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") {
                        inT = true
                        sb.clear()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inT) sb.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        inT = false
                    }
                    if (parser.name == "si") {
                        strings.add(sb.toString())
                        sb.clear()
                    }
                }
            }
            eventType = parser.next()
        }
        return strings
    }

    private fun parseSheet(inputStream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var currentRow = mutableListOf<String>()
        var cellType: String? = null
        var cellRef: String? = null
        var cellValue = ""
        var inV = false
        var inRow = false

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            inRow = true
                            currentRow = mutableListOf()
                        }
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t")
                            cellRef = parser.getAttributeValue(null, "r")
                            cellValue = ""
                        }
                        "v" -> {
                            inV = true
                            cellValue = ""
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV) cellValue += parser.text
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> inV = false
                        "c" -> {
                            if (inRow) {
                                // 填充跳过的空列
                                val colIndex = cellRef?.let { columnIndex(it) } ?: currentRow.size
                                while (currentRow.size < colIndex) {
                                    currentRow.add("")
                                }
                                val value = if (cellType == "s") {
                                    val idx = cellValue.trim().toIntOrNull()
                                    if (idx != null && idx < sharedStrings.size) sharedStrings[idx] else cellValue
                                } else {
                                    cellValue
                                }
                                currentRow.add(value)
                            }
                        }
                        "row" -> {
                            inRow = false
                            rows.add(currentRow)
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    /** 从单元格引用(如"C18")中提取列索引(0-based) */
    private fun columnIndex(cellRef: String): Int {
        var col = 0
        for (ch in cellRef) {
            if (ch.isLetter()) {
                col = col * 26 + (ch.uppercaseChar() - 'A' + 1)
            } else {
                break
            }
        }
        return col - 1
    }
}
