package com.zc.bakamitai.extensions

import com.zc.bakamitai.data.models.dtos.ShowDetailsDto
import org.jsoup.nodes.Document


fun Document.toShowDetailsDto(url: String): ShowDetailsDto {
    val imageE = this.selectFirst("img[class='img-responsive img-center']")
    val synopsisE = this.selectFirst("div.series-syn")
    val titleE = this.selectFirst("h1.entry-title")
    val sidE = this.selectFirst("table[sid]")

    val synopsis = synopsisE?.getElementsByTag("p")?.text()
    val image = imageE?.attr("src")
    val title = titleE?.text()
    val sid = sidE?.attr("sid")

    return ShowDetailsDto(synopsis, image?.toImageUrl(), title, sid, url)
}
