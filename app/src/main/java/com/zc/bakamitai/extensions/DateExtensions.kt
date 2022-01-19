package com.zc.bakamitai.extensions

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

fun Date.formatToDay(): String = format("EEE, d LLL yyyy HH:mm")
fun Date.formatToDay12Hour(): String = format("EEE, d LLL yyyy hh:mm aa")
