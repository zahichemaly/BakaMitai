package com.zc.bakamitai.data.models.dtos

import com.zc.bakamitai.data.models.DownloadsItem
import java.text.DecimalFormat

data class ShowDownloadDto(
    val title: String = "",
    val time: String = "",
    val releaseDate: String = "",
    val episode: Float = 0F,
    val downloads: List<DownloadsItem> = listOf()
) {
    fun getFormattedEpisode(): String {
        val df = DecimalFormat("0.#")
        return df.format(episode)
    }
}
