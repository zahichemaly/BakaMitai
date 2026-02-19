package com.zc.bakamitai.compose.features.details.data.mapper

import androidx.core.net.toUri
import com.zc.bakamitai.compose.features.details.data.model.EpisodeContentDTO
import com.zc.bakamitai.compose.features.details.domain.model.ShowDetails
import com.zc.bakamitai.data.Constants
import com.zc.bakamitai.extensions.toImageUrl
import org.jsoup.nodes.Document

fun Document.toShowDetails(page: String, episodeContentDTO: EpisodeContentDTO): ShowDetails {
    val imageE = this.selectFirst("img[class='img-responsive img-center']")
    val synopsisE = this.selectFirst("div.series-syn")
    val titleE = this.selectFirst("h1.entry-title")
    val sidE = this.selectFirst("table[sid]")

    val synopsis = synopsisE?.getElementsByTag("p")?.text()
    val image = imageE?.attr("src")
    val title = titleE?.text()
    val sid = sidE?.attr("sid")

    val pageUrl = Constants.Api.BASE_URL.toUri()
        .buildUpon()
        .appendEncodedPath("shows")
        .appendEncodedPath(page)
        .toString()

    return ShowDetails(
        title = title,
        synopsis = synopsis,
        image = image?.toImageUrl(),
        sid = sid,
        page = page,
        pageUrl = pageUrl,
        episodes = episodeContentDTO.toDomain()
    )
}
