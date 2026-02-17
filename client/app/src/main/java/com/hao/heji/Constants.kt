package com.hao.heji

import com.hao.heji.utils.YearMonth
import kotlinx.serialization.json.Json
import java.util.*

val currentYearMonth: YearMonth = today()

fun today(): YearMonth = YearMonth(
    year = Calendar.getInstance().get(Calendar.YEAR),
    month = Calendar.getInstance().get(Calendar.MONTH) + 1,
    day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
)

val json: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
}
