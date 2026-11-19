package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.coverimage.CoverImageView
import com.zc.bakamitai.common.ui.component.coverimage.POSTER_ASPECT_RATIO
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.timechip.TimeChipView
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.model.BookmarkCardUIModel

private const val TITLE_MAX_LINES = 2
private const val SCRIM_MIDDLE_STOP = 0.5f
private const val SCRIM_MIDDLE_ALPHA = 0.2f
private const val REMOVE_CONTAINER_ALPHA = 0.75f
private val RemoveButtonSize = 32.dp
private val RemoveTouchTargetSize = 48.dp
private val RemoveIconSize = 18.dp
private val AddedIconSize = 12.dp
private val TextGap = 2.dp
private val PreviewCardWidth = 180.dp

/**
 * A bookmarked show: its poster with the next airing (top-start) and the remove button (top-end), then the date
 * added and the title over the poster's scrim. TalkBack reads the card as one item ("TODAY 9:30 PM, Added …,
 * Frieren"), labels its click "Open details", and offers the removal as a card action too.
 */
@Composable
fun BookmarkCardView(
    model: BookmarkCardUIModel,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openLabel = UiText.StringResource(StringResource.cd_bookmarks_open).asString()
    val removeLabel = model.removeLabel.asString()
    Card(
        onClick = onClick,
        modifier = modifier.semantics {
            onClick(label = openLabel, action = null)
            customActions = listOf(
                CustomAccessibilityAction(removeLabel) {
                    onRemoveClick()
                    true
                },
            )
        },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(POSTER_ASPECT_RATIO),
        ) {
            CoverImageView(url = model.coverUrl, modifier = Modifier.matchParentSize())
            PosterScrim(modifier = Modifier.matchParentSize())
            // The end padding keeps a long chip (large font, narrow card) clear of the remove button's touch target.
            TimeChipView(
                model = model.airing,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = BakaMitaiTheme.spacing.sm,
                        top = BakaMitaiTheme.spacing.sm,
                        end = RemoveTouchTargetSize,
                    ),
            )
            // The 48 dp touch target puts the 32 dp circle 8 dp from the corner, like the time chip.
            RemoveButton(
                contentDescription = removeLabel,
                onClick = onRemoveClick,
                modifier = Modifier.align(Alignment.TopEnd),
            )
            CardTexts(
                model = model,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(BakaMitaiTheme.spacing.sm),
            )
        }
    }
}

/** The gradient that keeps the texts readable over the poster. */
@Composable
private fun PosterScrim(modifier: Modifier = Modifier) {
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                0f to Color.Transparent,
                SCRIM_MIDDLE_STOP to container.copy(alpha = SCRIM_MIDDLE_ALPHA),
                1f to container,
            ),
        ),
    )
}

@Composable
private fun RemoveButton(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(RemoveButtonSize)
                .background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = REMOVE_CONTAINER_ALPHA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = contentDescription,
                modifier = Modifier.size(RemoveIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CardTexts(model: BookmarkCardUIModel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TextGap)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Event,
                contentDescription = null,
                modifier = Modifier.size(AddedIconSize),
                tint = colors.onSurfaceVariant,
            )
            Text(
                text = model.addedLabel.asString(),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = model.title.asString(),
            color = colors.onSurface,
            maxLines = TITLE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

/**
 * Loading placeholder for [BookmarkCardView]: the same card with a poster-sized block (the real card has nothing
 * below its poster). Use it inside a `SkeletonView`.
 */
@Composable
fun BookmarkCardSkeletonView(modifier: Modifier = Modifier) {
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
    }
}

private fun previewCard(title: String, airing: TimeChipUIModel, coverUrl: String? = null) = BookmarkCardUIModel(
    slug = title,
    title = UiText.StringValue(title),
    coverUrl = coverUrl,
    addedLabel = UiText.StringValue("Added Nov 18, 2026"),
    airing = airing,
    removeLabel = UiText.StringValue("Remove $title from bookmarks"),
)

private class BookmarkCardPreviewProvider : PreviewParameterProvider<BookmarkCardUIModel> {
    override val values = sequenceOf(
        previewCard("Sousou no Frieren", TimeChipUIModel(UiText.StringValue("TODAY 9:30 PM"), TimeChipTone.UPCOMING)),
        previewCard("Dandadan", TimeChipUIModel(UiText.StringValue("MON 9:30 PM"), TimeChipTone.DEFAULT)),
        previewCard("Chainsaw Man", TimeChipUIModel(UiText.StringValue("NOT AIRING"), TimeChipTone.MUTED)),
        previewCard(
            "The Prince of Tennis II - U-17 World Cup Final Member Selection Match",
            TimeChipUIModel(UiText.StringValue("SAT 11:00 AM"), TimeChipTone.DEFAULT),
            coverUrl = "https://subsplease.org/wp-content/uploads/2026/07/158138.jpg",
        ),
    )
}

@AdaptivePreviews
@Composable
private fun BookmarkCardViewPreview(@PreviewParameter(BookmarkCardPreviewProvider::class) model: BookmarkCardUIModel) {
    BakaMitaiTheme {
        Surface {
            BookmarkCardView(
                model = model,
                onClick = {},
                onRemoveClick = {},
                modifier = Modifier
                    .padding(BakaMitaiTheme.spacing.md)
                    .width(PreviewCardWidth),
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun BookmarkCardSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                BookmarkCardSkeletonView(modifier = Modifier.width(PreviewCardWidth))
            }
        }
    }
}
