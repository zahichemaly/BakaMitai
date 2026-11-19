package com.zc.bakamitai.feature.home.presentation

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowSizeClass
import com.zc.bakamitai.common.ui.component.error.ErrorView
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.toplevelappbar.TopLevelAppBarView
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.home.presentation.component.releaserow.ReleaseRowSkeletonView
import com.zc.bakamitai.feature.home.presentation.component.releaserow.ReleaseRowView
import com.zc.bakamitai.feature.home.presentation.component.releaserow.model.ReleaseRowUIModel
import com.zc.bakamitai.feature.home.presentation.component.sectionheader.SectionHeaderView
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.TodaySummarySkeletonView
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.TodaySummaryView
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.UpcomingCardSkeletonView
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.UpcomingCardView
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.UpcomingCardWidth
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.model.UpcomingCardUIModel
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val COMPACT_COLUMNS = 1
private const val MEDIUM_COLUMNS = 2
private const val EXPANDED_COLUMNS = 3
private const val CONTENT_TYPE_SUMMARY = "summary"
private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_CAROUSEL = "carousel"
private const val CONTENT_TYPE_EMPTY = "empty"
private const val CONTENT_TYPE_RELEASE = "release"
private const val UPCOMING_SKELETON_COUNT = 3
private const val LATEST_SKELETON_ROWS = 3

private val fullLineSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

/**
 * Home: today's "Upcoming" carousel and "Latest Releases" list, under the "Today's Broadcasts" banner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopLevelAppBarView(
                subtitle = UiText.StringResource(StringResource.home_app_bar_subtitle),
                onSettingsClick = { onIntent(HomeIntent.SettingsClicked) },
                scrollBehavior = scrollBehavior,
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
            state.summary != null -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(HomeIntent.Refresh) },
                modifier = contentModifier,
            ) {
                if (state.isEmpty) {
                    HomeEmptyView(summary = state.summary)
                } else {
                    HomeContent(state = state, onIntent = onIntent)
                }
            }

            state.error != null -> ErrorView(
                model = state.error,
                onRetry = { onIntent(HomeIntent.Retry) },
                modifier = contentModifier,
            )

            else -> HomeSkeleton(modifier = contentModifier)
        }
    }
}

/**
 * Window-size dependent layout values, read in the composable (never stored in state).
 *
 * @property margin the content's side margin: wider from medium widths up.
 * @property latestColumns Latest Releases columns: 1 / 2 / 3 for compact / medium / expanded.
 */
private class HomeLayout(windowSizeClass: WindowSizeClass) {
    private val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    private val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val margin: Dp = if (isMedium) BakaMitaiTheme.spacing.marginTablet else BakaMitaiTheme.spacing.margin
    val latestColumns: Int = when {
        isExpanded -> EXPANDED_COLUMNS
        isMedium -> MEDIUM_COLUMNS
        else -> COMPACT_COLUMNS
    }
}

@Composable
private fun HomeContent(state: HomeState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    val layout = HomeLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val spacing = BakaMitaiTheme.spacing
    val onShowClick = remember(onIntent) { { slug: String -> onIntent(HomeIntent.ShowClicked(slug)) } }
    HomeGrid(layout = layout, modifier = modifier) {
        state.summary?.let { summary ->
            item(key = "summary", span = fullLineSpan, contentType = CONTENT_TYPE_SUMMARY) {
                TodaySummaryView(model = summary)
            }
        }
        upcomingSection(state = state, horizontalMargin = layout.margin, onShowClick = onShowClick)
        item(key = "latest-header", span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) {
            SectionHeaderView(
                icon = Icons.Outlined.Movie,
                accentColor = MaterialTheme.colorScheme.secondary,
                title = UiText.StringResource(StringResource.home_section_latest),
                pill = UiText.StringResource(StringResource.home_section_latest_badge),
                modifier = Modifier.padding(top = spacing.md),
            )
        }
        if (state.latest.isEmpty()) {
            item(key = "latest-empty", span = fullLineSpan, contentType = CONTENT_TYPE_EMPTY) {
                InlineEmptyText(text = UiText.StringResource(StringResource.home_latest_empty))
            }
        }
        items(items = state.latest, key = { it.key }, contentType = { CONTENT_TYPE_RELEASE }) { release ->
            ReleaseRowView(model = release, onClick = { onShowClick(release.slug) })
        }
    }
}

/** The grid behind both the content and its skeleton, so the two line up exactly. */
@Composable
private fun HomeGrid(
    layout: HomeLayout,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    val spacing = BakaMitaiTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Fixed(layout.latestColumns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = layout.margin, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.gutter),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

/**
 * First load: the content's layout with skeleton placeholders, so nothing moves when the data arrives. Section
 * titles are real text; everything that depends on the data is a skeleton.
 */
@Composable
private fun HomeSkeleton(modifier: Modifier = Modifier) {
    val layout = HomeLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val spacing = BakaMitaiTheme.spacing
    val upcomingAccent = MaterialTheme.colorScheme.primary
    val latestAccent = MaterialTheme.colorScheme.secondary
    SkeletonView(modifier = modifier) {
        HomeGrid(layout = layout, userScrollEnabled = false) {
            item(span = fullLineSpan, contentType = CONTENT_TYPE_SUMMARY) { TodaySummarySkeletonView() }
            item(span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) {
                SectionHeaderView(
                    icon = Icons.Outlined.Schedule,
                    accentColor = upcomingAccent,
                    title = UiText.StringResource(StringResource.home_section_upcoming),
                    pill = null,
                    modifier = Modifier.padding(top = spacing.md),
                )
            }
            item(span = fullLineSpan, contentType = CONTENT_TYPE_CAROUSEL) {
                LazyRow(
                    modifier = Modifier.breakOutOfHorizontalPadding(layout.margin),
                    contentPadding = PaddingValues(horizontal = layout.margin),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    userScrollEnabled = false,
                ) {
                    items(count = UPCOMING_SKELETON_COUNT) {
                        UpcomingCardSkeletonView(modifier = Modifier.width(UpcomingCardWidth))
                    }
                }
            }
            item(span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) {
                SectionHeaderView(
                    icon = Icons.Outlined.Movie,
                    accentColor = latestAccent,
                    title = UiText.StringResource(StringResource.home_section_latest),
                    pill = UiText.StringResource(StringResource.home_section_latest_badge),
                    modifier = Modifier.padding(top = spacing.md),
                )
            }
            items(count = layout.latestColumns * LATEST_SKELETON_ROWS, contentType = { CONTENT_TYPE_RELEASE }) {
                ReleaseRowSkeletonView()
            }
        }
    }
}

/** The Upcoming header, then the carousel or its empty text. */
private fun LazyGridScope.upcomingSection(state: HomeState, horizontalMargin: Dp, onShowClick: (String) -> Unit) {
    item(key = "upcoming-header", span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) {
        SectionHeaderView(
            icon = Icons.Outlined.Schedule,
            accentColor = MaterialTheme.colorScheme.primary,
            title = UiText.StringResource(StringResource.home_section_upcoming),
            pill = state.upcomingCountLabel,
            modifier = Modifier.padding(top = BakaMitaiTheme.spacing.md),
        )
    }
    if (state.upcoming.isEmpty()) {
        item(key = "upcoming-empty", span = fullLineSpan, contentType = CONTENT_TYPE_EMPTY) {
            InlineEmptyText(text = UiText.StringResource(StringResource.home_upcoming_empty))
        }
    } else {
        item(key = "upcoming-row", span = fullLineSpan, contentType = CONTENT_TYPE_CAROUSEL) {
            UpcomingCarousel(items = state.upcoming, horizontalMargin = horizontalMargin, onShowClick = onShowClick)
        }
    }
}

/**
 * Snaps to the start and scrolls edge to edge: it breaks out of the grid's side padding and puts the same
 * padding back inside the row, so the first card lines up with the rest of the content.
 */
@Composable
private fun UpcomingCarousel(
    items: ImmutableList<UpcomingCardUIModel>,
    horizontalMargin: Dp,
    onShowClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LazyRow(
        modifier = modifier.breakOutOfHorizontalPadding(horizontalMargin),
        state = listState,
        contentPadding = PaddingValues(horizontal = horizontalMargin),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
        flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start),
    ) {
        items(items = items, key = { it.key }, contentType = { CONTENT_TYPE_CAROUSEL }) { card ->
            UpcomingCardView(
                model = card,
                onClick = { onShowClick(card.slug) },
                modifier = Modifier.width(UpcomingCardWidth),
            )
        }
    }
}

private fun Modifier.breakOutOfHorizontalPadding(padding: Dp) = layout { measurable, constraints ->
    val extra = (padding * 2).roundToPx()
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}

@Composable
private fun InlineEmptyText(text: UiText, modifier: Modifier = Modifier) {
    Text(
        text = text.asString(),
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

/** Both sections are empty: the banner, then one message. Scrollable, so pull-to-refresh still works. */
@Composable
private fun HomeEmptyView(summary: TodaySummaryUIModel, modifier: Modifier = Modifier) {
    val margin = HomeLayout(currentWindowAdaptiveInfo().windowSizeClass).margin
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = margin, vertical = BakaMitaiTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xl),
    ) {
        TodaySummaryView(model = summary)
        Text(
            text = UiText.StringResource(StringResource.home_empty).asString(),
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private val previewSummary = TodaySummaryUIModel(
    utcOffsetLabel = UiText.StringResource(StringResource.utc_offset, listOf("-5")),
    upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, 4, listOf(4)),
    releasedCount = UiText.StringPlural(PluralResource.home_summary_available, 3, listOf(3)),
)

private val previewBadge07 = EpisodeBadgeUIModel(
    label = UiText.StringResource(StringResource.home_episode, listOf("07")),
    contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("07")),
    isSpecial = false,
)

private val previewBadge08 = EpisodeBadgeUIModel(
    label = UiText.StringResource(StringResource.home_episode, listOf("08")),
    contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
    isSpecial = false,
)

private val previewUpcoming = persistentListOf(
    UpcomingCardUIModel(
        key = "dandadan@4:00 PM",
        slug = "dandadan",
        title = UiText.StringValue("Dandadan"),
        coverUrl = null,
        timeLabel = UiText.StringValue("4:00 PM"),
        episodeBadge = previewBadge08,
    ),
    UpcomingCardUIModel(
        key = "prince@5:30 PM",
        slug = "prince",
        title = UiText.StringValue("The Prince of Tennis II - U-17 World Cup Final Member Selection Match"),
        coverUrl = null,
        timeLabel = UiText.StringValue("5:30 PM"),
        episodeBadge = null,
    ),
    UpcomingCardUIModel(
        key = "kanata-kara@6:00 PM",
        slug = "kanata-kara",
        title = UiText.StringValue("Kanata kara"),
        coverUrl = null,
        timeLabel = UiText.StringValue("6:00 PM"),
        episodeBadge = previewBadge08,
    ),
    UpcomingCardUIModel(
        key = "psyren@7:00 PM",
        slug = "psyren",
        title = UiText.StringValue("PSYREN"),
        coverUrl = null,
        timeLabel = UiText.StringValue("7:00 PM"),
        episodeBadge = previewBadge07,
    ),
)

private val previewLatest = persistentListOf(
    ReleaseRowUIModel(
        key = "e#07",
        slug = "e",
        title = UiText.StringValue("Sasaki to Pii-chan S2"),
        coverUrl = null,
        timeLabel = UiText.StringValue("7:03 AM"),
        episodeBadge = previewBadge07,
    ),
    ReleaseRowUIModel(
        key = "f#01-24",
        slug = "f",
        title = UiText.StringValue("Vinland Saga S2"),
        coverUrl = null,
        timeLabel = UiText.StringValue("9:03 AM"),
        episodeBadge = EpisodeBadgeUIModel(
            label = UiText.StringResource(StringResource.home_episode_batch, listOf("01-24")),
            contentDescription = UiText.StringResource(StringResource.cd_episode_batch, listOf("01-24")),
            isSpecial = true,
        ),
    ),
    ReleaseRowUIModel(
        key = "g#08",
        slug = "g",
        title = UiText.StringValue("Tensei Shitara Ken Deshita S2"),
        coverUrl = null,
        timeLabel = UiText.StringValue("9:33 AM"),
        episodeBadge = previewBadge08,
    ),
)

private val previewContent = HomeState(
    summary = previewSummary,
    upcomingCountLabel = UiText.StringPlural(PluralResource.home_section_upcoming_count, 4, listOf(4)),
    upcoming = previewUpcoming,
    latest = previewLatest,
)

private class HomeStateProvider : PreviewParameterProvider<HomeState> {
    override val values = sequenceOf(
        HomeState(isLoading = true),
        previewContent,
        previewContent.copy(
            upcoming = persistentListOf(),
            upcomingCountLabel = UiText.StringPlural(PluralResource.home_section_upcoming_count, 0, listOf(0)),
        ),
        HomeState(
            summary = previewSummary,
            upcomingCountLabel = UiText.StringPlural(PluralResource.home_section_upcoming_count, 0, listOf(0)),
        ),
        previewContent.copy(isRefreshing = true),
        HomeState(error = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)),
    )
}

@AdaptivePreviews
@Composable
private fun HomeScreenPreview(@PreviewParameter(HomeStateProvider::class) state: HomeState) {
    BakaMitaiTheme {
        HomeScreen(state = state, onIntent = {})
    }
}
