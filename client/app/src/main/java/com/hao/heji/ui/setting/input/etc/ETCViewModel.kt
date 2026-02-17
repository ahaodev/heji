package com.hao.heji.ui.setting.input.etc

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hao.heji.App
import com.hao.heji.config.Config
import com.hao.heji.data.BillType
import com.hao.heji.data.converters.DateConverters
import com.hao.heji.data.converters.MoneyConverters
import com.hao.heji.data.db.Bill
import com.hao.heji.data.db.Category
import com.github.shamil.Xid
import com.hao.heji.json
import com.hao.heji.ui.base.BaseViewModel
import com.hao.heji.ui.setting.input.etc.dto.ETCListInfoEntity
import com.hao.heji.ui.setting.input.etc.dto.HBETCEntity
import com.hao.heji.ui.setting.input.etc.dto.HBETCEntity.DataBean.OrderArrBean
import com.hao.heji.network.HttpRetrofit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.function.Consumer

/**
 * @date: 2020/11/10
 * @author: 锅得铁
 * #
 */
internal class ETCViewModel : BaseViewModel<ETCUiState>() {


    var etcID = ETC.ID
    var carID = ETC.CAR_ID
    var yearMonth: String? = null

    /**
     * 获取类别
     *
     * @return 类别名称
     */
    private val categoryName: String
        get() {
            val categories = App.dataBase.categoryDao().queryByCategoryName("过路费")
            val category: Category
            if (categories.isEmpty()) {
                category = Category(name = "过路费", bookId = Config.book.id).apply {
                    level = 0
                    type = BillType.EXPENDITURE.value
                }
                App.dataBase.categoryDao().insert(category)
            } else {
                category = categories[0]
            }
            return category.name
        }

    /**
     * 保存到数据库
     *
     * @param strBody 内容
     * @return
     */
    private fun saveToDB(strBody: String) {
        val etcListInfo = json.decodeFromString<ETCListInfoEntity>(strBody)
        if (etcListInfo.data.isNotEmpty()) {
            val data = etcListInfo.data
            data.forEach(Consumer { info: ETCListInfoEntity.Info ->
                val bill = Bill().apply {
                    id = Xid.string()
                    bookId = Config.book.id
                    money = BigDecimal(info.etcPrice).divide(BigDecimal(100))
                    remark = info.exEnStationName
                    time = DateConverters.str2Date(info.exchargetime)
                    category = categoryName
                    type = BillType.EXPENDITURE.value
                }.also {
                    it.hashValue = it.hashCode()
                }

                /**
                 * 如果不存在才插入
                 */
                val exist = App.dataBase.billDao().exist(bill.hashCode()) > 0
                if (!exist) {
                    val count = App.dataBase.billDao().insert(bill)
                    LogUtils.d("成功导入${count}条 ", bill)
                } else {
                    LogUtils.d("ETC账单已存在", bill)
                }
            })
            send(ETCUiState.InputSuccess)

        } else {
            send(ETCUiState.InputError(RuntimeException("input fail :null data ")))
        }
    }

    /**
     * 请求账单详情列表
     *
     * @param etcID ETC号码
     * @param month 月份
     * @param carID 车牌号
     */
    fun requestHBGSETCList(etcID: String, month: String, carID: String) {
        val requestURL = "http://www.hbgsetc.com/index.php?/newhome/getMonthBillData"
        //www - url 解码方式
        val requestBody =
            "cardNo=$etcID&month=$month&vehplate=$carID&flag=0".toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
        //伪装成浏览器请求
        val request: Request = Request.Builder()
            .url(requestURL)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "*/*")
            .addHeader("User-Agent", ETC.USER_AGENTS[Random().nextInt(ETC.USER_AGENTS.size - 1)])
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .post(requestBody)
            .build()
        HttpRetrofit.okHttpClient().newCall(request).enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val strBody = response.body.string()
                    try {
                        val jsonObject = JSONObject(strBody)
                        val status = jsonObject.getString("status")
                        if (status == "error") {
                            val error = jsonObject.getString("msg")
                            ToastUtils.showLong(error)
                        } else if (status == "OK") {
                            val hbetcEntity = json.decodeFromString<HBETCEntity>(strBody)
                            if (hbetcEntity.data != null && hbetcEntity.data.orderArr.isNotEmpty()) {
                                val data = hbetcEntity.data.orderArr
                                data.forEach(Consumer { info: OrderArrBean -> saveToBillDB(info) })
                                send(ETCUiState.InputSuccess)
                            } else {
                                ToastUtils.showShort("导入失败")
                                send(ETCUiState.InputError(RuntimeException("导入失败")))
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        ToastUtils.showShort("解析失败")
                        send(ETCUiState.InputError(RuntimeException("解析错误")))
                    }
                } else if (response.code == 404) {
                    requestETCList2(etcID, month, carID)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                ToastUtils.showLong(e.message)
                send(ETCUiState.InputError(RuntimeException("请求错误")))
                requestETCList2(etcID, month, carID)
            }
        })
    }

    /**
     *
     * 请求1失败后尝试2
     * 请求账单详情列表
     *
     * @param etcID ETC号码
     * @param month 月份
     * @param carID 车牌号
     */
    private fun requestETCList2(etcID: String, month: String, carID: String) {
        //伪装User-Agent
        val url =
            "http://hubeiweixin.u-road.com:80/HuBeiCityAPIServer/index.php/huibeicityserver/loadmonthinfo"
        LogUtils.d("尝试URL2", url)
        //www - url 解码方式
        val requestBody =
            "caidno=$etcID&month=$month&vehplate=$carID".toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
        //伪装成浏览器请求
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("User-Agent", ETC.USER_AGENTS[Random().nextInt(ETC.USER_AGENTS.size - 1)])
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .post(requestBody)
            .build()
        HttpRetrofit.okHttpClient().newCall(request).enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val strBody = response.body.string()
                    try {
                        val jsonObject = JSONObject(strBody)
                        val status = jsonObject.getString("status")
                        if (status == "error") {
                            val error = jsonObject.getString("msg")
                            ToastUtils.showLong(error)
                        } else {
                            saveToDB(strBody)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        ToastUtils.showShort("解析失败")
                        send(ETCUiState.InputError(RuntimeException("解析错误")))
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                ToastUtils.showLong(e.message)
                send(ETCUiState.InputError(RuntimeException("请求错误")))
            }
        })
    }

    private fun saveToBillDB(info: OrderArrBean) {
        val bill = Bill().apply {
            id = Xid.string()
            bookId=Config.book.id
            money = BigDecimal(info.totalFee).divide(BigDecimal(100))
            remark = info.enStationName + "|" + info.exStationName
            time = DateConverters.str2Date(info.exTime)
            category = categoryName
            type = BillType.EXPENDITURE.value
        }.also {
            it.hashValue = it.hashCode()
        }

        val exist = App.dataBase.billDao().exist(bill.hashCode()) > 0
        if (!exist) {
            val count = App.dataBase.billDao().insert(bill)
            LogUtils.d("成功导入${count} ", bill)
        } else {
            LogUtils.d("ETC账单已存在", bill)
        }
    }
}