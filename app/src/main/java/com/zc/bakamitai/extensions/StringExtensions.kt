package com.zc.bakamitai.extensions

import android.net.Uri
import com.zc.bakamitai.data.network.Api
import timber.log.Timber
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts string to [Date] using format [format].
 */
fun String.toDate(format: String): Date? {
    val sdf = SimpleDateFormat(format, locale)
    if (isEmpty()) {
        Timber.e("Date string is empty")
        return null
    }
    return try {
        sdf.parse(this)
    } catch (ex: ParseException) {
        Timber.e("Error formatting date $this. Exception: ${ex.stackTraceToString()}")
        null
    }
}

/**
 * Converts string to [Date] using format Sat, 15 Jan 2022 14:32:05 +0200.
 */
fun String.toDateTime(): Date? = toDate("EEE, d LLL yyyy HH:mm:sss Z")

fun String.toDayOfWeekNumber(): Int {
    val days = DateFormatSymbols().weekdays.toList()
    val exists = days.any { it.contentEquals(this, true) }
    if (!exists) return 10
    val sdf = SimpleDateFormat("EEEE", locale)
    val date = try {
        sdf.parse(this)
    } catch (ex: ParseException) {
        Timber.e("Error formatting date $this. Exception: ${ex.stackTraceToString()}")
        return -1
    }
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.DAY_OF_WEEK)
}

/**
 * Appends [this] to [Api.BASE_URL].
 */
fun String.toImageUrl(): String {
    return Uri.parse(Api.BASE_URL)
        .buildUpon()
        .appendEncodedPath(this)
        .toString()
}
