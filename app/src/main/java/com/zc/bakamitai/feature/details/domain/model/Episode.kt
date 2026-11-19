package com.zc.bakamitai.feature.details.domain.model

import kotlin.time.Instant

/**
 * An episode or a batch of a show.
 *
 * @property label as SubsPlease writes it: `"07"`, `"1216v2"`, `"Egghead SP11"`, `"Movie"`, `"01-28"`.
 * @property releasedAt the reported release instant (currently an hour early in winter, docs/api.md §3.5).
 * @property downloads at most one per [Resolution]; empty when SubsPlease lists no usable link.
 */
data class Episode(val label: String, val releasedAt: Instant, val downloads: List<Download>)
