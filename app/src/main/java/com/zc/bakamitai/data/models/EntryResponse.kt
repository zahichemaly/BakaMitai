package com.zc.bakamitai.data.models

import android.net.Uri
import com.google.gson.annotations.SerializedName
import com.zc.bakamitai.BakaApplication
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.data.network.Api

data class EntryResponse(

    @field:SerializedName("xdcc")
    val xdcc: String,

    @field:SerializedName("release_date")
    val releaseDate: String,

    @field:SerializedName("downloads")
    val downloads: List<DownloadsItem>,

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("show")
    val show: String,

    @field:SerializedName("episode")
    val episode: String,

    @field:SerializedName("time")
    val time: String,

    @field:SerializedName("page")
    val page: String
) {
    fun toEntryDto(): EntryDto {
        val imageUrl = Uri.parse(Api.BASE_URL)
            .buildUpon()
            .appendEncodedPath(imageUrl)
            .toString()
        val episode = BakaApplication.getContext().getString(R.string.episode, episode)
        return EntryDto(
            time = time,
            name = show,
            episode = episode,
            imageUrl = imageUrl,
            date = releaseDate
        )
    }
}

data class DownloadsItem(

    @field:SerializedName("res")
    val res: String,

    @field:SerializedName("magnet")
    val magnet: String
)
