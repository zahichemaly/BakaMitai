package com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * One resolution slot of an episode and its three sources. A missing link disables its button; a slot without
 * any link isn't released.
 *
 * @property label "1080p FHD", "720p HD", "480p SD", or "SD" for a missing SD slot.
 */
@Immutable
data class ResolutionUIModel(
    val label: UiText,
    val tone: ResolutionTone,
    val magnetUri: String?,
    val torrentUrl: String?,
    val xdccUrl: String?,
) {
    val isAvailable: Boolean get() = magnetUri != null || torrentUrl != null || xdccUrl != null
}
