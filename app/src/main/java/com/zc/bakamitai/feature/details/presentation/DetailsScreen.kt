package com.zc.bakamitai.feature.details.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowSizeClass
import com.zc.bakamitai.common.ui.component.emptystate.EmptyStateView
import com.zc.bakamitai.common.ui.component.error.ErrorView
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.common.ui.util.centeredContentPadding
import com.zc.bakamitai.feature.details.presentation.component.detailsheader.DetailsHeaderSkeletonView
import com.zc.bakamitai.feature.details.presentation.component.detailsheader.DetailsHeaderView
import com.zc.bakamitai.feature.details.presentation.component.downloadsheader.DownloadsHeaderView
import com.zc.bakamitai.feature.details.presentation.component.episodegroupheader.EpisodeGroupHeaderView
import com.zc.bakamitai.feature.details.presentation.component.episoderow.EpisodeRowSkeletonView
import com.zc.bakamitai.feature.details.presentation.component.episoderow.EpisodeRowView
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionTone
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionUIModel
import com.zc.bakamitai.feature.details.presentation.component.episoderow.model.EpisodeUIState
import com.zc.bakamitai.feature.details.presentation.component.synopsiscard.SynopsisCardSkeletonView
import com.zc.bakamitai.feature.details.presentation.component.synopsiscard.SynopsisCardView
import com.zc.bakamitai.feature.details.presentation.model.DetailsHeaderUIModel
import com.zc.bakamitai.feature.details.presentation.model.DownloadSource
import com.zc.bakamitai.feature.details.presentation.model.DownloadsUIState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val CONTENT_TYPE_HERO = "hero"
private const val CONTENT_TYPE_SYNOPSIS = "synopsis"
private const val CONTENT_TYPE_DOWNLOADS_HEADER = "downloads-header"
private const val CONTENT_TYPE_DOWNLOADS_LOADING = "downloads-loading"
private const val CONTENT_TYPE_DOWNLOADS_ERROR = "downloads-error"
private const val CONTENT_TYPE_DOWNLOADS_EMPTY = "downloads-empty"
private const val CONTENT_TYPE_GROUP = "group"
private const val CONTENT_TYPE_EPISODE = "episode"
private const val CONTENT_TYPE_LIMIT_NOTE = "limit-note"
private const val SKELETON_EPISODE_COUNT = 4
private const val PREVIEW_EPISODE_COUNT = 3
private const val PREVIEW_EPISODE_LIMIT = 52

/**
 * Details: one show's hero (cover, title, bookmark), synopsis and downloads, under a bar with back, Share and Open
 * in web. The header shows as soon as the page is parsed, while the downloads section is still a skeleton.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    state: DetailsState,
    onIntent: (DetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DetailsTopAppBar(
                title = state.header?.title,
                showActions = state.header != null,
                onIntent = onIntent,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Default content insets: with the navigation bar hidden they include the system navigation bar; on medium
        // and wider windows, the rail's side is left out (the suite consumed it).
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when {
            state.header != null -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(DetailsIntent.Refresh) },
                modifier = contentModifier,
            ) {
                DetailsContent(state = state, header = state.header, listState = listState, onIntent = onIntent)
            }

            state.error != null -> ErrorView(
                model = state.error,
                onRetry = { onIntent(DetailsIntent.Retry) },
                modifier = contentModifier,
            )

            else -> DetailsSkeleton(modifier = contentModifier)
        }
    }
}

/**
 * Back, the show's title, then Share and Open in web. The title and both actions appear once the page is loaded
 * (the title fades in); until then the bar has only Back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTopAppBar(
    title: UiText?,
    showActions: Boolean,
    onIntent: (DetailsIntent) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            AnimatedVisibility(visible = title != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = title?.asString().orEmpty(),
                    color = colors.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { onIntent(DetailsIntent.BackClicked) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = UiText.StringResource(StringResource.cd_navigate_back).asString(),
                    tint = colors.onSurfaceVariant,
                )
            }
        },
        actions = {
            if (showActions) {
                IconButton(onClick = { onIntent(DetailsIntent.ShareClicked) }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = UiText.StringResource(StringResource.cd_details_share).asString(),
                        tint = colors.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onIntent(DetailsIntent.OpenInWebClicked) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = UiText.StringResource(StringResource.cd_details_open_in_web).asString(),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            scrolledContainerColor = colors.surfaceContainer,
        ),
        scrollBehavior = scrollBehavior,
    )
}

/**
 * The list behind both the content and its skeleton, so the two line up. It has no side padding of its own:
 * [content] gets the side padding that centers at most `MaxContentWidth` of content, and every item pads itself
 * with it.
 */
@Composable
private fun DetailsList(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: LazyListScope.(horizontalPadding: Dp) -> Unit,
) {
    val spacing = BakaMitaiTheme.spacing
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val margin = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        spacing.marginTablet
    } else {
        spacing.margin
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val horizontalPadding = centeredContentPadding(maxWidth = maxWidth, margin = margin)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = spacing.xl),
            userScrollEnabled = userScrollEnabled,
        ) {
            content(horizontalPadding)
        }
    }
}

@Composable
private fun DetailsContent(
    state: DetailsState,
    header: DetailsHeaderUIModel,
    listState: LazyListState,
    onIntent: (DetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = BakaMitaiTheme.spacing
    val onBookmarkClick = remember(onIntent) { { onIntent(DetailsIntent.BookmarkClicked) } }
    val onSynopsisToggle = remember(onIntent) { { onIntent(DetailsIntent.SynopsisToggled) } }
    val onEpisodeToggle = remember(onIntent) { { key: String -> onIntent(DetailsIntent.EpisodeToggled(key)) } }
    val onSourceClick = remember(onIntent) {
        { source: DownloadSource, url: String -> onIntent(DetailsIntent.SourceClicked(source, url)) }
    }
    val onRetryEpisodes = remember(onIntent) { { onIntent(DetailsIntent.RetryEpisodes) } }
    DetailsList(listState = listState, modifier = modifier) { horizontalPadding ->
        item(key = CONTENT_TYPE_HERO, contentType = CONTENT_TYPE_HERO) {
            DetailsHeaderView(
                model = header,
                isBookmarked = state.isBookmarked,
                onBookmarkClick = onBookmarkClick,
                contentPadding = horizontalPadding,
            )
        }
        item(key = CONTENT_TYPE_SYNOPSIS, contentType = CONTENT_TYPE_SYNOPSIS) {
            SynopsisCardView(
                synopsis = header.synopsis,
                isExpanded = state.isSynopsisExpanded,
                onToggle = onSynopsisToggle,
                modifier = Modifier.padding(start = horizontalPadding, end = horizontalPadding, top = spacing.md),
            )
        }
        item(key = CONTENT_TYPE_DOWNLOADS_HEADER, contentType = CONTENT_TYPE_DOWNLOADS_HEADER) {
            DownloadsHeaderView(
                countLabel = (state.downloads as? DownloadsUIState.Content)?.countLabel,
                modifier = Modifier.padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = spacing.lg,
                    bottom = spacing.md,
                ),
            )
        }
        downloadsItems(
            downloads = state.downloads,
            horizontalPadding = horizontalPadding,
            onEpisodeToggle = onEpisodeToggle,
            onSourceClick = onSourceClick,
            onRetry = onRetryEpisodes,
        )
    }
}

/** The downloads section under its header: a skeleton, an error, the empty state, or the groups and rows. */
private fun LazyListScope.downloadsItems(
    downloads: DownloadsUIState,
    horizontalPadding: Dp,
    onEpisodeToggle: (String) -> Unit,
    onSourceClick: (DownloadSource, String) -> Unit,
    onRetry: () -> Unit,
) {
    when (downloads) {
        DownloadsUIState.Loading -> item(
            key = CONTENT_TYPE_DOWNLOADS_LOADING,
            contentType = CONTENT_TYPE_DOWNLOADS_LOADING,
        ) {
            SkeletonView(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm)) {
                    repeat(SKELETON_EPISODE_COUNT) { EpisodeRowSkeletonView() }
                }
            }
        }

        is DownloadsUIState.Error -> item(
            key = CONTENT_TYPE_DOWNLOADS_ERROR,
            contentType = CONTENT_TYPE_DOWNLOADS_ERROR,
        ) {
            ErrorView(model = downloads.error, onRetry = onRetry, modifier = Modifier.fillMaxWidth())
        }

        DownloadsUIState.Empty -> item(
            key = CONTENT_TYPE_DOWNLOADS_EMPTY,
            contentType = CONTENT_TYPE_DOWNLOADS_EMPTY,
        ) {
            EmptyStateView(
                icon = Icons.Outlined.VideoLibrary,
                title = UiText.StringResource(StringResource.details_no_episodes_title),
                message = UiText.StringResource(StringResource.details_no_episodes_message),
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
        }

        is DownloadsUIState.Content -> {
            val hasBatches = downloads.batches.isNotEmpty()
            if (hasBatches) {
                groupHeader(
                    key = "group-batches",
                    label = UiText.StringResource(StringResource.details_group_batches),
                    horizontalPadding = horizontalPadding,
                )
                episodeItems(downloads.batches, horizontalPadding, onEpisodeToggle, onSourceClick)
                if (downloads.episodes.isNotEmpty()) {
                    groupHeader(
                        key = "group-episodes",
                        label = UiText.StringResource(StringResource.details_group_episodes),
                        horizontalPadding = horizontalPadding,
                    )
                }
            }
            episodeItems(downloads.episodes, horizontalPadding, onEpisodeToggle, onSourceClick)
            downloads.limitNote?.let { note ->
                item(key = CONTENT_TYPE_LIMIT_NOTE, contentType = CONTENT_TYPE_LIMIT_NOTE) {
                    EpisodeLimitNote(note = note, modifier = Modifier.padding(horizontal = horizontalPadding))
                }
            }
        }
    }
}

private fun LazyListScope.groupHeader(key: String, label: UiText, horizontalPadding: Dp) {
    item(key = key, contentType = CONTENT_TYPE_GROUP) {
        EpisodeGroupHeaderView(
            label = label,
            modifier = Modifier.padding(horizontal = horizontalPadding),
        )
    }
}

private fun LazyListScope.episodeItems(
    episodes: ImmutableList<EpisodeUIState>,
    horizontalPadding: Dp,
    onEpisodeToggle: (String) -> Unit,
    onSourceClick: (DownloadSource, String) -> Unit,
) {
    items(items = episodes, key = { it.key }, contentType = { CONTENT_TYPE_EPISODE }) { episode ->
        EpisodeRowView(
            model = episode,
            onToggle = { onEpisodeToggle(episode.key) },
            onSourceClick = onSourceClick,
            modifier = Modifier.padding(
                start = horizontalPadding,
                end = horizontalPadding,
                bottom = BakaMitaiTheme.spacing.sm,
            ),
        )
    }
}

/** First load: the hero, synopsis and episode cards in the content's layout, under the real section header. */
@Composable
private fun DetailsSkeleton(modifier: Modifier = Modifier) {
    val spacing = BakaMitaiTheme.spacing
    SkeletonView(modifier = modifier) {
        DetailsList(listState = rememberLazyListState(), userScrollEnabled = false) { horizontalPadding ->
            item(contentType = CONTENT_TYPE_HERO) { DetailsHeaderSkeletonView(contentPadding = horizontalPadding) }
            item(contentType = CONTENT_TYPE_SYNOPSIS) {
                SynopsisCardSkeletonView(
                    modifier = Modifier.padding(start = horizontalPadding, end = horizontalPadding, top = spacing.md),
                )
            }
            item(contentType = CONTENT_TYPE_DOWNLOADS_HEADER) {
                DownloadsHeaderView(
                    countLabel = null,
                    modifier = Modifier.padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = spacing.lg,
                        bottom = spacing.md,
                    ),
                )
            }
            items(count = SKELETON_EPISODE_COUNT, contentType = { CONTENT_TYPE_EPISODE }) {
                EpisodeRowSkeletonView(
                    modifier = Modifier.padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        bottom = spacing.sm,
                    ),
                )
            }
        }
    }
}

/** "SubsPlease lists only the latest 52 releases.", after the last episode. */
@Composable
private fun EpisodeLimitNote(note: UiText, modifier: Modifier = Modifier) {
    Text(
        text = note.asString(),
        modifier = modifier
            .fillMaxWidth()
            .padding(BakaMitaiTheme.spacing.md),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
    )
}

@AdaptivePreviews
@Composable
private fun DetailsScreenPreview(@PreviewParameter(DetailsStatePreviewProvider::class) state: DetailsState) {
    BakaMitaiTheme {
        DetailsScreen(state = state, onIntent = {})
    }
}

/**
 * First load; header with downloads loading; batches and episodes (one expanded, one without SD); episodes with
 * the limit note; downloads error; no episodes; page error; refreshing; a long title without a synopsis.
 */
private class DetailsStatePreviewProvider : PreviewParameterProvider<DetailsState> {

    private val header = DetailsHeaderUIModel(
        title = UiText.StringValue("Sousou no Frieren"),
        coverUrl = null,
        synopsis = UiText.StringValue(
            "The demon king has been defeated, and the victorious hero party returns home before disbanding. The " +
                "four—mage Frieren, hero Himmel, priest Heiter, and warrior Eisen—reminisce about their decade-long " +
                "journey as the moment to bid each other farewell arrives.",
        ),
    )

    private fun slot(label: String, tone: ResolutionTone, isAvailable: Boolean = true) = ResolutionUIModel(
        label = UiText.StringValue(label),
        tone = tone,
        magnetUri = "magnet:?xt=urn:btih:x".takeIf { isAvailable },
        torrentUrl = "https://nyaa.si/view/1/torrent".takeIf { isAvailable },
        xdccUrl = "https://subsplease.org/xdcc/?search=x".takeIf { isAvailable },
    )

    private fun row(key: String, title: String, isExpanded: Boolean = false, hasSd: Boolean = true) = EpisodeUIState(
        key = key,
        title = UiText.StringValue(title),
        releasedLabel = UiText.StringValue("Mar 22, 2024 · 11:32 AM"),
        resolutions = persistentListOf(
            slot("1080p FHD", ResolutionTone.FHD),
            slot("720p HD", ResolutionTone.HD),
            if (hasSd) slot("480p SD", ResolutionTone.SD) else slot("SD", ResolutionTone.SD, isAvailable = false),
        ),
        isExpanded = isExpanded,
    )

    private fun count(count: Int) = UiText.StringPlural(PluralResource.details_episode_count, count, listOf(count))

    private val content = DetailsState(
        header = header,
        isBookmarked = true,
        downloads = DownloadsUIState.Content(
            countLabel = count(PREVIEW_EPISODE_COUNT),
            batches = persistentListOf(row("batch|01-28", "Episodes 01–28")),
            episodes = persistentListOf(
                row("episode|28", "Episode 28", isExpanded = true, hasSd = false),
                row("episode|27", "Episode 27"),
                row("episode|01", "Episode 1"),
            ),
            limitNote = null,
        ),
    )

    private val networkError = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)

    override val values = sequenceOf(
        DetailsState(),
        DetailsState(header = header),
        content,
        DetailsState(
            header = header,
            downloads = DownloadsUIState.Content(
                countLabel = count(PREVIEW_EPISODE_LIMIT),
                batches = persistentListOf(),
                episodes = persistentListOf(
                    row("episode|1180", "Episode 1180", isExpanded = true),
                    row("episode|Egghead SP11", "Egghead SP11"),
                    row("episode|1143v2", "Episode 1143 (v2)"),
                ),
                limitNote = UiText.StringResource(
                    StringResource.details_episode_limit_note,
                    listOf(PREVIEW_EPISODE_LIMIT),
                ),
            ),
        ),
        DetailsState(header = header, downloads = DownloadsUIState.Error(networkError)),
        DetailsState(header = header, downloads = DownloadsUIState.Empty),
        DetailsState(error = ErrorUIModel(UiText.StringResource(StringResource.error_not_found), isRetryable = false)),
        content.copy(isRefreshing = true),
        DetailsState(
            header = header.copy(
                title = UiText.StringValue(
                    "Ore dake Level Up na Ken - Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de " +
                        "Kurasu Youna Monogatari Season 2",
                ),
                synopsis = null,
            ),
            downloads = DownloadsUIState.Content(
                countLabel = count(1),
                batches = persistentListOf(),
                episodes = persistentListOf(row("episode|Movie", "Movie", isExpanded = true)),
                limitNote = null,
            ),
        ),
    )
}
