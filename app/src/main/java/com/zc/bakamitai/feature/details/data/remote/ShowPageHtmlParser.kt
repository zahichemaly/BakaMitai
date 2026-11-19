package com.zc.bakamitai.feature.details.data.remote

import com.fleeksoft.ksoup.Ksoup
import com.zc.bakamitai.feature.details.data.dto.ShowPageDTO
import org.koin.core.annotation.Single

/**
 * Reads a show page (docs/api.md §4.2). Pure extraction: Ksoup decodes the entities and flattens tags, and the
 * mapper decides what's usable (a missing title or `sid` is a parsing error there).
 */
@Single
class ShowPageHtmlParser {

    fun parse(html: String): ShowPageDTO {
        val document = Ksoup.parse(html = html)
        val synopsis = document.selectFirst(SYNOPSIS_SELECTOR)
        // Only the <p>s: the <h2>Synopsis</h2> heading isn't part of the text.
        val paragraphs = synopsis?.select(PARAGRAPH_SELECTOR)?.map { it.text() }?.filter { it.isNotBlank() }.orEmpty()
        return ShowPageDTO(
            title = document.selectFirst(TITLE_SELECTOR)?.text()?.ifBlank { null },
            // A synopsis written without <p> is the block's own text.
            synopsisParagraphs = paragraphs.ifEmpty { listOfNotNull(synopsis?.ownText()?.ifBlank { null }) },
            coverSrc = document.selectFirst(COVER_SELECTOR)?.attr(ATTRIBUTE_SRC)?.ifBlank { null },
            sid = document.selectFirst(RELEASE_TABLE_SELECTOR)?.attr(ATTRIBUTE_SID)?.ifBlank { null },
        )
    }

    private companion object {
        const val TITLE_SELECTOR = "h1.entry-title"
        const val SYNOPSIS_SELECTOR = "div.series-syn"
        const val PARAGRAPH_SELECTOR = "p"
        const val COVER_SELECTOR = "#secondary img.img-responsive"
        const val RELEASE_TABLE_SELECTOR = "table#show-release-table"
        const val ATTRIBUTE_SRC = "src"
        const val ATTRIBUTE_SID = "sid"
    }
}
