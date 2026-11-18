package com.zc.bakamitai.feature.home.domain.model

import kotlinx.datetime.LocalTime

/**
 * A show on a day's schedule.
 *
 * @property time the scheduled release time in the requested time zone.
 * @property aired whether the episode is out; `null` when the source doesn't say (the weekly schedule).
 */
data class ScheduledShow(val show: Show, val time: LocalTime, val aired: Boolean?)
