package com.zc.bakamitai.feature.shows.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.koin.core.annotation.Single

/** SubsPlease's all shows page (docs/api.md §4.1). It doesn't depend on the zone, so no `tz`. */
@Single
class ShowsService(private val client: HttpClient) {

    /** The page's HTML, decoded with the charset of its `Content-Type` (UTF-8). */
    suspend fun getAllShowsPage(): String = client.get(SHOWS_PATH).bodyAsText()

    private companion object {
        const val SHOWS_PATH = "shows/"
    }
}
