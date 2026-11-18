package com.zc.bakamitai.feature.home.domain.model

import kotlinx.datetime.LocalTime

/**
 * A show scheduled for later today.
 *
 * @property time the scheduled time in the device's time zone.
 * @property episodeNumber the episode expected next, or `null` when it can't be worked out.
 */
data class UpcomingBroadcast(val show: Show, val time: LocalTime, val episodeNumber: Int?)
