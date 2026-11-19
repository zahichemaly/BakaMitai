package com.zc.bakamitai.feature.shows.data.mapper

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.shows.data.dto.ShowLinkDTO

/** `/shows/<slug>`, relative or absolute, with or without a trailing `/`, query or fragment. Group 1 is the slug. */
private val ShowHrefRegex = Regex("""^(?:https?://[^/]+)?/shows/([^/?#]+)/?(?:[?#].*)?$""")
private val WhitespaceRegex = Regex("""\s+""")

/**
 * The show behind a link, or `null` when the link isn't a show page or has no usable title. The slug is opaque
 * (it may contain `_`); the title is the `title` attribute, else the link text, with whitespace collapsed. The
 * catalogue has no covers, so `coverUrl` is `null`.
 */
fun ShowLinkDTO.toDomain(): Show? {
    val slug = href?.trim()?.let { ShowHrefRegex.matchEntire(it) }?.groupValues?.get(1)
    val title = sequenceOf(title, text)
        .mapNotNull { it?.replace(WhitespaceRegex, " ")?.trim()?.ifEmpty { null } }
        .firstOrNull()
    return if (slug != null && title != null) Show(slug = ShowSlug(slug), title = title, coverUrl = null) else null
}

/** The usable links' shows, in page order. */
fun List<ShowLinkDTO>.toDomain(): List<Show> = mapNotNull { it.toDomain() }
