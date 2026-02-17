package com.hao.heji.data.converters

import com.blankj.utilcode.util.TimeUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

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
