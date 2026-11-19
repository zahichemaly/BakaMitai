package com.zc.bakamitai.feature.settings.presentation.component.aboutrow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

private val IconCircleSize = 40.dp
private val IconSize = 20.dp

/**
 * "About BakaMitai" with the source code URL: a tap opens it in the browser. TalkBack reads one button, "double-tap
 * to open in browser".
 *
 * @param urlLabel the URL without its scheme, "github.com/zahichemaly/BakaMitai".
 */
@Composable
fun AboutRowView(urlLabel: UiText, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
        Box(
            modifier = Modifier
                .size(IconCircleSize)
                .background(color = colors.surfaceContainerHigh, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                modifier = Modifier.size(IconSize),
                tint = colors.onSurface,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = UiText.StringResource(StringResource.settings_about_title).asString(),
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(text = urlLabel.asString(), color = colors.primary, style = MaterialTheme.typography.labelSmall)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(IconSize),
            tint = colors.outline,
        )
    }
}

@AdaptivePreviews
@Composable
private fun AboutRowViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            AboutRowView(
                urlLabel = UiText.StringValue("github.com/zahichemaly/BakaMitai"),
                onClick = {},
                modifier = Modifier.padding(BakaMitaiTheme.spacing.xs),
            )
        }
    }
}
