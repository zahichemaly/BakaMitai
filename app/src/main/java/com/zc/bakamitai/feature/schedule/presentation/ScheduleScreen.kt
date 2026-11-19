package com.zc.bakamitai.feature.schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowSizeClass
import com.zc.bakamitai.common.ui.component.emptystate.EmptyStateView
import com.zc.bakamitai.common.ui.component.error.ErrorView
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.component.toplevelappbar.TopLevelAppBarView
import com.zc.bakamitai.common.ui.mapper.toDayName
import com.zc.bakamitai.common.ui.mapper.toShortDayName
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.DayChipSkeletonView
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.DayChipView
import com.zc.bakamitai.feature.schedule.presentation.component.daychip.model.DayChipUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.DayHeaderSkeletonView
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.DayHeaderView
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.DayHeaderUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model.StatusFilterChipUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.ScheduleCardSkeletonView
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.ScheduleCardView
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.component.statusbadge.model.StatusBadgeUIModel
import com.zc.bakamitai.feature.schedule.presentation.component.schedulecard.model.ScheduleCardUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.ScheduleDayUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.ScheduleEmptyUIModel
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DayOfWeek

private const val COMPACT_COLUMNS = 2
private const val MEDIUM_COLUMNS = 3
private const val EXPANDED_COLUMNS = 4
private const val DAYS_PER_WEEK = 7
private const val SKELETON_CARD_ROWS = 3
private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_EMPTY = "empty"
private const val CONTENT_TYPE_CARD = "card"
private const val CONTENT_TYPE_DAY = "day"
private const val PREVIEW_FIRST_DATE = 16

private val fullLineSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

/**
 * Schedule: the week's day tabs pinned under the top bar, then the selected day's header and its poster grid.
 *
 * @param showSettingsAction whether the top bar shows the settings action; `false` until Settings exists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    state: ScheduleState,
    onIntent: (ScheduleIntent) -> Unit,
    modifier: Modifier = Modifier,
    showSettingsAction: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // One bar state per day: a newly selected day starts at the top, so the bar tint starts over too.
    val appBarState = key(state.selectedDay) { rememberTopAppBarState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)
    val onDayClick = remember(onIntent) { { day: DayOfWeek -> onIntent(ScheduleIntent.DaySelected(day)) } }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopLevelAppBarView(
                subtitle = UiText.StringResource(StringResource.schedule_app_bar_subtitle),
                showSettingsAction = showSettingsAction,
                onSettingsClick = { onIntent(ScheduleIntent.SettingsClicked) },
                scrollBehavior = scrollBehavior,
                bottomContent = if (state.hasContent) {
                    { DayStrip(days = state.days, selectedDay = state.selectedDay, onDayClick = onDayClick) }
                } else {
                    null
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Default content insets: Scaffold leaves out what the navigation suite consumed. Its bottom bar consumes
        // the bottom system bar, but its rail doesn't, so on medium and wider windows the content pads for it.
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when {
            state.day != null -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(ScheduleIntent.Refresh) },
                modifier = contentModifier,
            ) {
                // Each day opens at the top, and keeps its scroll position across rotation.
                key(state.selectedDay) {
                    ScheduleContent(
                        day = state.day,
                        statusFilter = state.statusFilter,
                        onIntent = onIntent,
                    )
                }
            }

            state.error != null -> ErrorView(
                model = state.error,
                onRetry = { onIntent(ScheduleIntent.Retry) },
                modifier = contentModifier,
            )

            else -> ScheduleSkeleton(modifier = contentModifier)
        }
    }
}

/**
 * Window-size dependent layout values, read in the composable (never stored in state).
 *
 * @property margin the content's side margin: wider from medium widths up.
 * @property columns poster columns: 2 / 3 / 4 for compact / medium / expanded.
 */
private class ScheduleLayout(windowSizeClass: WindowSizeClass) {
    private val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    private val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val margin: Dp = if (isMedium) BakaMitaiTheme.spacing.marginTablet else BakaMitaiTheme.spacing.margin
    val columns: Int = when {
        isExpanded -> EXPANDED_COLUMNS
        isMedium -> MEDIUM_COLUMNS
        else -> COMPACT_COLUMNS
    }
}

/**
 * The day tabs. On a phone they scroll: the selected tab and its left neighbour are kept in view.
 */
@Composable
private fun DayStrip(
    days: ImmutableList<DayChipUIModel>,
    selectedDay: DayOfWeek?,
    onDayClick: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = ScheduleLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val selectedIndex = remember(days, selectedDay) { days.indexOfFirst { it.dayOfWeek == selectedDay } }
    val firstVisibleIndex = (selectedIndex - 1).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleIndex)
    LaunchedEffect(firstVisibleIndex) { listState.animateScrollToItem(firstVisibleIndex) }
    DayStripRow(margin = layout.margin, listState = listState, modifier = modifier.selectableGroup()) {
        items(items = days, key = { it.dayOfWeek.name }, contentType = { CONTENT_TYPE_DAY }) { day ->
            DayChipView(
                model = day,
                selected = day.dayOfWeek == selectedDay,
                onClick = { onDayClick(day.dayOfWeek) },
            )
        }
    }
}

/** The row behind both the day tabs and their skeleton, so the two line up exactly. */
@Composable
private fun DayStripRow(
    margin: Dp,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(horizontal = margin, vertical = BakaMitaiTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

@Composable
private fun ScheduleContent(
    day: ScheduleDayUIModel,
    statusFilter: StatusFilter,
    onIntent: (ScheduleIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = ScheduleLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val onShowClick = remember(onIntent) { { slug: String -> onIntent(ScheduleIntent.ShowClicked(slug)) } }
    val onFilterClick = remember(onIntent) {
        { filter: StatusFilter -> onIntent(ScheduleIntent.FilterSelected(filter)) }
    }
    val onResetClick = remember(onIntent) { { onIntent(ScheduleIntent.ResetFilterClicked) } }
    ScheduleGrid(layout = layout, gridState = rememberLazyGridState(), modifier = modifier) {
        item(key = "header", span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) {
            DayHeaderView(model = day.header, selectedFilter = statusFilter, onFilterClick = onFilterClick)
        }
        day.empty?.let { empty ->
            item(key = "empty", span = fullLineSpan, contentType = CONTENT_TYPE_EMPTY) {
                ScheduleEmptyView(model = empty, onResetClick = onResetClick)
            }
        }
        items(items = day.cards, key = { it.key }, contentType = { CONTENT_TYPE_CARD }) { card ->
            ScheduleCardView(model = card, onClick = { onShowClick(card.slug) })
        }
    }
}

/** The grid behind both the content and its skeleton, so the two line up exactly. */
@Composable
private fun ScheduleGrid(
    layout: ScheduleLayout,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    val spacing = BakaMitaiTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Fixed(layout.columns),
        modifier = modifier.fillMaxSize(),
        state = gridState,
        contentPadding = PaddingValues(horizontal = layout.margin, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(spacing.gutter),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

/**
 * First load: the day tabs where the strip will be, then the grid with the header and a few rows of cards, so
 * nothing moves when the data arrives. The top bar above is real.
 */
@Composable
private fun ScheduleSkeleton(modifier: Modifier = Modifier) {
    val layout = ScheduleLayout(currentWindowAdaptiveInfo().windowSizeClass)
    SkeletonView(modifier = modifier) {
        Column {
            DayStripRow(margin = layout.margin, listState = rememberLazyListState(), userScrollEnabled = false) {
                items(count = DAYS_PER_WEEK, contentType = { CONTENT_TYPE_DAY }) { DayChipSkeletonView() }
            }
            ScheduleGrid(layout = layout, gridState = rememberLazyGridState(), userScrollEnabled = false) {
                item(span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) { DayHeaderSkeletonView() }
                items(count = layout.columns * SKELETON_CARD_ROWS, contentType = { CONTENT_TYPE_CARD }) {
                    ScheduleCardSkeletonView()
                }
            }
        }
    }
}

/** No card to show: the day has no shows, or the filter hides them all (then "Reset Filters"). */
@Composable
private fun ScheduleEmptyView(model: ScheduleEmptyUIModel, onResetClick: () -> Unit, modifier: Modifier = Modifier) {
    EmptyStateView(
        icon = Icons.Outlined.CalendarViewDay,
        title = model.title,
        message = model.message,
        modifier = modifier,
        action = if (model.showResetAction) {
            {
                Button(onClick = onResetClick) {
                    Text(text = UiText.StringResource(StringResource.schedule_filter_reset).asString())
                }
            }
        } else {
            null
        },
    )
}

@AdaptivePreviews
@Composable
private fun ScheduleScreenPreview(@PreviewParameter(ScheduleStatePreviewProvider::class) state: ScheduleState) {
    BakaMitaiTheme {
        ScheduleScreen(state = state, onIntent = {}, showSettingsAction = false)
    }
}

private val previewDays = DayOfWeek.entries.mapIndexed { index, day ->
    val date = UiText.StringValue((PREVIEW_FIRST_DATE + index).toString())
    val isToday = day == DayOfWeek.THURSDAY
    DayChipUIModel(
        dayOfWeek = day,
        weekdayLabel = day.toShortDayName(),
        dateLabel = date,
        contentDescription = UiText.StringResource(
            if (isToday) StringResource.cd_schedule_day_today else StringResource.cd_schedule_day,
            listOf(day.toDayName(), date),
        ),
        isToday = isToday,
    )
}.toImmutableList()

private val previewAired = StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_aired), false)
private val previewUpcoming = StatusBadgeUIModel(UiText.StringResource(StringResource.schedule_status_upcoming), true)

private fun previewCard(title: String, time: String, status: StatusBadgeUIModel?) = ScheduleCardUIModel(
    key = "$title@$time",
    slug = title,
    title = UiText.StringValue(title),
    coverUrl = null,
    time = TimeChipUIModel(
        label = UiText.StringValue(time),
        tone = if (status?.isUpcoming == true) TimeChipTone.UPCOMING else TimeChipTone.DEFAULT,
    ),
    status = status,
)

private val previewTodayCards = persistentListOf(
    previewCard("Bleach: Thousand-Year Blood War", "5:00 PM", previewAired),
    previewCard("Oshi no Ko Season 2", "6:30 PM", previewAired),
    previewCard("Delicious in Dungeon", "9:00 PM", previewUpcoming),
    previewCard("Spy x Family Season 3", "10:00 PM", previewUpcoming),
)

private val previewTodayHeader = DayHeaderUIModel(
    title = UiText.StringResource(StringResource.day_thursday),
    subtitle = UiText.StringResource(
        StringResource.schedule_time_zone,
        listOf(UiText.StringResource(StringResource.utc_offset, listOf("+2"))),
    ),
    countLabel = UiText.StringPlural(PluralResource.schedule_show_count, 4, listOf(4)),
    filters = persistentListOf(
        StatusFilterChipUIModel(StatusFilter.ALL, UiText.StringResource(StringResource.schedule_filter_all, listOf(4))),
        StatusFilterChipUIModel(
            StatusFilter.AIRED,
            UiText.StringResource(StringResource.schedule_filter_aired, listOf(2)),
        ),
        StatusFilterChipUIModel(
            StatusFilter.UPCOMING,
            UiText.StringResource(StringResource.schedule_filter_upcoming, listOf(2)),
        ),
    ),
)

private val previewToday = ScheduleState(
    days = previewDays,
    selectedDay = DayOfWeek.THURSDAY,
    day = ScheduleDayUIModel(header = previewTodayHeader, cards = previewTodayCards, empty = null),
)

private val previewFridayHeader = previewTodayHeader.copy(
    title = UiText.StringResource(StringResource.day_friday),
    countLabel = UiText.StringPlural(PluralResource.schedule_show_count, 2, listOf(2)),
    filters = persistentListOf(),
)

/** Loading; today (All, then Upcoming); another day; filter-empty; day-empty; refreshing; error. */
private class ScheduleStatePreviewProvider : PreviewParameterProvider<ScheduleState> {
    override val values = sequenceOf(
        ScheduleState(isLoading = true),
        previewToday,
        previewToday.copy(
            statusFilter = StatusFilter.UPCOMING,
            day = previewToday.day?.copy(
                cards = previewTodayCards.filter { it.status == previewUpcoming }.toImmutableList(),
            ),
        ),
        previewToday.copy(
            selectedDay = DayOfWeek.FRIDAY,
            day = ScheduleDayUIModel(
                header = previewFridayHeader,
                cards = persistentListOf(
                    previewCard("Kusuriya no Hitorigoto", "10:00 AM", null),
                    previewCard("Rilakkuma", "9:00 PM", null),
                ),
                empty = null,
            ),
        ),
        previewToday.copy(
            statusFilter = StatusFilter.UPCOMING,
            day = ScheduleDayUIModel(
                header = previewTodayHeader,
                cards = persistentListOf(),
                empty = ScheduleEmptyUIModel(
                    title = UiText.StringResource(StringResource.schedule_filter_empty_title),
                    message = UiText.StringResource(StringResource.schedule_filter_empty_message),
                    showResetAction = true,
                ),
            ),
        ),
        previewToday.copy(
            selectedDay = DayOfWeek.SATURDAY,
            day = ScheduleDayUIModel(
                header = previewFridayHeader.copy(
                    title = UiText.StringResource(StringResource.day_saturday),
                    countLabel = UiText.StringPlural(PluralResource.schedule_show_count, 0, listOf(0)),
                ),
                cards = persistentListOf(),
                empty = ScheduleEmptyUIModel(
                    title = UiText.StringResource(StringResource.schedule_day_empty_title),
                    message = UiText.StringResource(
                        StringResource.schedule_day_empty_message,
                        listOf(UiText.StringResource(StringResource.day_saturday)),
                    ),
                    showResetAction = false,
                ),
            ),
        ),
        previewToday.copy(isRefreshing = true),
        ScheduleState(error = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)),
    )
}
