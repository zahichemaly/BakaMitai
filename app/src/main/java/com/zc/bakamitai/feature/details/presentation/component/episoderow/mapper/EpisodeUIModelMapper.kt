package com.zc.bakamitai.feature.details.presentation.component.episoderow.mapper

import com.zc.bakamitai.common.ui.mapper.toTimeLabel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.model.Episode
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.mapper.toResolutionUIModels
import com.zc.bakamitai.feature.details.presentation.component.episoderow.model.EpisodeUIState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

private const val EPISODE_KEY_PREFIX = "episode|"
private const val BATCH_KEY_PREFIX = "batch|"
private const val MOVIE_LABEL = "Movie"
private const val EN_DASH = "–"

/** "20", "12.5". */
private val NumberLabelRegex = Regex("""^\d+(\.\d+)?$""")

/** "1216v2": group 1 is the episode, group 2 the revision. */
private val RevisionLabelRegex = Regex("""^(\d+(?:\.\d+)?)v(\d+)$""", RegexOption.IGNORE_CASE)

/** "01-28": group 1 is the first episode, group 2 the last. */
private val RangeLabelRegex = Regex("""^(\d+)\s*-\s*(\d+)$""")

/** "Nov 17, 2026". */
private val ReleaseDateFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day(Padding.NONE)
    chars(", ")
    year()
}

/** The row's list key; SubsPlease keys entries by label, so it's unique within its list. */
fun Episode.toEpisodeKey(isBatch: Boolean): String = (if (isBatch) BATCH_KEY_PREFIX else EPISODE_KEY_PREFIX) + label

/**
 * The row of an episode or batch. Its release line is in [timeZone]: the date, "Nov 17, 2026", and the shared
 * 12-hour time.
 */
fun Episode.toEpisodeUIState(isBatch: Boolean, timeZone: TimeZone, isExpanded: Boolean): EpisodeUIState =
    EpisodeUIState(
        key = toEpisodeKey(isBatch),
        title = label.toEpisodeTitle(),
        releasedLabel = UiText.StringResource(
            StringResource.details_episode_released,
            listOf(
                UiText.StringValue(ReleaseDateFormat.format(releasedAt.toLocalDateTime(timeZone).date)),
                releasedAt.toTimeLabel(timeZone),
            ),
        ),
        resolutions = downloads.toResolutionUIModels(),
        isExpanded = isExpanded,
    )

/**
 * "Episode 20", "Episode 1216 (v2)", "Movie", "Episodes 01–28"; anything else ("Egghead SP11") as SubsPlease
 * writes it.
 */
private fun String.toEpisodeTitle(): UiText {
    val revision = RevisionLabelRegex.matchEntire(this)
    val range = RangeLabelRegex.matchEntire(this)
    return when {
        NumberLabelRegex.matches(this) -> UiText.StringResource(StringResource.details_episode, listOf(this))

        revision != null -> UiText.StringResource(
            StringResource.details_episode_revision,
            listOf(revision.groupValues[1], revision.groupValues[2]),
        )

        equals(MOVIE_LABEL, ignoreCase = true) -> UiText.StringResource(StringResource.details_episode_movie)

        range != null -> UiText.StringResource(
            StringResource.details_episode_batch,
            listOf(range.groupValues[1] + EN_DASH + range.groupValues[2]),
        )

        else -> UiText.StringValue(this)
    }
}
