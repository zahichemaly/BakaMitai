package com.zc.bakamitai.data.models

import com.google.gson.annotations.SerializedName
import com.zc.bakamitai.BakaApplication
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.DownloadDto
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.extensions.toImageUrl

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
        val episode = BakaApplication.getContext().getString(R.string.ep_, episode)
        return EntryDto(
            time = time,
            name = show,
            episode = episode,
            imageUrl = imageUrl.toImageUrl(),
            date = releaseDate,
            page = page
        )
    }
}

data class DownloadsItem(

    @field:SerializedName("res")
    val res: String,

    @field:SerializedName("magnet")
    val magnet: String,

    @field:SerializedName("torrent")
    val torrent: String,

    @field:SerializedName("xdcc")
    val xdcc: String
) {

    fun toDownloadDto(): DownloadDto {
        return DownloadDto(
            res = res.toIntOrNull() ?: 0,
            magnet = magnet,
            torrent = torrent,
            xdcc = xdcc
        )
    }
}
