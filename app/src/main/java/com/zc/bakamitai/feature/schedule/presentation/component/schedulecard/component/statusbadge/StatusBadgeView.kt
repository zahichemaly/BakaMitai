package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel

private const val AIRED_CONTAINER_ALPHA = 0.8f
private const val UPCOMING_CONTAINER_ALPHA = 0.3f
private const val UPCOMING_BASE_ALPHA = 0.8f
private const val PULSE_MIN_ALPHA = 0.3f
private const val PULSE_DURATION_MILLIS = 1_000
private val BadgeHorizontalPadding = 10.dp
private val BadgeVerticalPadding = 2.dp
private val StatusDotSize = 6.dp

/**
 * An Aired / Upcoming pill for a poster (DESIGN.md status badge): a status dot and the label. Aired is secondary
 * with a static dot; Upcoming is tertiary with a pulsing dot.
 */
@Composable
fun StatusBadgeView(model: StatusBadgeUIModel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (model.isUpcoming) colors.tertiary else colors.secondary
    val containerColor = if (model.isUpcoming) {
        // The design blurs the poster behind the pill; without the blur, the tint alone is unreadable on bright
        // covers, so it sits on the time chip's translucent dark base.
        colors.tertiaryContainer.copy(alpha = UPCOMING_CONTAINER_ALPHA)
            .compositeOver(colors.surfaceContainerLowest.copy(alpha = UPCOMING_BASE_ALPHA))
    } else {
        colors.surfaceContainerHighest.copy(alpha = AIRED_CONTAINER_ALPHA)
    }
    Surface(modifier = modifier, shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Row(
            modifier = Modifier.padding(horizontal = BadgeHorizontalPadding, vertical = BadgeVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (model.isUpcoming) PulsingStatusDot(color = contentColor) else StatusDot(color = contentColor)
            Text(text = model.label.asString(), maxLines = 1, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(StatusDotSize)
            .background(color, CircleShape),
    )
}

/** The pulse is read in the draw phase only, so it never recomposes the badge. */
@Composable
private fun PulsingStatusDot(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "statusBadgePulse")
    val pulseAlpha = transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MILLIS), RepeatMode.Reverse),
        label = "statusBadgeAlpha",
    )
    StatusDot(color = color, modifier = modifier.graphicsLayer { alpha = pulseAlpha.value })
}

@AdaptivePreviews
@Composable
private fun StatusBadgeViewPreview() {
    BakaMitaiTheme {
        Surface {
            Row(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
            ) {
                StatusBadgeView(
                    StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_aired), isUpcoming = false),
                )
                StatusBadgeView(
                    StatusBadgeUIModel(
                        UiText.StringResource(StringResource.schedule_status_upcoming),
                        isUpcoming = true,
                    ),
                )
            }
        }
    }
}
