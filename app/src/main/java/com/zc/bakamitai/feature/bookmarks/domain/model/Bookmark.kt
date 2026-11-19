package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.common.domain.model.Show
import kotlin.time.Instant

/**
 * A saved show, with when it was saved (the Bookmarks screen sorts by it).
 *
 * @property airing the show's weekly slot, or `null` when it isn't on the weekly schedule (or wasn't fetched yet).
 */
data class Bookmark(val show: Show, val addedAt: Instant, val airing: AiringSlot? = null)
