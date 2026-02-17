package com.hao.heji.network.response

import kotlinx.serialization.Serializable

@Serializable
data class ImageEntity(var _id: String, var md5: String, var bill_id: String, var length: Long, var ext: String)