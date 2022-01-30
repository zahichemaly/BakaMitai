package com.zc.bakamitai.data.models.dtos

import android.net.Uri
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.data.network.Api
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

    fun getPageUrl(): String {
        return Uri.parse(Constants.Api.BASE_URL)
            .buildUpon()
            .appendEncodedPath("shows")
            .appendEncodedPath(url)
            .toString()
    }
}
