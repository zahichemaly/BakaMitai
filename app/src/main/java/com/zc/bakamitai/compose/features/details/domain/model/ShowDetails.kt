package com.zc.bakamitai.compose.features.details.domain.model

data class ShowDetails(
    val synopsis: String? = null,
    val image: String? = null,
    val title: String? = null,
    val sid: String? = null,
    val page: String? = null,
    val pageUrl: String? = null,
    val episodes: List<Episode> = emptyList()
)
