package com.zc.bakamitai.data.models.dtos

import com.zc.bakamitai.extensions.formatToDay
import com.zc.bakamitai.extensions.toDateTime
import java.util.*

data class EntryDto(
    val time: String = "",
    val name: String = "",
    val episode: String = "",
    val imageUrl: String = "",
    private val date: String = "",
) {
    private var _date: Date? = null

    init {
        if (date.isNotEmpty()) {
            _date = date.toDateTime()
        }
    }

    fun getFormattedDate(): String {
        return _date?.formatToDay() ?: date
    }

    fun getDate(): Date? = _date
}
