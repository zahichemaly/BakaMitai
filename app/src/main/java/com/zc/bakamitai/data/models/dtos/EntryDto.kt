package com.zc.bakamitai.data.models.dtos

import com.zc.bakamitai.extensions.formatToDay
import com.zc.bakamitai.extensions.formatToDay12Hour
import com.zc.bakamitai.extensions.to12HourFormat
import com.zc.bakamitai.extensions.toDateTime
import java.util.*

data class EntryDto(
    val page: String = "",
    val name: String = "",
    val episode: String = "",
    val time: String = "",
    val date: String = "",
    val imageUrl: String = "",
    val aired: Boolean = false,
) {

    fun getFormattedDate(is12HourFormat: Boolean): String {
        return if (is12HourFormat) getDateTime()?.formatToDay12Hour() ?: date
        else getDateTime()?.formatToDay() ?: date
    }

    fun getFormattedTime(is12HourFormat: Boolean): String {
        return if (is12HourFormat) time.to12HourFormat()
        else time
    }

    fun getDateTime(): Date? = date.toDateTime()
}
