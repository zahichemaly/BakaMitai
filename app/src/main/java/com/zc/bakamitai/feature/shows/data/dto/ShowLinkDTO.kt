package com.zc.bakamitai.feature.shows.data.dto

/**
 * One show link of the all shows page (docs/api.md §4.1), as found: `<a href="/shows/<slug>" title="…">…</a>`.
 * Built by `AllShowsHtmlParser` from HTML, so it isn't `@Serializable`. Blank values are `null`.
 *
 * @property href the link's `href`, e.g. `/shows/86-eighty-six`.
 * @property title the `title` attribute, entity-decoded.
 * @property text the link's text, entity-decoded, nested tags flattened.
 */
data class ShowLinkDTO(val href: String?, val title: String?, val text: String?)
