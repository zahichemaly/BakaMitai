package com.zc.bakamitai.feature.details.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * The show's header: what its page says about it.
 *
 * @property coverUrl the absolute cover URL, or `null` for the placeholder poster.
 * @property synopsis the synopsis, paragraphs separated by a blank line, or `null` when the show has none yet.
 */
@Immutable
data class DetailsHeaderUIModel(val title: UiText, val coverUrl: String?, val synopsis: UiText?)
