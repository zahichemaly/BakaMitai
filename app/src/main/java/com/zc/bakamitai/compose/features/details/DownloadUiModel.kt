package com.zc.bakamitai.compose.features.details

data class DownloadUiModel(
    val title: String,
    val urls: List<DownloadUrl>
)

data class DownloadUrl(
    val res: String,
    val links: List<DownloadLink>,
)

data class DownloadLink(
    val source: String,
    val link: String,
    val onClick: () -> Unit = {}
)
