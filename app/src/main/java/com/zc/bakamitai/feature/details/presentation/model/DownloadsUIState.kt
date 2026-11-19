package com.zc.bakamitai.feature.details.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.presentation.component.episoderow.model.EpisodeUIState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** The downloads section, which loads after the header (docs/4-details.md §5.10). */
@Immutable
sealed interface DownloadsUIState {

    /** Flips the row with [key] in [Content]; any other state is returned unchanged. */
    fun withEpisodeToggled(key: String): DownloadsUIState = when (this) {
        is Content -> copy(batches = batches.toggled(key), episodes = episodes.toggled(key))
        Loading, Empty, is Error -> this
    }

    @Immutable
    data object Loading : DownloadsUIState

    @Immutable
    data class Error(val error: ErrorUIModel) : DownloadsUIState

    /** The show has neither episodes nor batches. */
    @Immutable
    data object Empty : DownloadsUIState

    /**
     * @property countLabel "20 EPISODES", or `null` when there are only batches.
     * @property limitNote set when SubsPlease's episode cap was hit: older episodes aren't listed.
     */
    @Immutable
    data class Content(
        val countLabel: UiText?,
        val batches: ImmutableList<EpisodeUIState>,
        val episodes: ImmutableList<EpisodeUIState>,
        val limitNote: UiText?,
    ) : DownloadsUIState {

        /** The keys of the expanded rows, kept across a refresh. */
        val expandedKeys: Set<String>
            get() = (batches.asSequence() + episodes.asSequence()).filter { it.isExpanded }.map { it.key }.toSet()
    }
}

private fun ImmutableList<EpisodeUIState>.toggled(key: String): ImmutableList<EpisodeUIState> =
    if (none { it.key == key }) {
        this
    } else {
        map { if (it.key == key) it.copy(isExpanded = !it.isExpanded) else it }.toImmutableList()
    }
