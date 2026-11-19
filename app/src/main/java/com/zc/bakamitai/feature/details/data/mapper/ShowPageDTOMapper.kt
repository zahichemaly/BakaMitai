package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.common.data.mapper.toCoverUrl
import com.zc.bakamitai.common.data.network.SUBSPLEASE_BASE_URL
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.details.data.dto.ShowPageDTO
import com.zc.bakamitai.feature.details.domain.model.ShowOverview

/** What SubsPlease writes in place of a synopsis it doesn't have yet (docs/4-details.md D6). */
private const val SYNOPSIS_PLACEHOLDER = "Synopsis will be added later"
private const val PARAGRAPH_SEPARATOR = "\n\n"
private const val SHOWS_PATH = "shows/"

private val WhitespaceRegex = Regex("""\s+""")

/**
 * The page's show, or `null` when it has no title. Whitespace is collapsed in the title and in each paragraph;
 * paragraphs are joined with a blank line. The placeholder synopsis counts as none.
 */
fun ShowPageDTO.toDomain(slug: ShowSlug): ShowOverview? {
    val pageTitle = title?.collapseWhitespace()?.ifEmpty { null }
    val synopsis = synopsisParagraphs
        .map { it.collapseWhitespace() }
        .filter { it.isNotEmpty() }
        .joinToString(PARAGRAPH_SEPARATOR)
        .takeUnless { it.isEmpty() || it.isSynopsisPlaceholder() }
    return pageTitle?.let {
        ShowOverview(
            show = Show(slug = slug, title = it, coverUrl = coverSrc.toCoverUrl()),
            synopsis = synopsis,
            webUrl = SUBSPLEASE_BASE_URL + SHOWS_PATH + slug.value + "/",
        )
    }
}

/** The `sid` for `f=show`: digits only, or `null` when the page has no usable one. */
fun ShowPageDTO.toSid(): String? = sid?.trim()?.takeIf { value -> value.isNotEmpty() && value.all { it in '0'..'9' } }

private fun String.collapseWhitespace(): String = replace(WhitespaceRegex, " ").trim()

private fun String.isSynopsisPlaceholder(): Boolean = trimEnd('.').equals(SYNOPSIS_PLACEHOLDER, ignoreCase = true)
