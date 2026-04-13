package com.hao.heji

import com.hao.heji.data.db.Bill
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.IOException

class JsonJavaTest {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    @Throws(IOException::class)
    fun jsonTest() {
        val jsonString =
            "{\"_id\":\"611e854692891153fb00ae14\",\"book_id\":\"mybook\",\"money\":0,\"type\":-1,\"category\":null,\"time\":\"2021-22-20 00:22:30\",\"upd_time\":0,\"crt_user\":\"testuser\",\"remark\":null}"
        val billEntity = json.decodeFromString(Bill.serializer(), jsonString)
        println(billEntity)
    }
}