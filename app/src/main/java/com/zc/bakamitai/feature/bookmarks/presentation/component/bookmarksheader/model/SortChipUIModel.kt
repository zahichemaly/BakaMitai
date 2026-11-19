package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption

/** One sort chip: its option, label and leading icon. */
@Immutable
data class SortChipUIModel(val option: BookmarkSortOption, val label: UiText, val icon: ImageVector)
