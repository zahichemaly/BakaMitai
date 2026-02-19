package com.zc.bakamitai.compose.features.details.data.remote

import com.zc.bakamitai.compose.core.network.AppClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Document

class DetailsDataSourceImpl(private val appClient: AppClient) : DetailsDataSource {
    override suspend fun getDetails(page: String): Document {
        return withContext(Dispatchers.IO) {
            appClient.get<Document>("/page/$page") {
            }
        }
    }
}
