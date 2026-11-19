package com.zc.bakamitai.feature.details.presentation.component.detailsheader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.zc.bakamitai.common.ui.component.coverimage.CoverImageView
import com.zc.bakamitai.common.ui.component.coverimage.POSTER_ASPECT_RATIO
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.details.presentation.component.detailsheader.component.bookmarkbutton.BookmarkButtonSkeletonView
import com.zc.bakamitai.feature.details.presentation.component.detailsheader.component.bookmarkbutton.BookmarkButtonView
import com.zc.bakamitai.feature.details.presentation.model.DetailsHeaderUIModel

private const val TITLE_MAX_LINES = 4
private const val SKELETON_SECOND_TITLE_LINE = 0.6f
private val PosterWidth = 112.dp
private val PosterWidthMedium = 128.dp

/**
 * The hero: the poster and the title, then the bookmark button. It sits on the page's own surface, like the other
 * screens: no backdrop, scrim or tint. The content is inset by [contentPadding], the list's side padding.
 *
 * @param contentPadding the side padding of the hero's content.
 */
@Composable
fun DetailsHeaderView(
    model: DetailsHeaderUIModel,
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    contentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    HeaderContent(contentPadding = contentPadding, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md)) {
            CoverImageView(
                url = model.coverUrl,
                modifier = Modifier
                    .width(posterWidth())
                    .aspectRatio(POSTER_ASPECT_RATIO)
                    .clip(MaterialTheme.shapes.medium),
            )
            Text(
                text = model.title.asString(),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = BakaMitaiTheme.spacing.xs)
                    .semantics { heading() },
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = TITLE_MAX_LINES,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        BookmarkButtonView(isBookmarked = isBookmarked, onClick = onBookmarkClick)
    }
}

/**
 * Loading placeholder for [DetailsHeaderView]: the poster's block, two title lines and the button's
 * pill, in the same layout. Use it inside a `SkeletonView`.
 */
@Composable
fun DetailsHeaderSkeletonView(contentPadding: Dp, modifier: Modifier = Modifier) {
    HeaderContent(contentPadding = contentPadding, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md)) {
            SkeletonBlock(
                modifier = Modifier
                    .width(posterWidth())
                    .aspectRatio(POSTER_ASPECT_RATIO),
                shape = MaterialTheme.shapes.medium,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = BakaMitaiTheme.spacing.xs),
            ) {
                SkeletonTextLine(style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())
                SkeletonTextLine(
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(SKELETON_SECOND_TITLE_LINE),
                )
            }
        }
        BookmarkButtonSkeletonView()
    }
}

/** The hero's content column, shared by the header and its skeleton so the two line up. */
@Composable
private fun HeaderContent(contentPadding: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val spacing = BakaMitaiTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = contentPadding, end = contentPadding, top = spacing.lg, bottom = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
        horizontalAlignment = Alignment.Start,
    ) {
        content()
    }
}

/** 112 dp on compact widths, 128 dp from medium widths up. */
@Composable
private fun posterWidth(): Dp {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        PosterWidthMedium
    } else {
        PosterWidth
    }
}

/** Samples: not bookmarked; bookmarked with a 4-line title; no cover. */
private class DetailsHeaderPreviewProvider : PreviewParameterProvider<Pair<DetailsHeaderUIModel, Boolean>> {
    private val header = DetailsHeaderUIModel(
        title = UiText.StringValue("Sousou no Frieren"),
        coverUrl = "https://subsplease.org/wp-content/uploads/2023/09/138006.jpg",
        synopsis = null,
    )

    override val values = sequenceOf(
        header to false,
        header.copy(
            title = UiText.StringValue(
                "Ore dake Level Up na Ken - Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de Kurasu " +
                    "Youna Monogatari Season 2",
            ),
        ) to true,
        header.copy(title = UiText.StringValue("A3! Season Autumn & Winter"), coverUrl = null) to false,
    )
}

@AdaptivePreviews
@Composable
private fun DetailsHeaderViewPreview(
    @PreviewParameter(DetailsHeaderPreviewProvider::class) sample: Pair<DetailsHeaderUIModel, Boolean>,
) {
    BakaMitaiTheme {
        Surface {
            DetailsHeaderView(
                model = sample.first,
                isBookmarked = sample.second,
                onBookmarkClick = {},
                contentPadding = BakaMitaiTheme.spacing.margin,
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun DetailsHeaderSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView {
                DetailsHeaderSkeletonView(contentPadding = BakaMitaiTheme.spacing.margin)
            }
        }
    }
}
