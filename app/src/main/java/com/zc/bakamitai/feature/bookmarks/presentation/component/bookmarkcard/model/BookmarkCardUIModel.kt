package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.text.UiText

/**
 * A bookmarked show's poster card.
 *
 * @property coverUrl absolute cover URL, or `null` for the placeholder.
 * @property addedLabel "Added Nov 18, 2026".
 * @property airing the next airing: "TODAY 9:30 PM", "MON 9:30 PM" or "NOT AIRING".
 * @property removeLabel the remove button's label for TalkBack, "Remove Frieren from bookmarks".
 */
@Immutable
data class BookmarkCardUIModel(
    val slug: String,
    val title: UiText,
    val coverUrl: String?,
    val addedLabel: UiText,
    val airing: TimeChipUIModel,
    val removeLabel: UiText,
)
