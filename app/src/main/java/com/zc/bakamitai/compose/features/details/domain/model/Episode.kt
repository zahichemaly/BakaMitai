package com.zc.bakamitai.compose.features.details.domain.model

data class Episode(
    val time: String,
    val releaseDate: String,
    val show: String,
    val episode: String,
    val downloads: List<Download>
)

data class Download(
    val resolution: String,
    val links: Map<LinkSource, String> = emptyMap()
)

enum class LinkSource {
    XDCC, MAGNET, TORRENT
}
