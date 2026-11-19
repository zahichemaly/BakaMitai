package com.zc.bakamitai.common.ui.component.timechip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private const val CONTAINER_ALPHA = 0.8f
private val IconSize = 14.dp
private val HorizontalPadding = 8.dp
private val VerticalPadding = 2.dp

/**
 * A time pill for a poster's corner: a translucent dark container with an icon and a `labelSmall` label, colored
 * by [TimeChipUIModel.tone]. Not clickable; TalkBack reads its label with the card it sits on.
 */
@Composable
fun TimeChipView(model: TimeChipUIModel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (icon, iconTint, textColor) = when (model.tone) {
        TimeChipTone.DEFAULT -> ChipStyle(Icons.Outlined.Schedule, colors.primary, colors.onSurface)
        TimeChipTone.UPCOMING -> ChipStyle(Icons.Outlined.Alarm, colors.tertiary, colors.tertiary)
        TimeChipTone.MUTED -> ChipStyle(Icons.Outlined.EventBusy, colors.onSurfaceVariant, colors.onSurfaceVariant)
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surfaceContainerLowest.copy(alpha = CONTAINER_ALPHA),
        contentColor = textColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = HorizontalPadding, vertical = VerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(IconSize), tint = iconTint)
            Text(
                text = model.label.asString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private data class ChipStyle(val icon: ImageVector, val iconTint: Color, val textColor: Color)

@AdaptivePreviews
@Composable
private fun TimeChipViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
            ) {
                TimeChipView(TimeChipUIModel(UiText.StringValue("MON 9:30 PM"), TimeChipTone.DEFAULT))
                TimeChipView(TimeChipUIModel(UiText.StringValue("TODAY 9:30 PM"), TimeChipTone.UPCOMING))
                TimeChipView(TimeChipUIModel(UiText.StringValue("NOT AIRING"), TimeChipTone.MUTED))
            }
        }
    }
}
