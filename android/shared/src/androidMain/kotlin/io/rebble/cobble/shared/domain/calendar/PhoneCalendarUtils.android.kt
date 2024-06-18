package io.rebble.cobble.shared.domain.calendar

import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.entity.Calendar

private val uri: Uri = CalendarContract.Calendars.CONTENT_URI
private val calendarProjection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.CALENDAR_COLOR
)

actual suspend fun getCalendars(platformContext: PlatformContext): List<Calendar> {
    platformContext as AndroidPlatformContext

    val contentResolver = platformContext.applicationContext.contentResolver
    return contentResolver.query(uri, calendarProjection, null, null, null)?.use { cursor ->
        return@use generateSequence {
            if (cursor.moveToNext()) {
                val id = cursor.getNullableColumnIndex(CalendarContract.Calendars._ID)
                        ?.let { cursor.getLong(it) } ?: return@generateSequence null
                val accountName = cursor.getNullableColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val displayName = cursor.getNullableColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val ownerAccount = cursor.getNullableColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val color = cursor.getNullableColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null

                Calendar(
                        id = 0,
                        platformId = id.toString(),
                        name = displayName,
                        ownerName = accountName,
                        ownerId = ownerAccount,
                        color = color,
                        enabled = true
                )
            } else {
                null
            }
        }.toList()
    } ?: listOf()
}

private fun Cursor.getNullableColumnIndex(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    return if (index == -1) {
        null
    } else {
        index
    }
}