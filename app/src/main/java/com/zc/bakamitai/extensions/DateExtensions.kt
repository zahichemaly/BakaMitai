package com.zc.bakamitai.extensions

import com.zc.bakamitai.data.Constants
import java.text.SimpleDateFormat
import java.util.*

val locale = Locale("en")

/**
 * Converts [Date] to [String] using specified [format].
 */
fun Date.format(format: String): String {
    val sdf = SimpleDateFormat(format, locale)
    return sdf.format(this)
}

fun Date.formatToDay(): String = format(Constants.DateFormat.DATE_TIME)
fun Date.formatToDay12Hour(): String = format(Constants.DateFormat.DATE_TIME_AM_PM)
