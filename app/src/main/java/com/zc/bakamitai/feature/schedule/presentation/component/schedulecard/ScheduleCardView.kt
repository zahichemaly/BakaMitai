package com.zc.bakamitai.feature.schedule.presentation.component.schedulecard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.coverimage.CoverImageView
import com.zc.bakamitai.common.ui.component.coverimage.POSTER_ASPECT_RATIO
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.StatusBadgeView
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model.ScheduleCardUIModel

private const val TITLE_LINES = 2
private const val SCRIM_MIDDLE_STOP = 0.5f
private const val SCRIM_MIDDLE_ALPHA = 0.4f
private const val TIME_CHIP_CONTAINER_ALPHA = 0.8f
private const val SKELETON_SECOND_LINE_FRACTION = 0.6f
private val TimeIconSize = 14.dp
private val TimeChipHorizontalPadding = 8.dp
private val TimeChipVerticalPadding = 2.dp
private val PreviewCardWidth = 180.dp

/**
 * A show on the selected day: the poster with its status (today only) and scheduled time, then the title on
 * two lines. TalkBack reads it as one item: status, time, title.
 *
 * @param onClick opens the show, or `null` when the card can't be opened (no ripple, not clickable).
 */
@Composable
fun ScheduleCardView(model: ScheduleCardUIModel, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors) {
            ScheduleCardContent(model)
        }
    } else {
        Card(modifier = modifier.semantics(mergeDescendants = true) {}, shape = shape, colors = colors) {
            ScheduleCardContent(model)
        }
    }
}

@Composable
private fun ColumnScope.ScheduleCardContent(model: ScheduleCardUIModel) {
    val colors = MaterialTheme.colorScheme
    val spacing = BakaMitaiTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(POSTER_ASPECT_RATIO),
    ) {
        CoverImageView(url = model.coverUrl, modifier = Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        SCRIM_MIDDLE_STOP to colors.surfaceDim.copy(alpha = SCRIM_MIDDLE_ALPHA),
                        1f to colors.surfaceContainerLow,
                    ),
                ),
        )
        model.status?.let { status ->
            StatusBadgeView(
                model = status,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(spacing.sm),
            )
        }
        TimeChip(
            timeLabel = model.timeLabel,
            isUpcoming = model.status?.isUpcoming == true,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(spacing.sm),
        )
    }
    Text(
        text = model.title.asString(),
        modifier = Modifier.padding(spacing.sm),
        color = colors.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = TITLE_LINES,
        minLines = TITLE_LINES,
        style = MaterialTheme.typography.titleMedium,
    )
}

/** The scheduled time: tertiary with an alarm for an upcoming show, otherwise a primary clock. */
@Composable
private fun TimeChip(timeLabel: UiText, isUpcoming: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surfaceContainerLowest.copy(alpha = TIME_CHIP_CONTAINER_ALPHA),
        contentColor = if (isUpcoming) colors.tertiary else colors.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TimeChipHorizontalPadding, vertical = TimeChipVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isUpcoming) Icons.Outlined.Alarm else Icons.Outlined.Schedule,
                contentDescription = null,
                modifier = Modifier.size(TimeIconSize),
                tint = if (isUpcoming) colors.tertiary else colors.primary,
            )
            Text(text = timeLabel.asString(), maxLines = 1, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Loading placeholder for [ScheduleCardView]: the same card with a poster block and two title lines, so it has
 * the real card's height. There is no badge: the status isn't known before loading. Use it inside a
 * `SkeletonView`.
 */
@Composable
fun ScheduleCardSkeletonView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(POSTER_ASPECT_RATIO),
            shape = RectangleShape,
        )
        Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.sm)) {
            SkeletonTextLine(style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            SkeletonTextLine(
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(SKELETON_SECOND_LINE_FRACTION),
            )
        }
    }
}

private val previewAired = StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_aired), false)
private val previewUpcoming = StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_upcoming), true)

/** Samples with whether the card is clickable. */
private class ScheduleCardPreviewProvider : PreviewParameterProvider<Pair<ScheduleCardUIModel, Boolean>> {
    override val values = sequenceOf(
        ScheduleCardUIModel(
            key = "bleach@17:00",
            slug = "bleach",
            title = UiText.StringValue("Bleach: Thousand-Year Blood War"),
            coverUrl = null,
            timeLabel = UiText.StringValue("5:00 PM"),
            status = previewAired,
        ) to true,
        ScheduleCardUIModel(
            key = "dungeon@21:00",
            slug = "dungeon",
            title = UiText.StringValue("Delicious in Dungeon"),
            coverUrl = null,
            timeLabel = UiText.StringValue("9:00 PM"),
            status = previewUpcoming,
        ) to false,
        ScheduleCardUIModel(
            key = "prince@11:00",
            slug = "prince",
            title = UiText.StringValue("The Prince of Tennis II - U-17 World Cup Final Member Selection Match"),
            coverUrl = null,
            timeLabel = UiText.StringValue("11:00 AM"),
            status = null,
        ) to false,
    )
}

@AdaptivePreviews
@Composable
private fun ScheduleCardViewPreview(
    @PreviewParameter(ScheduleCardPreviewProvider::class) sample: Pair<ScheduleCardUIModel, Boolean>,
) {
    val (model, isClickable) = sample
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                ScheduleCardView(
                    model = model,
                    onClick = if (isClickable) ({}) else null,
                    modifier = Modifier.width(PreviewCardWidth),
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun ScheduleCardSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                ScheduleCardSkeletonView(modifier = Modifier.width(PreviewCardWidth))
            }
        }
    }
}
