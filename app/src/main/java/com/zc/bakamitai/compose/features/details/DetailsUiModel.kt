package com.zc.bakamitai.compose.features.details

data class DetailsUiModel(
    val title: String,
    val summary: String,
    val imageUrl: String,
    val downloads: List<DownloadUiModel>
)
