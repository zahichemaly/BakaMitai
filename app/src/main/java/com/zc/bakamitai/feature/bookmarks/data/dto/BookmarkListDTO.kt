package com.zc.bakamitai.feature.bookmarks.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The root of `bookmarks.json`: the saved shows, unique by slug, in the order they were added. An object rather
 * than a bare list, so fields can be added later.
 */
@Serializable
data class BookmarkListDTO(@SerialName("bookmarks") val bookmarks: List<BookmarkDTO>)
