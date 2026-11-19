package com.zc.bakamitai.common.ui.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText

private const val MOVIE_LABEL = "Movie"
private const val EN_DASH = "–"

/** "20", "12.5". */
private val NumberLabelRegex = Regex("""^\d+(\.\d+)?$""")

/** "1216v2": group 1 is the episode, group 2 the revision. */
private val RevisionLabelRegex = Regex("""^(\d+(?:\.\d+)?)v(\d+)$""", RegexOption.IGNORE_CASE)

/** "01-28": group 1 is the first episode, group 2 the last. */
private val RangeLabelRegex = Regex("""^(\d+)\s*-\s*(\d+)$""")

/**
 * An episode label as SubsPlease writes it, as a title: "Episode 20", "Episode 1216 (v2)", "Movie",
 * "Episodes 01–28"; anything else ("Egghead SP11") as it is.
 */
fun String.toEpisodeTitle(): UiText {
    val revision = RevisionLabelRegex.matchEntire(this)
    val range = RangeLabelRegex.matchEntire(this)
    return when {
        NumberLabelRegex.matches(this) -> UiText.StringResource(StringResource.episode_label_number, listOf(this))

        revision != null -> UiText.StringResource(
            StringResource.episode_label_revision,
            listOf(revision.groupValues[1], revision.groupValues[2]),
        )

        equals(MOVIE_LABEL, ignoreCase = true) -> UiText.StringResource(StringResource.episode_label_movie)

        range != null -> UiText.StringResource(
            StringResource.episode_label_batch,
            listOf(range.groupValues[1] + EN_DASH + range.groupValues[2]),
        )

        else -> UiText.StringValue(this)
    }
}
