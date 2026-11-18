package com.zc.bakamitai.feature.home.presentation.component.todaysummary

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.skeleton.textLineHeight
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel

private const val AIRING_CONTAINER_ALPHA = 0.1f
private const val AVAILABLE_CONTAINER_ALPHA = 0.2f
private const val PULSE_MIN_ALPHA = 0.3f
private const val PULSE_DURATION_MILLIS = 1_000
private val ZoneIconSize = 16.dp
private val StatusDotSize = 6.dp
private val ChipContentGap = 6.dp
private val ChipHorizontalPadding = 10.dp
private val ChipVerticalPadding = 4.dp
private val SecondaryGlowRadius = 112.dp
private val PrimaryGlowRadius = 96.dp
private val SkeletonOffsetWidth = 64.dp
private val SkeletonAiringChipWidth = 84.dp
private val SkeletonAvailableChipWidth = 100.dp

/**
 * The "Today's Broadcasts" banner: the device's UTC offset and how many shows are airing later today and
 * available already. TalkBack reads it as one item.
 */
@Composable
fun TodaySummaryView(model: TodaySummaryUIModel, modifier: Modifier = Modifier) {
    val glowPrimary = BakaMitaiTheme.extendedColors.glowPrimary
    val glowSecondary = BakaMitaiTheme.extendedColors.glowSecondary
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .drawBehind {
                    val topRight = Offset(size.width, 0f)
                    val bottomLeft = Offset(0f, size.height)
                    val secondaryRadius = SecondaryGlowRadius.toPx()
                    val primaryRadius = PrimaryGlowRadius.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowSecondary, Color.Transparent),
                            center = topRight,
                            radius = secondaryRadius,
                        ),
                        radius = secondaryRadius,
                        center = topRight,
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowPrimary, Color.Transparent),
                            center = bottomLeft,
                            radius = primaryRadius,
                        ),
                        radius = primaryRadius,
                        center = bottomLeft,
                    )
                }
                .padding(BakaMitaiTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        ) {
            SummaryHeader(utcOffsetLabel = model.utcOffsetLabel)
            Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm)) {
                SummaryChip(
                    text = model.upcomingCount,
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = AIRING_CONTAINER_ALPHA),
                    isPulsing = true,
                )
                SummaryChip(
                    text = model.releasedCount,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = AVAILABLE_CONTAINER_ALPHA,
                    ),
                    isPulsing = false,
                )
            }
        }
    }
}

/**
 * Loading placeholder for [TodaySummaryView]: the same banner with its real title, and blocks for the UTC offset
 * and the two counts. Use it inside a `SkeletonView`.
 */
@Composable
fun TodaySummarySkeletonView(modifier: Modifier = Modifier) {
    val chipHeight = textLineHeight(MaterialTheme.typography.labelSmall) + ChipVerticalPadding * 2
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = UiText.StringResource(StringResource.home_summary_title).asString(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
                SkeletonTextLine(
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(SkeletonOffsetWidth),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm)) {
                SkeletonBlock(
                    modifier = Modifier.size(width = SkeletonAiringChipWidth, height = chipHeight),
                    shape = CircleShape,
                )
                SkeletonBlock(
                    modifier = Modifier.size(width = SkeletonAvailableChipWidth, height = chipHeight),
                    shape = CircleShape,
                )
            }
        }
    }
}

@Composable
private fun SummaryHeader(utcOffsetLabel: UiText, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = UiText.StringResource(StringResource.home_summary_title).asString(),
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(ZoneIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = utcOffsetLabel.asString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SummaryChip(
    text: UiText,
    contentColor: Color,
    containerColor: Color,
    isPulsing: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Row(
            modifier = Modifier.padding(horizontal = ChipHorizontalPadding, vertical = ChipVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(ChipContentGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isPulsing) PulsingStatusDot(color = contentColor) else StatusDot(color = contentColor)
            Text(text = text.asString(), style = MaterialTheme.typography.labelSmall)
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

/** The pulse is read in the draw phase only, so it never recomposes the banner. */
@Composable
private fun PulsingStatusDot(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "statusDotPulse")
    val pulseAlpha = transition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MILLIS), RepeatMode.Reverse),
        label = "statusDotAlpha",
    )
    StatusDot(color = color, modifier = modifier.graphicsLayer { alpha = pulseAlpha.value })
}

private class TodaySummaryUIModelProvider : PreviewParameterProvider<TodaySummaryUIModel> {
    override val values = sequenceOf(
        TodaySummaryUIModel(
            utcOffsetLabel = UiText.StringResource(StringResource.utc_offset, listOf("-5")),
            upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, 4, listOf(4)),
            releasedCount = UiText.StringPlural(PluralResource.home_summary_available, 5, listOf(5)),
        ),
        TodaySummaryUIModel(
            utcOffsetLabel = UiText.StringResource(StringResource.utc_offset, listOf("")),
            upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, 0, listOf(0)),
            releasedCount = UiText.StringPlural(PluralResource.home_summary_available, 0, listOf(0)),
        ),
    )
}

@AdaptivePreviews
@Composable
private fun TodaySummaryViewPreview(@PreviewParameter(TodaySummaryUIModelProvider::class) model: TodaySummaryUIModel) {
    BakaMitaiTheme {
        Surface {
            // Surface passes its minimum size on: the Column lets the banner wrap its content.
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                TodaySummaryView(model = model)
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun TodaySummarySkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                TodaySummarySkeletonView()
            }
        }
    }
}
