package com.zc.bakamitai.feature.settings.presentation.component.settingsfooter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private val DisclaimerMaxWidth = 320.dp

/** The app's version ("BakaMitai v2.0.0") and the unofficial-app disclaimer, centered. */
@Composable
fun SettingsFooterView(versionLabel: UiText, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = BakaMitaiTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = versionLabel.asString(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = UiText.StringResource(StringResource.settings_footer_disclaimer).asString(),
            modifier = Modifier.widthIn(max = DisclaimerMaxWidth),
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@AdaptivePreviews
@Composable
private fun SettingsFooterViewPreview() {
    BakaMitaiTheme {
        Surface {
            SettingsFooterView(
                versionLabel = UiText.StringResource(StringResource.settings_footer_version, listOf("2.0.0")),
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            )
        }
    }
}
