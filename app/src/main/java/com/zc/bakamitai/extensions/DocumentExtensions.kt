package com.zc.bakamitai.extensions

import com.zc.bakamitai.data.models.dtos.ShowDetailsDto
import com.zc.bakamitai.data.models.dtos.ShowDto
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

fun Document.toShowDtoList(): List<ShowDto> {
    val shows = arrayListOf<ShowDto>()
    val divs = this.select("div.all-shows").select("a[title]")
    for (div in divs) {
        val href = div.attr("href")
        shows.add(
            ShowDto(
                title = div.attr("title"),
                link = href.parsePage(),
            )
        )
    }
    return shows
}
