package com.zc.bakamitai.feature.settings.presentation.component.settingssection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/**
 * A Settings section: an uppercase title in an accent color (a TalkBack heading), above a rounded card on the low
 * surface container that holds [content]. The rows carry the icons, not the header. Static chrome: it shows the
 * same while the settings load.
 *
 * @param contentPadding the card's inner padding.
 */
@Composable
fun SettingsSectionView(
    title: UiText,
    accentColor: Color,
    modifier: Modifier = Modifier,
    contentPadding: Dp = BakaMitaiTheme.spacing.md,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
    ) {
        Text(
            text = title.asString(),
            modifier = Modifier
                .padding(horizontal = BakaMitaiTheme.spacing.xs)
                .semantics { heading() },
            color = accentColor,
            style = MaterialTheme.typography.labelLarge,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@AdaptivePreviews
@Composable
private fun SettingsSectionViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xl),
            ) {
                SettingsSectionView(
                    title = UiText.StringResource(StringResource.settings_section_appearance),
                    accentColor = MaterialTheme.colorScheme.secondary,
                ) {
                    Text(text = UiText.StringResource(StringResource.settings_theme_title).asString())
                }
                SettingsSectionView(
                    title = UiText.StringResource(StringResource.settings_section_about),
                    accentColor = MaterialTheme.colorScheme.outline,
                    contentPadding = BakaMitaiTheme.spacing.xs,
                ) {
                    Text(text = UiText.StringResource(StringResource.settings_about_title).asString())
                }
            }
        }
    }
}
