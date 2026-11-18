package com.zc.bakamitai.feature.schedule.presentation.component.dayheader

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BroadcastOnHome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
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
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.DayHeaderUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.StatusFilterChipUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val TITLE_LINES = 2
private const val SKELETON_TITLE_FRACTION = 0.6f
private val TitleIconSize = 20.dp
private val PillIconSize = 16.dp
private val PillHorizontalPadding = 12.dp
private val PillVerticalPadding = 6.dp
private val PillContentGap = 6.dp
private val SkeletonSubtitleWidth = 112.dp
private val SkeletonPillWidth = 124.dp
private val SkeletonFilterWidths = listOf(64.dp, 84.dp, 108.dp)

/**
 * The selected day's header: "Thursday Schedule" (a TalkBack heading), the device's offset, the show count, and
 * the status filter chips when the day has statuses (today).
 */
@Composable
fun DayHeaderView(
    model: DayHeaderUIModel,
    selectedFilter: StatusFilter,
    onFilterClick: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                DayTitle(title = model.title)
                Text(
                    text = model.subtitle.asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            CountPill(countLabel = model.countLabel)
        }
        if (model.filters.isNotEmpty()) {
            StatusFilterRow(filters = model.filters, selectedFilter = selectedFilter, onFilterClick = onFilterClick)
        }
    }
}

@Composable
private fun DayTitle(title: UiText, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(TitleIconSize),
            tint = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = title.asString(),
            modifier = Modifier
                .weight(1f, fill = false)
                .semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = TITLE_LINES,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

/** "6 Shows Airing": TalkBack reads it as one item; its icon is decorative. */
@Composable
private fun CountPill(countLabel: UiText, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(PillContentGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.BroadcastOnHome,
                contentDescription = null,
                modifier = Modifier.size(PillIconSize),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(text = countLabel.asString(), maxLines = 1, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun StatusFilterRow(
    filters: ImmutableList<StatusFilterChipUIModel>,
    selectedFilter: StatusFilter,
    onFilterClick: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = colors.surfaceContainerHigh,
        labelColor = colors.onSurfaceVariant,
        selectedContainerColor = colors.primary,
        selectedLabelColor = colors.onPrimary,
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
    ) {
        filters.forEach { chip ->
            FilterChip(
                selected = chip.filter == selectedFilter,
                onClick = { onFilterClick(chip.filter) },
                label = { Text(text = chip.label.asString(), style = MaterialTheme.typography.labelMedium) },
                shape = CircleShape,
                colors = chipColors,
                border = null,
            )
        }
    }
}

/**
 * Loading placeholder for [DayHeaderView]: lines for the title and the offset, the count pill, and the three
 * filter chips (today opens first, and today has them). Use it inside a `SkeletonView`.
 */
@Composable
fun DayHeaderSkeletonView(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    val pillHeight = maxOf(textLineHeight(typography.labelSmall), PillIconSize) + PillVerticalPadding * 2
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SkeletonTextLine(
                    style = typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION),
                )
                SkeletonTextLine(style = typography.labelMedium, modifier = Modifier.width(SkeletonSubtitleWidth))
            }
            SkeletonBlock(modifier = Modifier.size(width = SkeletonPillWidth, height = pillHeight), shape = CircleShape)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs)) {
            SkeletonFilterWidths.forEach { width ->
                // Each filter chip keeps the 48 dp touch target around its 32 dp pill.
                Box(modifier = Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
                    SkeletonBlock(
                        modifier = Modifier.size(width = width, height = FilterChipDefaults.Height),
                        shape = CircleShape,
                    )
                }
            }
        }
    }
}

private val previewFilters = persistentListOf(
    StatusFilterChipUIModel(StatusFilter.ALL, UiText.StringResource(StringResource.schedule_filter_all, listOf(6))),
    StatusFilterChipUIModel(StatusFilter.AIRED, UiText.StringResource(StringResource.schedule_filter_aired, listOf(5))),
    StatusFilterChipUIModel(
        StatusFilter.UPCOMING,
        UiText.StringResource(StringResource.schedule_filter_upcoming, listOf(1)),
    ),
)

private fun previewHeader(dayName: UiText, count: Int, filters: ImmutableList<StatusFilterChipUIModel>) =
    DayHeaderUIModel(
        title = dayName,
        subtitle = UiText.StringResource(
            StringResource.schedule_time_zone,
            listOf(UiText.StringResource(StringResource.utc_offset, listOf("+2"))),
        ),
        countLabel = UiText.StringPlural(PluralResource.schedule_show_count, count, listOf(count)),
        filters = filters,
    )

/** Samples with the selected filter: each filter on today, a day without filters, a long day name. */
private class DayHeaderPreviewProvider : PreviewParameterProvider<Pair<DayHeaderUIModel, StatusFilter>> {
    private val today = previewHeader(
        UiText.StringResource(StringResource.day_thursday),
        count = 6,
        filters = previewFilters,
    )

    override val values = sequenceOf(
        today to StatusFilter.ALL,
        today to StatusFilter.AIRED,
        today to StatusFilter.UPCOMING,
        previewHeader(
            UiText.StringResource(StringResource.day_friday),
            count = 1,
            filters = persistentListOf(),
        ) to StatusFilter.ALL,
        previewHeader(
            UiText.StringResource(StringResource.day_wednesday),
            count = 14,
            filters = persistentListOf(),
        ) to StatusFilter.ALL,
    )
}

@AdaptivePreviews
@Composable
private fun DayHeaderViewPreview(
    @PreviewParameter(DayHeaderPreviewProvider::class) sample: Pair<DayHeaderUIModel, StatusFilter>,
) {
    val (model, selectedFilter) = sample
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                DayHeaderView(model = model, selectedFilter = selectedFilter, onFilterClick = {})
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun DayHeaderSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                DayHeaderSkeletonView()
            }
        }
    }
}
