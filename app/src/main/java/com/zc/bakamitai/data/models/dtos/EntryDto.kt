package com.zc.bakamitai.data.models.dtos

import com.zc.bakamitai.extensions.formatToDay
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

    fun getFormattedDate(): String {
        return getDateTime()?.formatToDay() ?: date
    }

    fun getDateTime(): Date? = date.toDateTime()
}
