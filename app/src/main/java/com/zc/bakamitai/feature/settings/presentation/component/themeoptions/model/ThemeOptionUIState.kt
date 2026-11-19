package com.zc.bakamitai.feature.settings.presentation.component.themeoptions.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption

/** One theme row: "Dark / Always use the dark theme", with its icon (filled when selected). */
@Immutable
data class ThemeOptionUIState(
    val option: ThemeOption,
    val title: UiText,
    val description: UiText,
    val icon: ImageVector,
    val isSelected: Boolean,
)
