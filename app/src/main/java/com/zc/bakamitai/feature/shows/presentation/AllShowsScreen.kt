package com.zc.bakamitai.feature.shows.presentation

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.offset
import androidx.window.core.layout.WindowSizeClass
import com.zc.bakamitai.common.ui.component.emptystate.EmptyStateView
import com.zc.bakamitai.common.ui.component.error.ErrorView
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.toplevelappbar.TopLevelAppBarView
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.common.ui.util.centeredContentPadding
import com.zc.bakamitai.feature.shows.presentation.component.letterchip.LetterChipSkeletonView
import com.zc.bakamitai.feature.shows.presentation.component.letterchip.LetterChipView
import com.zc.bakamitai.feature.shows.presentation.component.letterheader.LetterHeaderSkeletonView
import com.zc.bakamitai.feature.shows.presentation.component.letterheader.LetterHeaderView
import com.zc.bakamitai.feature.shows.presentation.component.searchfield.ShowSearchFieldView
import com.zc.bakamitai.feature.shows.presentation.component.showrow.ShowRowSkeletonView
import com.zc.bakamitai.feature.shows.presentation.component.showrow.ShowRowView
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowPosition
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowUIModel
import com.zc.bakamitai.feature.shows.presentation.model.IndexLetterUIModel
import com.zc.bakamitai.feature.shows.presentation.model.ShowSectionUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_ROW = "row"
private const val CONTENT_TYPE_FOOTER = "footer"
private const val CONTENT_TYPE_EMPTY = "empty"
private const val CONTENT_TYPE_CHIP = "chip"
private const val SKELETON_CHIP_COUNT = 10
private const val SKELETON_SECTION_COUNT = 2

/** The skeleton rows' title widths: one section's worth of rows, varied like real titles. */
private val SkeletonTitleFractions = listOf(0.7f, 0.45f, 0.85f, 0.6f, 0.5f, 0.75f)

/**
 * All Shows: the A–Z catalogue under a search field and letter chips pinned in the top bar. The chips jump to a
 * section and follow the section at the top of the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllShowsScreen(
    state: AllShowsState,
    onIntent: (AllShowsIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // A new query starts the list (and so the bar tint) at the top; rotation keeps both.
    val listState = key(state.query) { rememberLazyListState() }
    val appBarState = key(state.query) { rememberTopAppBarState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)
    val onQueryChange = remember(onIntent) { { query: String -> onIntent(AllShowsIntent.QueryChanged(query)) } }
    val onClearClick = remember(onIntent) { { onIntent(AllShowsIntent.QueryCleared) } }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopLevelAppBarView(
                subtitle = UiText.StringResource(StringResource.all_shows_app_bar_subtitle),
                onSettingsClick = { onIntent(AllShowsIntent.SettingsClicked) },
                scrollBehavior = scrollBehavior,
                bottomContent = if (state.error == null) {
                    {
                        SearchDeck(
                            query = state.query,
                            sections = state.sections,
                            listState = listState,
                            onQueryChange = onQueryChange,
                            onClearClick = onClearClick,
                        )
                    }
                } else {
                    null
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Default content insets: Scaffold leaves out what the navigation suite consumed. Its bottom bar consumes
        // the bottom system bar, but its rail doesn't, so on medium and wider windows the content pads for it.
    ) { padding ->
        AboveKeyboard(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.hasContent -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onIntent(AllShowsIntent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AllShowsContent(
                        state = state,
                        listState = listState,
                        onIntent = onIntent,
                    )
                }

                state.error != null -> ErrorView(model = state.error, onRetry = { onIntent(AllShowsIntent.Retry) })

                else -> AllShowsSkeleton(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * Lays [content] out above the part of the keyboard that covers this box, so the last rows and the empty view can
 * scroll above it. `imePadding()` would pad by the whole keyboard height, which counts the navigation bar under
 * the content a second time. The keyboard height is read in the layout phase only, so it never recomposes.
 */
@Composable
private fun AboveKeyboard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val ime = WindowInsets.ime
    var spaceBelow by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .onPlaced { coordinates ->
                val rootHeight = coordinates.findRootCoordinates().size.height
                spaceBelow = rootHeight - (coordinates.positionInRoot().y.roundToInt() + coordinates.size.height)
            }
            .layout { measurable, constraints ->
                val overlap = (ime.getBottom(this) - spaceBelow).coerceIn(0, constraints.maxHeight)
                val placeable = measurable.measure(constraints.offset(vertical = -overlap))
                layout(placeable.width, placeable.height + overlap) { placeable.place(0, 0) }
            },
        propagateMinConstraints = true,
    ) {
        content()
    }
}

/**
 * Window-size dependent layout values, read in the composable (never stored in state).
 *
 * @property margin the content's side margin: wider from medium widths up.
 */
private class AllShowsLayout(windowSizeClass: WindowSizeClass) {
    private val isMedium = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val margin: Dp = if (isMedium) BakaMitaiTheme.spacing.marginTablet else BakaMitaiTheme.spacing.margin

    /** The side padding in a pane [maxWidth] wide: [margin], or more to center `MaxContentWidth` of content. */
    fun horizontalPadding(maxWidth: Dp): Dp = centeredContentPadding(maxWidth = maxWidth, margin = margin)
}

/** The pinned deck under the bar: the search field, then the letter chips while the list has sections. */
@Composable
private fun SearchDeck(
    query: String,
    sections: ImmutableList<ShowSectionUIModel>,
    listState: LazyListState,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layout = AllShowsLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val spacing = BakaMitaiTheme.spacing
    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val horizontalPadding = layout.horizontalPadding(maxWidth)
            ShowSearchFieldView(
                query = query,
                onQueryChange = onQueryChange,
                onClearClick = onClearClick,
                modifier = Modifier.padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = spacing.xs,
                    bottom = spacing.sm,
                ),
            )
        }
        if (sections.isNotEmpty()) {
            LetterChips(sections = sections, listState = listState)
        }
    }
}

/**
 * One chip per listed section. The active chip is the section at the top of the list (read late, in a derived
 * state); the row scrolls to keep it in view. A tap scrolls the list to the section: scroll state only, so it's
 * handled here rather than through an intent.
 */
@Composable
private fun LetterChips(
    sections: ImmutableList<ShowSectionUIModel>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    // The list index of each section's header: each section is its header, then its rows.
    val headerIndices = remember(sections) {
        sections.runningFold(0) { headerIndex, section -> headerIndex + 1 + section.rows.size }.dropLast(1)
    }
    val activeSection by remember(headerIndices, listState) {
        derivedStateOf { headerIndices.indexOfLast { it <= listState.firstVisibleItemIndex }.coerceAtLeast(0) }
    }
    val chipRowState = rememberLazyListState()
    // Also when the chips change (a new query), since the row keeps its scroll position by key.
    LaunchedEffect(sections, activeSection) {
        chipRowState.animateScrollToItem((activeSection - 1).coerceAtLeast(0))
    }
    val scope = rememberCoroutineScope()
    LetterChipRow(listState = chipRowState, modifier = modifier.selectableGroup()) {
        itemsIndexed(
            items = sections,
            key = { _, section -> section.headerKey },
            contentType = { _, _ -> CONTENT_TYPE_CHIP },
        ) { index, section ->
            LetterChipView(
                letter = section.letter,
                selected = index == activeSection,
                onClick = { scope.launch { listState.animateScrollToItem(headerIndices[index]) } },
            )
        }
    }
}

/** The row behind both the chips and their skeleton, so the two line up exactly. */
@Composable
private fun LetterChipRow(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    val layout = AllShowsLayout(currentWindowAdaptiveInfo().windowSizeClass)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = layout.horizontalPadding(maxWidth)),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            userScrollEnabled = userScrollEnabled,
            content = content,
        )
    }
}

/**
 * The catalogue: per section a sticky header and its rows, then the footer; or "No anime found" when nothing
 * matches. Dragging the list hides the keyboard.
 */
@Composable
private fun AllShowsContent(
    state: AllShowsState,
    listState: LazyListState,
    onIntent: (AllShowsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(listState, focusManager) {
        listState.interactionSource.interactions
            .filterIsInstance<DragInteraction.Start>()
            .collect { focusManager.clearFocus() }
    }
    val onShowClick = remember(onIntent) { { slug: String -> onIntent(AllShowsIntent.ShowClicked(slug)) } }
    ShowsList(listState = listState, modifier = modifier) {
        if (state.showNoResults) {
            item(key = CONTENT_TYPE_EMPTY, contentType = CONTENT_TYPE_EMPTY) {
                EmptyStateView(
                    icon = Icons.Outlined.SearchOff,
                    title = UiText.StringResource(StringResource.all_shows_no_results_title),
                    message = UiText.StringResource(StringResource.all_shows_no_results_message),
                )
            }
        } else {
            state.sections.forEach { section ->
                stickyHeader(key = section.headerKey, contentType = CONTENT_TYPE_HEADER) {
                    LetterHeaderView(letter = section.letter, countLabel = section.countLabel)
                }
                itemsIndexed(
                    items = section.rows,
                    key = { _, row -> row.key },
                    contentType = { _, _ -> CONTENT_TYPE_ROW },
                ) { index, row ->
                    ShowRowView(
                        model = row,
                        position = ShowRowPosition.of(index, section.rows.size),
                        onClick = { onShowClick(row.slug) },
                    )
                }
            }
            state.footerLabel?.let { label ->
                item(key = CONTENT_TYPE_FOOTER, contentType = CONTENT_TYPE_FOOTER) { CatalogFooter(label = label) }
            }
        }
    }
}

/** The list behind both the content and its skeleton, so the two line up exactly. */
@Composable
private fun ShowsList(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    val layout = AllShowsLayout(currentWindowAdaptiveInfo().windowSizeClass)
    val spacing = BakaMitaiTheme.spacing
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val horizontalPadding = layout.horizontalPadding(maxWidth)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = spacing.xs,
                bottom = spacing.xl,
            ),
            userScrollEnabled = userScrollEnabled,
            content = content,
        )
    }
}

/**
 * First load: the chips where the real ones will appear (under the real search field), then two sections of
 * headers and rows in the list's layout, so nothing moves when the data arrives.
 */
@Composable
private fun AllShowsSkeleton(modifier: Modifier = Modifier) {
    SkeletonView(modifier = modifier) {
        Column {
            LetterChipRow(listState = rememberLazyListState(), userScrollEnabled = false) {
                items(count = SKELETON_CHIP_COUNT, contentType = { CONTENT_TYPE_CHIP }) { LetterChipSkeletonView() }
            }
            ShowsList(listState = rememberLazyListState(), userScrollEnabled = false) {
                repeat(SKELETON_SECTION_COUNT) {
                    item(contentType = CONTENT_TYPE_HEADER) { LetterHeaderSkeletonView() }
                    itemsIndexed(
                        items = SkeletonTitleFractions,
                        contentType = { _, _ -> CONTENT_TYPE_ROW },
                    ) { index, fraction ->
                        ShowRowSkeletonView(
                            position = ShowRowPosition.of(index, SkeletonTitleFractions.size),
                            titleWidthFraction = fraction,
                        )
                    }
                }
            }
        }
    }
}

/** "Showing 1263 shows": the rows listed. */
@Composable
private fun CatalogFooter(label: UiText, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = label.asString(),
            modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@AdaptivePreviews
@Composable
private fun AllShowsScreenPreview(@PreviewParameter(AllShowsStatePreviewProvider::class) state: AllShowsState) {
    BakaMitaiTheme {
        AllShowsScreen(state = state, onIntent = {})
    }
}

/** Loading; content ("#", A with a 2-line title, B with one row); searching "conan"; no results; refreshing; error. */
private class AllShowsStatePreviewProvider : PreviewParameterProvider<AllShowsState> {

    private fun row(title: String) = ShowRowUIModel(key = title, slug = title, title = UiText.StringValue(title))

    private fun section(letter: Char, isPrimaryAccent: Boolean, vararg titles: String): ShowSectionUIModel {
        val label = UiText.StringValue(letter.toString())
        val description = if (letter == '#') {
            UiText.StringResource(StringResource.cd_all_shows_other_section)
        } else {
            label
        }
        return ShowSectionUIModel(
            letter = IndexLetterUIModel(letter, label, description, isPrimaryAccent),
            countLabel = UiText.StringPlural(PluralResource.all_shows_section_count, titles.size, listOf(titles.size)),
            rows = titles.map(::row).toImmutableList(),
        )
    }

    private fun footer(count: Int) = UiText.StringPlural(PluralResource.all_shows_footer_count, count, listOf(count))

    private val content = AllShowsState(
        sections = persistentListOf(
            section('#', isPrimaryAccent = false, "100-man no Inochi no Ue ni Ore wa Tatte Iru", "86 - Eighty Six"),
            section(
                'A',
                isPrimaryAccent = true,
                "A3! Season Autumn & Winter",
                "Akame ga Kill!",
                "Ansatsu Kyoushitsu",
                "Ao no Exorcist - Shimane Illuminati-hen",
                "Ore dake Level Up na Ken - Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de Kurasu",
            ),
            section('B', isPrimaryAccent = false, "Blue Lock"),
        ),
        footerLabel = footer(count = 8),
    )

    override val values = sequenceOf(
        AllShowsState(isLoading = true),
        content,
        AllShowsState(
            query = "conan",
            sections = persistentListOf(
                section('D', isPrimaryAccent = false, "Detective Conan", "Detective Conan - Zero's Tea Time"),
                section('M', isPrimaryAccent = true, "Meitantei Conan - Hannin no Hanzawa-san"),
            ),
            footerLabel = footer(count = 3),
        ),
        AllShowsState(query = "zzzz", footerLabel = footer(count = 0)),
        content.copy(isRefreshing = true),
        AllShowsState(error = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)),
    )
}
