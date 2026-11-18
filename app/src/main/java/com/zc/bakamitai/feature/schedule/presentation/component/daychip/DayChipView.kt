package com.zc.bakamitai.feature.schedule.presentation.component.daychip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.skeleton.textLineHeight
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.model.DayChipUIModel
import kotlinx.datetime.DayOfWeek

private const val UNSELECTED_WEEKDAY_ALPHA = 0.75f
private val DayChipMinWidth = 52.dp
private val DayChipHeight = 64.dp
private val DayChipVerticalPadding = 4.dp
private val DayChipHorizontalPadding = 8.dp
private val TodayDotSize = 6.dp

/**
 * A day tab: the short weekday over this week's date, and a dot under today's date. The fill marks the selection,
 * the dot marks today, and the size never changes. TalkBack reads "Thursday 19, today", then "selected" and "tab".
 */
@Composable
fun DayChipView(model: DayChipUIModel, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val description = model.contentDescription.asString()
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .widthIn(min = DayChipMinWidth)
            .height(dayChipHeight())
            .semantics {
                role = Role.Tab
                contentDescription = description
            },
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primary else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .clearAndSetSemantics {}
                .padding(horizontal = DayChipHorizontalPadding, vertical = DayChipVerticalPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = model.weekdayLabel.asString(),
                color = LocalContentColor.current.copy(alpha = if (selected) 1f else UNSELECTED_WEEKDAY_ALPHA),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(text = model.dateLabel.asString(), maxLines = 1, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(BakaMitaiTheme.spacing.xs))
            // The dot's space is reserved on every chip, so the labels line up.
            val dotColor = when {
                !model.isToday -> Color.Transparent
                selected -> colors.onPrimary
                else -> colors.primary
            }
            Box(
                modifier = Modifier
                    .size(TodayDotSize)
                    .background(dotColor, CircleShape),
            )
        }
    }
}

/** Loading placeholder for [DayChipView]: a block of the chip's size and shape. Use it inside a `SkeletonView`. */
@Composable
fun DayChipSkeletonView(modifier: Modifier = Modifier) {
    SkeletonBlock(
        modifier = modifier.size(width = DayChipMinWidth, height = dayChipHeight()),
        shape = MaterialTheme.shapes.medium,
    )
}

/** The design's 64 dp, or more when a large font scale needs it, so the labels are never clipped. */
@Composable
@ReadOnlyComposable
private fun dayChipHeight(): Dp {
    val typography = MaterialTheme.typography
    val content = textLineHeight(typography.labelSmall) + textLineHeight(typography.headlineSmall) +
        BakaMitaiTheme.spacing.xs + TodayDotSize + DayChipVerticalPadding * 2
    return max(DayChipHeight, content)
}

/** Samples with whether the chip is selected: plain, today, selected, selected today. */
private class DayChipPreviewProvider : PreviewParameterProvider<Pair<DayChipUIModel, Boolean>> {
    private val monday = DayChipUIModel(
        dayOfWeek = DayOfWeek.MONDAY,
        weekdayLabel = UiText.StringResource(StringResource.day_short_monday),
        dateLabel = UiText.StringValue("16"),
        contentDescription = UiText.StringResource(
            StringResource.cd_schedule_day,
            listOf(UiText.StringResource(StringResource.day_monday), "16"),
        ),
        isToday = false,
    )
    private val thursday = DayChipUIModel(
        dayOfWeek = DayOfWeek.THURSDAY,
        weekdayLabel = UiText.StringResource(StringResource.day_short_thursday),
        dateLabel = UiText.StringValue("19"),
        contentDescription = UiText.StringResource(
            StringResource.cd_schedule_day_today,
            listOf(UiText.StringResource(StringResource.day_thursday), "19"),
        ),
        isToday = true,
    )

    override val values = sequenceOf(monday to false, thursday to false, monday to true, thursday to true)
}

@AdaptivePreviews
@Composable
private fun DayChipViewPreview(@PreviewParameter(DayChipPreviewProvider::class) sample: Pair<DayChipUIModel, Boolean>) {
    val (model, selected) = sample
    BakaMitaiTheme {
        Surface {
            Row(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                DayChipView(model = model, selected = selected, onClick = {})
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun DayChipSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs)) {
                    DayChipSkeletonView()
                    DayChipSkeletonView()
                }
            }
        }
    }
}
