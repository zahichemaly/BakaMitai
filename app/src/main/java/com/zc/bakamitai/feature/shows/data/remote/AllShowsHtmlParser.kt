package com.zc.bakamitai.feature.shows.data.remote

import com.fleeksoft.ksoup.Ksoup
import com.zc.bakamitai.feature.shows.data.dto.ShowLinkDTO
import org.koin.core.annotation.Single

/**
 * Reads the show links of the all shows page (docs/api.md §4.1), the only class that knows Ksoup. Pure
 * extraction, in page order: Ksoup decodes the entities, and the repository decides what an empty result means.
 */
@Single
class AllShowsHtmlParser {

    /** Every `div.all-shows-link > a` inside `div.all-shows`; an empty list when the page has none. */
    fun parse(html: String): List<ShowLinkDTO> = Ksoup.parse(html = html)
        // Scoped to the list: the site menu links to /shows/ too.
        .select(SHOW_LINK_SELECTOR)
        .map { link ->
            ShowLinkDTO(
                href = link.attr(ATTRIBUTE_HREF).ifBlank { null },
                title = link.attr(ATTRIBUTE_TITLE).ifBlank { null },
                text = link.text().ifBlank { null },
            )
        }

    private companion object {
        const val SHOW_LINK_SELECTOR = "div.all-shows div.all-shows-link > a"
        const val ATTRIBUTE_HREF = "href"
        const val ATTRIBUTE_TITLE = "title"
    }
}
