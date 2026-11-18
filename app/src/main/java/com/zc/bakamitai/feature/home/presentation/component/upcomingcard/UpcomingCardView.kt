package com.zc.bakamitai.feature.home.presentation.component.upcomingcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.coverimage.CoverImageView
import com.zc.bakamitai.common.ui.component.coverimage.POSTER_ASPECT_RATIO
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.home.presentation.component.episodebadge.EpisodeBadgeView
import com.zc.bakamitai.feature.home.presentation.component.upcomingcard.model.UpcomingCardUIModel
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel

/** Every Upcoming card is this wide, on every window size: wider windows show more cards. */
val UpcomingCardWidth = 200.dp

private const val TITLE_LINES = 2
private const val SCRIM_MIDDLE_STOP = 0.5f
private const val SCRIM_MIDDLE_ALPHA = 0.4f
private val TimeIconSize = 14.dp
private const val SKELETON_SECOND_LINE_FRACTION = 0.6f

/**
 * A show airing later today: poster with the episode badge and the scheduled time, then the title.
 *
 * @param onClick opens the show, or `null` when the card can't be opened (no ripple, not clickable).
 */
@Composable
fun UpcomingCardView(model: UpcomingCardUIModel, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors) {
            UpcomingCardContent(model)
        }
    } else {
        Card(modifier = modifier.semantics(mergeDescendants = true) {}, shape = shape, colors = colors) {
            UpcomingCardContent(model)
        }
    }
}

@Composable
private fun ColumnScope.UpcomingCardContent(model: UpcomingCardUIModel) {
    val scrimColor = MaterialTheme.colorScheme.surfaceContainerLow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(POSTER_ASPECT_RATIO),
    ) {
        CoverImageView(url = model.coverUrl, modifier = Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        SCRIM_MIDDLE_STOP to scrimColor.copy(alpha = SCRIM_MIDDLE_ALPHA),
                        1f to scrimColor,
                    ),
                ),
        )
        PosterOverlay(
            model = model,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(BakaMitaiTheme.spacing.sm),
        )
    }
    Text(
        text = model.title.asString(),
        modifier = Modifier.padding(BakaMitaiTheme.spacing.sm),
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = TITLE_LINES,
        minLines = TITLE_LINES,
        style = MaterialTheme.typography.titleMedium,
    )
}

/**
 * Loading placeholder for [UpcomingCardView]: the same card with a poster block and two title lines, so it has
 * the real card's height. Give it the same width (`UpcomingCardWidth`). Use it inside a `SkeletonView`.
 */
@Composable
fun UpcomingCardSkeletonView(modifier: Modifier = Modifier) {
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
        Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.sm)) {
            SkeletonTextLine(style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            SkeletonTextLine(
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(SKELETON_SECOND_LINE_FRACTION),
            )
        }
    }
}

@Composable
private fun PosterOverlay(model: UpcomingCardUIModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        model.episodeBadge?.let { badge -> EpisodeBadgeView(model = badge) }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Alarm,
                contentDescription = null,
                modifier = Modifier.size(TimeIconSize),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = model.timeLabel.asString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private val previewBadge = EpisodeBadgeUIModel(
    label = UiText.StringResource(StringResource.home_episode, listOf("08")),
    contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
    isSpecial = false,
)

private class UpcomingCardPreviewProvider : PreviewParameterProvider<Pair<UpcomingCardUIModel, Boolean>> {
    override val values = sequenceOf(
        UpcomingCardUIModel(
            key = "dandadan@18:30",
            slug = "dandadan",
            title = UiText.StringValue("Dandadan"),
            coverUrl = null,
            timeLabel = UiText.StringValue("6:30 PM"),
            episodeBadge = previewBadge,
        ) to true,
        UpcomingCardUIModel(
            key = "prince@11:00",
            slug = "prince",
            title = UiText.StringValue("The Prince of Tennis II - U-17 World Cup Final Member Selection Match"),
            coverUrl = null,
            timeLabel = UiText.StringValue("11:00 AM"),
            episodeBadge = null,
        ) to false,
    )
}

@AdaptivePreviews
@Composable
private fun UpcomingCardViewPreview(
    @PreviewParameter(UpcomingCardPreviewProvider::class) sample: Pair<UpcomingCardUIModel, Boolean>,
) {
    val (model, isClickable) = sample
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                UpcomingCardView(
                    model = model,
                    onClick = if (isClickable) ({}) else null,
                    modifier = Modifier.width(UpcomingCardWidth),
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun UpcomingCardSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                UpcomingCardSkeletonView(modifier = Modifier.width(UpcomingCardWidth))
            }
        }
    }
}
