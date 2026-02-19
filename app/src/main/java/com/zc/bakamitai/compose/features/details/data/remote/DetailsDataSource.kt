package com.zc.bakamitai.compose.features.details.data.remote

import org.jsoup.nodes.Document

interface DetailsDataSource {
    suspend fun getDetails(page: String): Document
}
