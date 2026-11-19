package com.zc.bakamitai.feature.bookmarks.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.BookmarkCardSkeletonView
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.BookmarkCardView
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.model.BookmarkCardUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.BookmarksHeaderSkeletonView
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.BookmarksHeaderView
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.sortChipUIModels
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toAlertsChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toBookmarkCountLabel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.BookmarksHeaderUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import kotlinx.collections.immutable.persistentListOf

private const val COMPACT_COLUMNS = 2
private const val MEDIUM_COLUMNS = 3
private const val EXPANDED_COLUMNS = 4
private const val SKELETON_CARD_ROWS = 3
private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_EMPTY = "empty"
private const val CONTENT_TYPE_CARD = "card"

private val fullLineSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

/**
 * Bookmarks: the sort chips and the counter row, then the saved shows' poster grid, or the empty state. Pull to
 * refresh updates the airing chips.
 *
 * @param showSettingsAction whether the top bar shows the settings action; `false` until Settings exists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    state: BookmarksState,
    onIntent: (BookmarksIntent) -> Unit,
    modifier: Modifier = Modifier,
    showSettingsAction: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopLevelAppBarView(
                subtitle = UiText.StringResource(StringResource.bookmarks_app_bar_subtitle),
                showSettingsAction = showSettingsAction,
                onSettingsClick = { onIntent(BookmarksIntent.SettingsClicked) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Default content insets: the navigation suite's rail isn't consumed, so wide windows pad for it.
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when {
            state.error != null -> ErrorView(
                model = state.error,
                onRetry = { onIntent(BookmarksIntent.Retry) },
                modifier = contentModifier,
            )

            state.isLoading -> BookmarksSkeleton(modifier = contentModifier)

            else -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(BookmarksIntent.Refresh) },
                modifier = contentModifier,
            ) {
                BookmarksContent(state = state, onIntent = onIntent)
            }
        }
    }
}

/**
 * Window-size dependent layout values, read in the composable (never stored in state).
 *
 * @property margin the content's side margin: wider from medium widths up.
 * @property columns poster columns: 2 / 3 / 4 for compact / medium / expanded.
 */
private class BookmarksLayout(windowSizeClass: WindowSizeClass) {
    private val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    private val isExpanded = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val margin: Dp = if (isMedium) BakaMitaiTheme.spacing.marginTablet else BakaMitaiTheme.spacing.margin
    val columns: Int = when {
        isExpanded -> EXPANDED_COLUMNS
        isMedium -> MEDIUM_COLUMNS
        else -> COMPACT_COLUMNS
    }
}

@Composable
private fun BookmarksContent(
    state: BookmarksState,
    onIntent: (BookmarksIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = BookmarksLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val gridState = rememberLazyGridState()
    // A new sort starts at the top; coming back from Details (same sort) keeps the scroll position.
    var scrolledForSort by rememberSaveable { mutableStateOf(state.selectedSort) }
    LaunchedEffect(state.selectedSort) {
        if (scrolledForSort != state.selectedSort) {
            scrolledForSort = state.selectedSort
            gridState.scrollToItem(0)
        }
    }
    val onCardClick = remember(onIntent) { { slug: String -> onIntent(BookmarksIntent.BookmarkClicked(slug)) } }
    val onRemoveClick = remember(onIntent) { { slug: String -> onIntent(BookmarksIntent.RemoveClicked(slug)) } }
    val onSortClick = remember(onIntent) {
        { option: BookmarkSortOption -> onIntent(BookmarksIntent.SortSelected(option)) }
    }
    val onAlertsClick = remember(onIntent) { { onIntent(BookmarksIntent.AlertsChipClicked) } }
    val onDiscoverClick = remember(onIntent) { { onIntent(BookmarksIntent.DiscoverShowsClicked) } }
    BookmarksGrid(layout = layout, gridState = gridState, modifier = modifier) {
        if (state.isEmpty) {
            item(key = "empty", span = fullLineSpan, contentType = CONTENT_TYPE_EMPTY) {
                BookmarksEmptyView(onDiscoverClick = onDiscoverClick)
            }
        } else {
            item(key = "header", span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) {
                BookmarksHeaderView(
                    model = state.header,
                    selectedSort = state.selectedSort,
                    onSortClick = onSortClick,
                    onAlertsClick = onAlertsClick,
                )
            }
            items(items = state.cards, key = { it.slug }, contentType = { CONTENT_TYPE_CARD }) { card ->
                BookmarkCardView(
                    model = card,
                    onClick = { onCardClick(card.slug) },
                    onRemoveClick = { onRemoveClick(card.slug) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

/** The grid behind both the content and its skeleton, so the two line up exactly. */
@Composable
private fun BookmarksGrid(
    layout: BookmarksLayout,
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
        contentPadding = PaddingValues(start = layout.margin, end = layout.margin, bottom = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(spacing.gutter),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

/** First load: the header and a few rows of cards in the content's grid, so nothing moves when it arrives. */
@Composable
private fun BookmarksSkeleton(modifier: Modifier = Modifier) {
    val layout = BookmarksLayout(currentWindowAdaptiveInfo().windowSizeClass)
    SkeletonView(modifier = modifier) {
        BookmarksGrid(layout = layout, gridState = rememberLazyGridState(), userScrollEnabled = false) {
            item(span = fullLineSpan, contentType = CONTENT_TYPE_HEADER) { BookmarksHeaderSkeletonView() }
            items(count = layout.columns * SKELETON_CARD_ROWS, contentType = { CONTENT_TYPE_CARD }) {
                BookmarkCardSkeletonView()
            }
        }
    }
}

/** No bookmarks: an invitation to pick shows from the weekly schedule (only scheduled shows get alerts). */
@Composable
private fun BookmarksEmptyView(onDiscoverClick: () -> Unit, modifier: Modifier = Modifier) {
    EmptyStateView(
        icon = Icons.Outlined.BookmarkBorder,
        title = UiText.StringResource(StringResource.bookmarks_empty_title),
        message = UiText.StringResource(StringResource.bookmarks_empty_message),
        modifier = modifier,
        action = {
            Button(onClick = onDiscoverClick) {
                Text(text = UiText.StringResource(StringResource.bookmarks_empty_action).asString())
            }
        },
    )
}

@AdaptivePreviews
@Composable
private fun BookmarksScreenPreview(@PreviewParameter(BookmarksStatePreviewProvider::class) state: BookmarksState) {
    BakaMitaiTheme {
        BookmarksScreen(state = state, onIntent = {}, showSettingsAction = false)
    }
}

private fun previewCard(slug: String, title: String, airing: String, tone: TimeChipTone) = BookmarkCardUIModel(
    slug = slug,
    title = UiText.StringValue(title),
    coverUrl = null,
    addedLabel = UiText.StringValue("Added Nov 18, 2026"),
    airing = TimeChipUIModel(UiText.StringValue(airing), tone),
    removeLabel = UiText.StringValue("Remove $title from bookmarks"),
)

private val previewCards = persistentListOf(
    previewCard("frieren", "Sousou no Frieren", "TODAY 9:30 PM", TimeChipTone.UPCOMING),
    previewCard("dandadan", "Dandadan", "THU 6:30 PM", TimeChipTone.DEFAULT),
    previewCard("solo-leveling", "Solo Leveling", "SAT 11:30 AM", TimeChipTone.DEFAULT),
    previewCard("chainsaw-man", "Chainsaw Man", "NOT AIRING", TimeChipTone.MUTED),
    previewCard("kaiju", "Kaijuu 8-gou Season 2: The Defense Force Squad Third", "SUN 11:00 PM", TimeChipTone.DEFAULT),
)

private fun previewHeader(alertsEnabled: Boolean) = BookmarksHeaderUIModel(
    sortChips = sortChipUIModels(),
    countLabel = previewCards.size.toBookmarkCountLabel(),
    alertsChip = alertsEnabled.toAlertsChipUIModel(),
)

/** Loading, content (two sorts, alerts on and off), empty, error. */
private class BookmarksStatePreviewProvider : PreviewParameterProvider<BookmarksState> {
    override val values = sequenceOf(
        BookmarksState(isLoading = true),
        BookmarksState(isLoading = false, header = previewHeader(alertsEnabled = true), cards = previewCards),
        BookmarksState(
            isLoading = false,
            selectedSort = BookmarkSortOption.RELEASE_TIME,
            header = previewHeader(alertsEnabled = false),
            cards = previewCards,
        ),
        BookmarksState(isLoading = false, header = BookmarksHeaderUIModel(sortChips = sortChipUIModels())),
        BookmarksState(
            isLoading = false,
            error = ErrorUIModel(UiText.StringResource(StringResource.error_generic), isRetryable = true),
        ),
    )
}
