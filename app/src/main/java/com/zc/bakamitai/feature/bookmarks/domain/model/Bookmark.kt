package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.common.domain.model.Show
import kotlin.time.Instant

/** A saved show, with when it was saved (the Bookmarks screen sorts by it). */
data class Bookmark(val show: Show, val addedAt: Instant)
