package com.zc.bakamitai.extensions

import com.zc.bakamitai.compose.core.domain.DateFormattedResult
import com.zc.bakamitai.data.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val locale = Locale("en")

/**
 * Converts [Date] to [String] using specified [format].
 */
fun Date.format(format: String): String {
    val sdf = SimpleDateFormat(format, locale)
    return sdf.format(this)
}

fun Date.formatToDay(): String = format(Constants.DateFormat.DATE_TIME)

fun Date.toDateFormattedResult(): DateFormattedResult {
    val timeStr = format(Constants.DateFormat.TIME_AM_PM)
    return when {
        isToday() -> DateFormattedResult.Today(timeStr)
        isTomorrow() -> DateFormattedResult.Tomorrow(timeStr)
        isYesterday() -> DateFormattedResult.Yesterday(timeStr)
        else -> DateFormattedResult.Other(format(Constants.DateFormat.DAY_OF_WEEK), timeStr)
    }
}

private fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    return isSameDay(today)
}

private fun Date.isTomorrow(): Boolean {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    return isSameDay(tomorrow)
}

private fun Date.isYesterday(): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return isSameDay(yesterday)
}

private fun Date.isSameDay(other: Calendar): Boolean {
    val target = Calendar.getInstance().apply { time = this@isSameDay }
    return target.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}
