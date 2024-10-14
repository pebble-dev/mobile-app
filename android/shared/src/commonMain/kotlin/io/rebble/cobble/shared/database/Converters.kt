package io.rebble.cobble.shared.database

import androidx.room.TypeConverter
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.datetime.Instant

class Converters {
    @TypeConverter
    fun StringToUuid(string: String?): Uuid? = string?.let { uuidFrom(it) }

    @TypeConverter
    fun UuidToString(uuid: Uuid?): String? = uuid?.toString()

    @TypeConverter
    fun LongToInstant(long: Long?): Instant? = long?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun InstantToLong(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}