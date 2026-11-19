package com.zc.bakamitai.feature.bookmarks.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One saved show in `bookmarks.json` (context.md §5). The slug is the key: Details reloads the show from it.
 *
 * @property coverUrl the absolute cover URL, or `null` when the show had none.
 */
@Serializable
data class BookmarkDTO(
    @SerialName("slug") val slug: String,
    @SerialName("title") val title: String,
    @SerialName("cover_url") val coverUrl: String?,
    @SerialName("added_at_epoch_millis") val addedAtEpochMillis: Long,
)
