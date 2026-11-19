package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * A bookmark as the Bookmarks screen shows it.
 *
 * @property addedOn the day it was bookmarked, in the device's time zone.
 * @property nextAiring the next occurrence of the weekly slot in the device's time zone, or `null` when the show
 * isn't on the weekly schedule.
 * @property airsToday whether [nextAiring] is today, in the device's time zone.
 */
data class BookmarkedShow(
    val bookmark: Bookmark,
    val addedOn: LocalDate,
    val nextAiring: LocalDateTime?,
    val airsToday: Boolean,
)
