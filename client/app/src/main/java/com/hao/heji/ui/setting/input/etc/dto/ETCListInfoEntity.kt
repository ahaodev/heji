package com.hao.heji.ui.setting.input.etc.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ETCListInfoEntity(
    @SerialName("status") val status: String = "",
    @SerialName("data") val data: List<Info> = emptyList()
) {
    @Serializable
    data class Info(
        val cardNo: String = "",
        @SerialName("payCardType") val payCardType: String = "",
        @SerialName("etcPrice") val etcPrice: Int = 0,
        @SerialName("exchargetime") val exchargetime: String = "",
        @SerialName("ex_enStationName") val exEnStationName: String = "",
        @SerialName("vehplate") val vehplate: String = "",
        @SerialName("province") val province: String = "",
        @SerialName("type") val type: Int = 0
    )
}
