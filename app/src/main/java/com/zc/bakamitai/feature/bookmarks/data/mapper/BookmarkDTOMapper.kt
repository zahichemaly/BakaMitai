package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlin.time.Instant

/** `null` for a stored entry without a slug or a title, which couldn't be opened or shown. */
fun BookmarkDTO.toDomain(): Bookmark? = if (slug.isBlank() || title.isBlank()) {
    null
} else {
    Bookmark(
        show = Show(slug = ShowSlug(slug), title = title, coverUrl = coverUrl),
        addedAt = Instant.fromEpochMilliseconds(addedAtEpochMillis),
    )
}
