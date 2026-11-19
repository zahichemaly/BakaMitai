package com.zc.bakamitai.feature.settings.presentation.component.aboutrow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.settings.presentation.component.settingsicon.SettingsIconView

private val TrailingIconSize = 20.dp

/**
 * "About BakaMitai", a single line: a tap opens the source code page in the browser. TalkBack reads one button,
 * "double-tap to open in browser".
 */
@Composable
fun AboutRowView(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = BakaMitaiTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClickLabel = UiText.StringResource(StringResource.cd_settings_open_in_browser).asString(),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconView(icon = Icons.Outlined.Code)
        Text(
            text = UiText.StringResource(StringResource.settings_about_title).asString(),
            modifier = Modifier.weight(1f),
            color = colors.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(TrailingIconSize),
            tint = colors.outline,
        )
    }
}

@AdaptivePreviews
@Composable
private fun AboutRowViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            AboutRowView(onClick = {}, modifier = Modifier.padding(BakaMitaiTheme.spacing.xs))
        }
    }
}
