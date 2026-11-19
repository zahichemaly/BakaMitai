package com.zc.bakamitai.feature.bookmarks.data.mapper

import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark

fun Bookmark.toDTO(): BookmarkDTO = BookmarkDTO(
    slug = show.slug.value,
    title = show.title,
    coverUrl = show.coverUrl,
    addedAtEpochMillis = addedAt.toEpochMilliseconds(),
)
