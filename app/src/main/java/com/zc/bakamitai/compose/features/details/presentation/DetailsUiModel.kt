package com.zc.bakamitai.compose.features.details.presentation

import com.zc.bakamitai.compose.features.details.presentation.component.DownloadUiModel

data class DetailsUiModel(
    val title: String,
    val summary: String,
    val imageUrl: String,
    val downloads: List<DownloadUiModel>
)
