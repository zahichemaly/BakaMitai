package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/** "Nov 17, 2026": English month names, like the rest of the UI (context.md §4.5). */
private val DateFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day(Padding.NONE)
    chars(", ")
    year()
}

/** A date, "Nov 17, 2026". */
fun LocalDate.toDateLabel(): UiText = UiText.StringValue(DateFormat.format(this))
