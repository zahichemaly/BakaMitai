package com.zc.bakamitai.compose.features.details

data class DetailsUiModel(
    val title: String,
    val imageUrl: String,
    val summary: String,
    val downloads: List<DownloadUiModel>
)
