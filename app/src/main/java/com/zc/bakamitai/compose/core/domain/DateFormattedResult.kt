package com.zc.bakamitai.compose.core.domain

import androidx.compose.runtime.Composable
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText

sealed class DateFormattedResult {
    object None : DateFormattedResult()
    data class Today(val time: String) : DateFormattedResult()
    data class Tomorrow(val time: String) : DateFormattedResult()
    data class Yesterday(val time: String) : DateFormattedResult()
    data class Other(val dayOfWeek: String, val time: String) : DateFormattedResult()
}

@Composable
fun DateFormattedResult.asString(): String {
    return when (this) {
        is DateFormattedResult.Today -> UiText.StringResource(StringResource.date_today_at, time)
            .asString()

        is DateFormattedResult.Tomorrow -> UiText.StringResource(StringResource.date_tomorrow_at, time)
            .asString()

        is DateFormattedResult.Yesterday -> UiText.StringResource(StringResource.date_yesterday_at, time)
            .asString()

        is DateFormattedResult.Other -> UiText.StringResource(StringResource.date_day_at, dayOfWeek, time)
            .asString()

        else -> ""
    }
}
