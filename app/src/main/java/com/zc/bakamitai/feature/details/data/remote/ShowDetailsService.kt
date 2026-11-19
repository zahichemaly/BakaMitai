package com.zc.bakamitai.feature.details.data.remote

import com.zc.bakamitai.feature.details.data.dto.ShowEpisodesDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLPathPart
import org.koin.core.annotation.Single

/** A show's page (docs/api.md §4.2) and its episodes (§3.5). */
@Single
class ShowDetailsService(private val client: HttpClient) {

    /**
     * The page's HTML, decoded with the charset of its `Content-Type` (UTF-8). Requested with the trailing slash:
     * without it, the site answers with a redirect. The page doesn't depend on the zone, so no `tz`.
     */
    suspend fun getShowPage(slug: String): String = client.get(SHOWS_PATH + slug.encodeURLPathPart() + "/").bodyAsText()

    /** [sid] comes from the show page; `tz` is required, or the body is empty. */
    suspend fun getShowEpisodes(sid: String, timeZoneId: String): ShowEpisodesDTO = client.get(API_PATH) {
        parameter(PARAM_FUNCTION, FUNCTION_SHOW)
        parameter(PARAM_TIME_ZONE, timeZoneId)
        parameter(PARAM_SHOW_ID, sid)
    }.body()

    private companion object {
        const val SHOWS_PATH = "shows/"
        const val API_PATH = "api/"
        const val PARAM_FUNCTION = "f"
        const val PARAM_TIME_ZONE = "tz"
        const val PARAM_SHOW_ID = "sid"
        const val FUNCTION_SHOW = "show"
    }
}
