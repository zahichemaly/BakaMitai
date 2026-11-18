package com.zc.bakamitai.feature.home.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset

/**
 * Everything Home shows about [date] in [timeZone].
 *
 * @property utcOffset the offset of [timeZone] when the data was loaded.
 * @property upcoming shows still to air today, by time ascending.
 * @property released releases dated today, by release time ascending.
 */
data class TodayBroadcasts(
    val date: LocalDate,
    val timeZone: TimeZone,
    val utcOffset: UtcOffset,
    val upcoming: List<UpcomingBroadcast>,
    val released: List<Release>,
)
