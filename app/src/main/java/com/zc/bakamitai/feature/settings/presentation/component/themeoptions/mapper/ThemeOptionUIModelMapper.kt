package com.zc.bakamitai.feature.settings.presentation.component.themeoptions.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.LightMode
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.component.themeoptions.model.ThemeOptionUIState
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** System default, Light and Dark, in order; this mode selected, with its filled icon. */
fun ThemeMode.toThemeOptionUIStates(): ImmutableList<ThemeOptionUIState> = persistentListOf(
    ThemeOptionUIState(
        option = ThemeOption.SYSTEM,
        title = UiText.StringResource(StringResource.settings_theme_system),
        description = UiText.StringResource(StringResource.settings_theme_system_description),
        icon = if (this == ThemeMode.SYSTEM) Icons.Filled.Devices else Icons.Outlined.Devices,
        isSelected = this == ThemeMode.SYSTEM,
    ),
    ThemeOptionUIState(
        option = ThemeOption.LIGHT,
        title = UiText.StringResource(StringResource.settings_theme_light),
        description = UiText.StringResource(StringResource.settings_theme_light_description),
        icon = if (this == ThemeMode.LIGHT) Icons.Filled.LightMode else Icons.Outlined.LightMode,
        isSelected = this == ThemeMode.LIGHT,
    ),
    ThemeOptionUIState(
        option = ThemeOption.DARK,
        title = UiText.StringResource(StringResource.settings_theme_dark),
        description = UiText.StringResource(StringResource.settings_theme_dark_description),
        icon = if (this == ThemeMode.DARK) Icons.Filled.DarkMode else Icons.Outlined.DarkMode,
        isSelected = this == ThemeMode.DARK,
    ),
)
