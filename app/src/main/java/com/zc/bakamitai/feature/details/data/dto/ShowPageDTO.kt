package com.zc.bakamitai.feature.details.data.dto

/**
 * What a show page says about the show (docs/api.md §4.2), as found. Built by `ShowPageHtmlParser` from HTML, so
 * it isn't `@Serializable`. Blank values are `null`; blank paragraphs are left out.
 *
 * @property title the `h1.entry-title` text, entity-decoded.
 * @property synopsisParagraphs the texts of the `<p>` in `div.series-syn` (or its own text when it has none).
 * @property coverSrc the sidebar cover's `src`, usually site-relative.
 * @property sid the release table's `sid` attribute, the id `f=show` takes.
 */
data class ShowPageDTO(
    val title: String?,
    val synopsisParagraphs: List<String>,
    val coverSrc: String?,
    val sid: String?,
)
