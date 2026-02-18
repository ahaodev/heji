package com.hao.heji.utils.excel

import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.opencsv.CSVReader
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.BillType
import com.hao.heji.data.db.Bill
import com.github.shamil.Xid
import com.hao.heji.utils.excel.entity.AliPayEntity
import com.hao.heji.utils.excel.entity.WeiXinPayEntity
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal


internal class CSVFileReader : IReader {
    private val TAG = "CSVFileReader"

    /**
     * 支付宝CSV格式(12列):
     * 交易时间,交易分类,交易对方,对方账号,商品说明,收/支,金额,收/付款方式,交易状态,交易订单号,商家订单号,备注
     */
    override fun readAliPay(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {
        try {
            //支付宝导出来的是gbk格式编码
            val inputStreamReader = InputStreamReader(inputStream, "GBK")
            val reader = CSVReader(inputStreamReader)
            var nextLine: Array<String>?
            var notIECount = 0
            var incomeCount = 0
            var expenditureCount = 0
            var zeroCount = 0
            var existCount = 0
            var inputCount = 0
            val startTime = System.currentTimeMillis()
            while (reader.readNext().also { nextLine = it } != null) {
                val columns = nextLine!!
                if (columns.size < 7) {
                    Log.d(TAG, columns.contentToString())
                    continue
                }
                // 跳过标题行
                if (columns[0].trim() == "交易时间") continue

                val aliPay = AliPayEntity(
                    transactionTime = columns[0].trim(),
                    transactionCategory = columns[1].trim(),
                    counterparty = columns[2].trim(),
                    counterpartyAccount = columns[3].trim(),
                    productName = columns[4].trim(),
                    receiptOrExpenditure = columns[5].trim(),
                    money = columns[6].trim(),
                    paymentMethod = if (columns.size > 7) columns[7].trim() else "",
                    tradingStatus = if (columns.size > 8) columns[8].trim() else "",
                    transactionOrderNumber = if (columns.size > 9) columns[9].trim() else "",
                    merchantOrderNumber = if (columns.size > 10) columns[10].trim() else "",
                    remark = if (columns.size > 11) columns[11].trim() else "",
                )
                Log.d(TAG, aliPay.toString())
                val aliPayType: Int = when (aliPay.receiptOrExpenditure) {
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
                    val bill = Bill().apply {
                        id = Xid.string()
                        bookId = Config.book.id
                        money = aliPay.money.toBigDecimal()
                        type = aliPayType
                        time = TimeUtils.string2Date(aliPay.transactionTime, "yyyy-MM-dd HH:mm:ss")
                        category = aliPay.transactionCategory.ifEmpty { "支付宝" }
                        remark = "${aliPay.counterparty} ${aliPay.productName}".trim()
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
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
            result(false, "${e.message}")
        }
    }

    override fun readWeiXinPay(
        inputStream: InputStream, result: (Boolean, msg: String) -> Unit
    ) {
        try {
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            val reader = CSVReader(inputStreamReader)
            var nextLine: Array<String>?
            while (reader.readNext().also { nextLine = it } != null) {
                val columns = nextLine!!
                if (columns.size < 9) {
                    Log.d(TAG, columns.contentToString())
                    continue
                }
                if (columns[4] != "支出" && columns[4] != "支付") continue
                val weiPay = WeiXinPayEntity(
                    columns[0].trim(),
                    columns[1].trim(),
                    columns[2].trim(),
                    columns[3].trim(),
                    columns[4].trim(),
                    columns[5].trim(),
                    columns[6].trim(),
                    columns[7].trim(),
                    columns[8].trim(),
                    columns[9].trim(),
                )
                val weiMoney = weiPay.money.split("¥")[1]

                var weiPayType: Int = if (weiPay.receiptOrExpenditure == "支出") {
                    BillType.EXPENDITURE.value
                } else if (weiPay.receiptOrExpenditure == "收入") {
                    BillType.INCOME.value
                } else {
                    continue
                }
                try {
                    val bill = Bill().apply {
                        id = Xid.string()
                        bookId = Config.book.id
                        money = weiMoney.toBigDecimal()
                        type = weiPayType
                        time = TimeUtils.string2Date(weiPay.transactionTime, "yyyy-MM-dd HH:mm:ss")
                        category = "微信"
                        remark = "${weiPay.counterparty}${weiPay.remark}"
                    }
                    if (bill.money.compareTo(BigDecimal.ZERO) == 0) {
                        continue
                    }
                    App.dataBase.billDao().let {
                        val exist = it.exist(bill.hashCode()) > 0
                        if (!exist) {
                            LogUtils.d(bill)
                            it.insert(bill)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Log.d(TAG, weiPay.toString())
            }
            result(true, "导入完成")
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
            result(false, "${e.message}")
        }
    }

    override fun readQianJi(inputStream: InputStream, result: (Boolean, msg: String) -> Unit) {
    }
}