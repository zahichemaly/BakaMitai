package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.skeleton.textLineHeight
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.sortChipUIModels
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toAlertsChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toBookmarkCountLabel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.AlertsChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.BookmarksHeaderUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.SortChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import kotlinx.collections.immutable.ImmutableList

private const val SKELETON_TITLE_FRACTION = 0.4f
private val SortIconSize = 18.dp
private val CountDotSize = 6.dp
private val AlertsIconSize = 14.dp
private val SkeletonAlertsWidth = 96.dp
private val SkeletonSortWidths = listOf(148.dp, 124.dp, 128.dp)

/**
 * The top of the Bookmarks grid: the sort chips (they scroll sideways on narrow screens), then "SAVED SHOWS · 8
 * shows" (a TalkBack heading) and the alerts chip, when the alerts were checked.
 */
@Composable
fun BookmarksHeaderView(
    model: BookmarksHeaderUIModel,
    selectedSort: BookmarkSortOption,
    onSortClick: (BookmarkSortOption) -> Unit,
    onAlertsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SortStrip(
            chips = model.sortChips,
            selectedSort = selectedSort,
            onSortClick = onSortClick,
            modifier = Modifier.padding(top = BakaMitaiTheme.spacing.xs, bottom = BakaMitaiTheme.spacing.sm),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BakaMitaiTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountTitle(countLabel = model.countLabel, modifier = Modifier.weight(1f, fill = false))
            model.alertsChip?.let { AlertsChip(model = it, onClick = onAlertsClick) }
        }
    }
}

@Composable
private fun SortStrip(
    chips: ImmutableList<SortChipUIModel>,
    selectedSort: BookmarkSortOption,
    onSortClick: (BookmarkSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = colors.surfaceContainerHigh,
        labelColor = colors.onSurfaceVariant,
        iconColor = colors.onSurfaceVariant,
        selectedContainerColor = colors.primary,
        selectedLabelColor = colors.onPrimary,
        selectedLeadingIconColor = colors.onPrimary,
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip.option == selectedSort,
                onClick = { onSortClick(chip.option) },
                label = { Text(text = chip.label.asString(), style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(imageVector = chip.icon, contentDescription = null, modifier = Modifier.size(SortIconSize))
                },
                shape = CircleShape,
                colors = chipColors,
                border = null,
            )
        }
    }
}

/** "SAVED SHOWS · 8 shows", read as one heading. */
@Composable
private fun CountTitle(countLabel: UiText?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = UiText.StringResource(StringResource.bookmarks_header_title).asString(),
            color = colors.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
        )
        if (countLabel != null) {
            Box(modifier = Modifier.size(CountDotSize).background(colors.secondary, CircleShape))
            Text(
                text = countLabel.asString(),
                color = colors.secondary,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/** "ALERTS ON" / "ALERTS OFF": a button TalkBack reads by its description, not its short label. */
@Composable
private fun AlertsChip(model: AlertsChipUIModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tint = if (model.isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val description = model.contentDescription.asString()
    Surface(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = Color.Transparent,
        contentColor = tint,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = BakaMitaiTheme.spacing.sm)
                .clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = model.icon, contentDescription = null, modifier = Modifier.size(AlertsIconSize))
            Text(text = model.label.asString(), maxLines = 1, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Loading placeholder for [BookmarksHeaderView]: three chip pills, the title line and the alerts chip. Use it inside
 * a `SkeletonView`.
 */
@Composable
fun BookmarksHeaderSkeletonView(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(top = BakaMitaiTheme.spacing.xs, bottom = BakaMitaiTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
        ) {
            SkeletonSortWidths.forEach { width ->
                // Each chip keeps the 48 dp touch target around its 32 dp pill.
                Box(modifier = Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
                    SkeletonBlock(
                        modifier = Modifier.size(width = width, height = FilterChipDefaults.Height),
                        shape = CircleShape,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BakaMitaiTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonTextLine(style = typography.labelLarge, modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION))
            Box(modifier = Modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
                SkeletonBlock(
                    modifier = Modifier.size(
                        width = SkeletonAlertsWidth,
                        height = textLineHeight(typography.labelSmall),
                    ),
                    shape = CircleShape,
                )
            }
        }
    }
}

/** Alerts on, off, and not checked yet. */
private class BookmarksHeaderPreviewProvider : PreviewParameterProvider<BookmarksHeaderUIModel> {
    override val values = sequenceOf(true, false, null).map { alertsEnabled ->
        BookmarksHeaderUIModel(
            sortChips = sortChipUIModels(),
            countLabel = 8.toBookmarkCountLabel(),
            alertsChip = alertsEnabled?.toAlertsChipUIModel(),
        )
    }
}

@AdaptivePreviews
@Composable
private fun BookmarksHeaderViewPreview(
    @PreviewParameter(BookmarksHeaderPreviewProvider::class) model: BookmarksHeaderUIModel,
) {
    BakaMitaiTheme {
        Surface {
            BookmarksHeaderView(
                model = model,
                selectedSort = BookmarkSortOption.RECENTLY_ADDED,
                onSortClick = {},
                onAlertsClick = {},
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun BookmarksHeaderSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                BookmarksHeaderSkeletonView()
            }
        }
    }
}
