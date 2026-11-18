package com.zc.bakamitai.feature.home.domain.model

import com.zc.bakamitai.common.domain.model.Show
import kotlin.time.Instant

/**
 * One released episode (or batch, or movie).
 *
 * @property episode the episode as SubsPlease labels it: `"07"`, a batch range `"01-24"`, or `"Movie"`.
 * @property releasedAt when it was released, as reported by `release_date`.
 */
data class Release(val show: Show, val episode: String, val releasedAt: Instant)
