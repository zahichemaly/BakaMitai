package com.zc.bakamitai.common.domain.model

import kotlinx.datetime.LocalTime

/**
 * A show on a day's schedule.
 *
 * @property time the scheduled release time in the requested time zone.
 * @property aired whether the episode is out. Today's schedule sets it, and Schedule copies it onto today's
 * weekly bucket; `null` when the source doesn't say (the weekly schedule's other days).
 */
data class ScheduledShow(val show: Show, val time: LocalTime, val aired: Boolean?)
