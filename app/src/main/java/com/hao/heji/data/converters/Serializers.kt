package com.hao.heji.data.converters

import com.blankj.utilcode.util.TimeUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(TimeUtils.date2String(value, DateConverters.DB_PATTERN))
    }

    override fun deserialize(decoder: Decoder): Date {
        return TimeUtils.string2Date(decoder.decodeString(), DateConverters.DB_PATTERN)
    }
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeLong(value.multiply(BigDecimal(100)).toLong())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeLong()).divide(BigDecimal(100), 2, RoundingMode.DOWN)
    }
}

/**
 * 兼容服务端返回ISO 8601时间字符串和客户端Long毫秒时间戳
 */
object Iso8601ToLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Iso8601ToLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }

    override fun deserialize(decoder: Decoder): Long {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            val content = element.jsonPrimitive.content
            return content.toLongOrNull() ?: parseIso8601(content)
        }
        return decoder.decodeLong()
    }

    fun parseIso8601(value: String): Long {
        return try {
            val truncated = value.substringBefore('.')
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            fmt.parse(truncated)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * 可空版本的ISO 8601时间序列化器
 */
object NullableIso8601ToLongSerializer : KSerializer<Long?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableIso8601ToLong", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Long?) {
        if (value != null) encoder.encodeLong(value) else encoder.encodeLong(0)
    }

    override fun deserialize(decoder: Decoder): Long? {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            val content = element.jsonPrimitive.content
            return content.toLongOrNull() ?: Iso8601ToLongSerializer.parseIso8601(content)
        }
        return decoder.decodeLong()
    }
}
