package com.zc.bakamitai.extensions

import android.net.Uri
import com.zc.bakamitai.data.Constants
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
fun String.toDateTime(): Date? = toDate(Constants.DateFormat.DATE_TIME_TZ)

/**
 * 15:00 ~> 3:00 PM.
 */
fun String.to12HourFormat(): String {
    val date = toDate(Constants.DateFormat.TIME)
    return date?.format(Constants.DateFormat.TIME_AM_PM) ?: Constants.Common.NOT_AVAILABLE
}

fun String.isDayOfWeek(): Boolean {
    val days = DateFormatSymbols().weekdays.toList()
    return days.any { it.contentEquals(this, true) }
}

fun String.toDayOfWeekNumber(startsMonday: Boolean): Int {
    if (!isDayOfWeek()) return 10 //for TBD shows, put them at the end
    val sdf = SimpleDateFormat(Constants.DateFormat.DAY_OF_WEEK, locale)
    val date = try {
        sdf.parse(this)
    } catch (ex: ParseException) {
        Timber.e("Error formatting date $this. Exception: ${ex.stackTraceToString()}")
        return -1
    }
    val calendar = Calendar.getInstance()
    calendar.time = date
    val dayOfWeekNumber = calendar.get(Calendar.DAY_OF_WEEK)
    if (startsMonday && dayOfWeekNumber == Calendar.SUNDAY) {
        return Calendar.SATURDAY + 1
    }
    return dayOfWeekNumber
}

/**
 * Appends [this] to [Constants.Api.BASE_URL].
 */
fun String.toImageUrl(): String {
    return Uri.parse(Constants.Api.BASE_URL)
        .buildUpon()
        .appendEncodedPath(this)
        .toString()
}

/**
 * Parses the page name from [this].
 *
 * Example: /shows/100-man-no-inochi-no-ue-ni-ore-wa-tatte-iru.
 *
 * Segments: ["shows", "100-man-no-inochi-no-ue-ni-ore-wa-tatte-iru"]
 *
 * @return "100-man-no-inochi-no-ue-ni-ore-wa-tatte-iru"
 */
fun String.parsePage(): String {
    val fullUrl = Uri.parse(Constants.Api.BASE_URL)
        .buildUpon()
        .appendEncodedPath(this)
        .build()
    val segments = fullUrl.pathSegments
    return segments.getOrNull(1) ?: this
}
