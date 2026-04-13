package com.hao.heji.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MqttBrokerInfo(
    @SerialName("address") val address: String,
    @SerialName("tcp_port") val tcpPort: String,
    @SerialName("ws_port") val wsPort: String,
) {
    fun toTcpUrl(): String = "tcp://$address:$tcpPort"
}
