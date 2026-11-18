package com.zc.bakamitai.feature.home.presentation.component.releaserow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.zc.bakamitai.feature.home.presentation.component.episodebadge.EpisodeBadgeSkeletonView
import com.zc.bakamitai.feature.home.presentation.component.episodebadge.EpisodeBadgeView
import com.zc.bakamitai.feature.home.presentation.component.releaserow.model.ReleaseRowUIModel
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel

private const val TITLE_LINES = 2
private const val TIME_CHIP_CONTAINER_ALPHA = 0.8f
private const val SKELETON_SECOND_LINE_FRACTION = 0.55f
private val ThumbnailWidth = 96.dp
private val TimeChipInset = 6.dp
private val TimeChipHorizontalPadding = 6.dp
private val TimeChipVerticalPadding = 2.dp

/**
 * A release from today: thumbnail with the release time, then the title and the episode badge.
 *
 * @param onClick opens the show, or `null` when the row can't be opened (no ripple, not clickable).
 */
@Composable
fun ReleaseRowView(model: ReleaseRowUIModel, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors) {
            ReleaseRowContent(model)
        }
    } else {
        Card(modifier = modifier.semantics(mergeDescendants = true) {}, shape = shape, colors = colors) {
            ReleaseRowContent(model)
        }
    }
}

@Composable
private fun ReleaseRowContent(model: ReleaseRowUIModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(BakaMitaiTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(ThumbnailWidth)
                .aspectRatio(POSTER_ASPECT_RATIO)
                .clip(MaterialTheme.shapes.medium),
        ) {
            CoverImageView(url = model.coverUrl, modifier = Modifier.matchParentSize())
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(TimeChipInset),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = TIME_CHIP_CONTAINER_ALPHA),
                contentColor = MaterialTheme.colorScheme.secondary,
            ) {
                Text(
                    text = model.timeLabel.asString(),
                    modifier = Modifier.padding(
                        horizontal = TimeChipHorizontalPadding,
                        vertical = TimeChipVerticalPadding,
                    ),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
        ) {
            Text(
                text = model.title.asString(),
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = TITLE_LINES,
                style = MaterialTheme.typography.titleMedium,
            )
            EpisodeBadgeView(model = model.episodeBadge)
        }
    }
}

/**
 * Loading placeholder for [ReleaseRowView]: the same card, padding and thumbnail size, with blocks for the
 * thumbnail, two title lines and the episode badge. Use it inside a `SkeletonView`.
 */
@Composable
fun ReleaseRowSkeletonView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(BakaMitaiTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(
                modifier = Modifier
                    .width(ThumbnailWidth)
                    .aspectRatio(POSTER_ASPECT_RATIO),
                shape = MaterialTheme.shapes.medium,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            ) {
                Column {
                    SkeletonTextLine(style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                    SkeletonTextLine(
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(SKELETON_SECOND_LINE_FRACTION),
                    )
                }
                EpisodeBadgeSkeletonView()
            }
        }
    }
}

private class ReleaseRowUIModelProvider : PreviewParameterProvider<ReleaseRowUIModel> {
    override val values = sequenceOf(
        ReleaseRowUIModel(
            key = "tensei#08",
            slug = "tensei",
            title = UiText.StringValue("Tensei Shitara Ken Deshita S2"),
            coverUrl = null,
            timeLabel = UiText.StringValue("9:33 AM"),
            episodeBadge = EpisodeBadgeUIModel(
                label = UiText.StringResource(StringResource.home_episode, listOf("08")),
                contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
                isSpecial = false,
            ),
        ),
        ReleaseRowUIModel(
            key = "vinland-saga-s2#01-24",
            slug = "vinland-saga-s2",
            title = UiText.StringValue("Vinland Saga S2"),
            coverUrl = null,
            timeLabel = UiText.StringValue("2:03 PM"),
            episodeBadge = EpisodeBadgeUIModel(
                label = UiText.StringResource(StringResource.home_episode_batch, listOf("01-24")),
                contentDescription = UiText.StringResource(StringResource.cd_episode_batch, listOf("01-24")),
                isSpecial = true,
            ),
        ),
        ReleaseRowUIModel(
            key = "your-letter#Movie",
            slug = "your-letter",
            title = UiText.StringValue("Your Letter"),
            coverUrl = null,
            timeLabel = UiText.StringValue("12:02 PM"),
            episodeBadge = EpisodeBadgeUIModel(
                label = UiText.StringResource(StringResource.home_episode_movie),
                contentDescription = UiText.StringResource(StringResource.cd_episode_movie),
                isSpecial = true,
            ),
        ),
    )
}

@AdaptivePreviews
@Composable
private fun ReleaseRowViewPreview(@PreviewParameter(ReleaseRowUIModelProvider::class) model: ReleaseRowUIModel) {
    BakaMitaiTheme {
        Surface {
            // Surface passes its minimum size on: the Column lets the row wrap its content.
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                ReleaseRowView(model = model, onClick = {})
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun ReleaseRowSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                ReleaseRowSkeletonView()
            }
        }
    }
}
