package com.zc.bakamitai.data.models

import com.google.gson.annotations.SerializedName
import com.zc.bakamitai.data.models.dtos.ShowDownloadDto

data class ShowEpisodeResponse(

    @field:SerializedName("batch")
    val batch: HashMap<String, EpisodeItem> = hashMapOf(),

    @field:SerializedName("episode")
    val episode: HashMap<String, EpisodeItem> = hashMapOf()
) {
    fun toShowDownloadsDto(): List<ShowDownloadDto> {
        val result = mutableListOf<ShowDownloadDto>()
        result.addAll(batch.map {
            val show = it.value
            ShowDownloadDto(
                title = it.key,
                time = show.time,
                releaseDate = show.releaseDate,
                episode = show.episode.toFloatOrNull() ?: 0F,
                downloads = show.downloads
            )
        })
        result.addAll(episode.map {
            val show = it.value
            ShowDownloadDto(
                title = it.key,
                time = show.time,
                releaseDate = show.releaseDate,
                episode = show.episode.toFloatOrNull() ?: 0F,
                downloads = show.downloads
            )
        })
        return result.sortedByDescending { it.episode }
    }
}

data class EpisodeItem(

    @field:SerializedName("release_date")
    val releaseDate: String,

    @field:SerializedName("downloads")
    val downloads: List<DownloadsItem>,

    @field:SerializedName("show")
    val show: String,

    @field:SerializedName("episode")
    val episode: String,

    @field:SerializedName("time")
    val time: String
)
