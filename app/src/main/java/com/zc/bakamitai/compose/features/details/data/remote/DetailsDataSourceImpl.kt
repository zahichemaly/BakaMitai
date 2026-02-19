package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.core.network.GenericResponse
import com.zc.bakamitai.compose.core.network.HttpClientWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document

class DetailsDataSourceImpl(private val httpClientWrapper: HttpClientWrapper) : DetailsDataSource {
    override suspend fun getDetails(page: String): GenericResponse<Document> {
        return withContext(Dispatchers.IO) {
            httpClientWrapper.get<Document>("/page/$page") {
            }
        }
    }
}
