package com.hao.heji.ui.setting.input.etc.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HBETCEntity(
    @SerialName("status") val status: String = "",
    @SerialName("data") val data: DataBean? = null
) {
    @Serializable
    data class DataBean(
        @SerialName("total_data") val totalData: TotalDataBean? = null,
        @SerialName("orderArr") val orderArr: List<OrderArrBean> = emptyList()
    ) {
        @Serializable
        data class TotalDataBean(
            @SerialName("total_num") val totalNum: Int = 0,
            @SerialName("total_fee") val totalFee: Int = 0,
            @SerialName("totalHighNum") val totalHighNum: Int = 0,
            @SerialName("totalHighSum") val totalHighSum: Int = 0,
            @SerialName("totalExpandNum") val totalExpandNum: Int = 0,
            @SerialName("totalExpandSum") val totalExpandSum: Int = 0
        )

        @Serializable
        data class OrderArrBean(
            @SerialName("sysid") val sysid: Int = 0,
            @SerialName("totalFee") val totalFee: Int = 0,
            @SerialName("enStationName") val enStationName: String = "",
            @SerialName("enTime") val enTime: String = "",
            @SerialName("exStationName") val exStationName: String = "",
            @SerialName("exTime") val exTime: String = "",
            @SerialName("vehicleplate") val vehicleplate: String = "",
            @SerialName("facecardnum") val facecardnum: String = "",
            @SerialName("serialnumber") val serialnumber: String = "",
            @SerialName("deductions") val deductions: String? = null,
            @SerialName("deductionsysids") val deductionsysids: String? = null,
            @SerialName("passRecordDetailVoList") val passRecordDetailVoList: String? = null,
            @SerialName("totalNum") val totalNum: String? = null,
            @SerialName("serviceType") val serviceType: Int = 0,
            @SerialName("eexitType") val eexitType: Int = 0
        )
    }
}
