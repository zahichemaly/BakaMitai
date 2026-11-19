package com.zc.bakamitai.feature.shows.presentation.component.showrow.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * A show's row in the catalogue.
 *
 * @property key unique in the list: several titles can share one slug.
 * @property slug the show to open.
 */
@Immutable
data class ShowRowUIModel(val key: String, val slug: String, val title: UiText)
