package com.zc.bakamitai.feature.home.presentation.component.todaysummary.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * The "Today's Broadcasts" banner.
 *
 * @property utcOffsetLabel the device's offset, "UTC-5" / "UTC+5:30" / "UTC".
 * @property upcomingCount "4 Airing": the number of upcoming shows.
 * @property releasedCount "5 Available": the number of releases today.
 */
@Immutable
data class TodaySummaryUIModel(val utcOffsetLabel: UiText, val upcomingCount: UiText, val releasedCount: UiText)
