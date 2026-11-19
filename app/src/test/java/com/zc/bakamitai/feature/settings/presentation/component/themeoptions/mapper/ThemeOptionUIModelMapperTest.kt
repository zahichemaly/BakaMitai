package com.zc.bakamitai.feature.settings.presentation.component.themeoptions.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.LightMode
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeOptionUIModelMapperTest {

    @Test
    fun `three options in order with their texts`() {
        val options = ThemeMode.SYSTEM.toThemeOptionUIStates()

        assertEquals(listOf(ThemeOption.SYSTEM, ThemeOption.LIGHT, ThemeOption.DARK), options.map { it.option })
        assertEquals(
            listOf(
                StringResource.settings_theme_system,
                StringResource.settings_theme_light,
                StringResource.settings_theme_dark,
            ).map { UiText.StringResource(it) },
            options.map { it.title },
        )
        assertEquals(
            listOf(
                StringResource.settings_theme_system_description,
                StringResource.settings_theme_light_description,
                StringResource.settings_theme_dark_description,
            ).map { UiText.StringResource(it) },
            options.map { it.description },
        )
    }

    @Test
    fun `only the stored mode is selected, with the filled icon`() {
        val options = ThemeMode.DARK.toThemeOptionUIStates()

        assertEquals(listOf(false, false, true), options.map { it.isSelected })
        assertEquals(
            listOf(Icons.Outlined.Devices, Icons.Outlined.LightMode, Icons.Filled.DarkMode),
            options.map { it.icon },
        )
    }
}
