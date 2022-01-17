package com.zc.bakamitai.data.models.dtos

import com.zc.bakamitai.data.room.entities.Bookmark

data class ShowDetailsDto(
    val synopsis: String? = null,
    val image: String? = null,
    val title: String? = null,
    val sid: String? = null,
    val url: String? = null,
) {
    fun toBookmark(): Bookmark {
        return Bookmark(
            id = sid!!,
            page = url ?: "",
            name = title ?: "",
            imageUrl = image ?: ""
        )
    }
}
