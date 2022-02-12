package com.zc.bakamitai.data.models.dtos

import java.text.DecimalFormat

data class ShowDownloadDto(
    val title: String = "",
    val time: String = "",
    val releaseDate: String = "",
    val episode: Float = 0F,
    val downloads: List<DownloadDto> = listOf(),
    var isExpanded: Boolean = false
) {
    fun getFormattedEpisode(): String {
        val df = DecimalFormat("0.#")
        return df.format(episode)
    }
}
