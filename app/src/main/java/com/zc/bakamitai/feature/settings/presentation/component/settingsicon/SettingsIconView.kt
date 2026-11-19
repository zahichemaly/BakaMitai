package com.zc.bakamitai.feature.settings.presentation.component.settingsicon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/** The leading icon's circle: rows indent content that sits under their text by this plus their gap. */
val SettingsIconSize: Dp = 40.dp
private val GlyphSize = 20.dp

/**
 * A setting row's leading icon: [icon] in `onSurface` on a `surfaceContainerHigh` circle (the design's About icon).
 * Decorative: the row's text says what it is. Rows keep it in their skeletons (it's static).
 */
@Composable
fun SettingsIconView(icon: ImageVector, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(SettingsIconSize)
            .background(color = colors.surfaceContainerHigh, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(GlyphSize),
            tint = colors.onSurface,
        )
    }
}

@AdaptivePreviews
@Composable
private fun SettingsIconViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Row(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            ) {
                SettingsIconView(icon = Icons.Outlined.CalendarMonth)
                SettingsIconView(icon = Icons.Outlined.Schedule)
                SettingsIconView(icon = Icons.Outlined.Palette)
                SettingsIconView(icon = Icons.Outlined.Notifications)
                SettingsIconView(icon = Icons.Outlined.Code)
            }
        }
    }
}
