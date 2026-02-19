package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.core.network.GenericResponse
import org.jsoup.nodes.Document

interface DetailsDataSource {
    suspend fun getDetails(page: String): GenericResponse<Document>
}
