package io.rebble.cobble.datasources.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson


class BooleanOrStringAdapter {
    @FromJson
    @BooleanOrString
    fun fromJson(value: Any): Boolean {
        return when (value) {
            is Boolean -> {
                value
            }
            is String -> {
                value.toBoolean()
            }
            else -> throw IllegalArgumentException("Unknown type: $value")
        }
    }

    @ToJson
    fun toJson(@BooleanOrString boolean: Boolean): Any {
        return boolean
    }
}

/**
 * JSON Boolean that can be decoded as either Boolean or as String
 */
@JsonQualifier
annotation class BooleanOrString