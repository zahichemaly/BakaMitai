package com.zc.bakamitai.feature.details.presentation.component.episoderow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.ResolutionRowView
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionTone
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionUIModel
import com.zc.bakamitai.feature.details.presentation.component.episoderow.model.EpisodeUIState
import com.zc.bakamitai.feature.details.presentation.model.DownloadSource
import kotlinx.collections.immutable.persistentListOf

private const val TITLE_MAX_LINES = 2
private const val EXPANDED_ROTATION = 180f
private const val SKELETON_TITLE_FRACTION = 0.4f
private const val SKELETON_RELEASE_FRACTION = 0.6f
private val EpisodeHeaderMinHeight = 56.dp
private val ChevronSize = 24.dp

/**
 * An episode or batch card. Its header (title, release line, chevron) toggles the body, which lists the three
 * resolutions and their sources.
 *
 * @param onSourceClick called with the source and its link.
 */
@Composable
fun EpisodeRowView(
    model: EpisodeUIState,
    onToggle: () -> Unit,
    onSourceClick: (DownloadSource, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = BakaMitaiTheme.spacing
    EpisodeCard(modifier = modifier) {
        Column {
            EpisodeHeader(model = model, onToggle = onToggle)
            AnimatedVisibility(
                visible = model.isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.padding(start = spacing.md, end = spacing.md, bottom = spacing.md)) {
                    model.resolutions.forEachIndexed { index, resolution ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = spacing.sm),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            )
                        }
                        ResolutionRowView(model = resolution, episodeTitle = model.title, onSourceClick = onSourceClick)
                    }
                }
            }
        }
    }
}

/**
 * Loading placeholder for [EpisodeRowView]: a collapsed card with the title and release lines and the chevron.
 * Use it inside a `SkeletonView`.
 */
@Composable
fun EpisodeRowSkeletonView(modifier: Modifier = Modifier) {
    EpisodeCard(modifier = modifier) {
        EpisodeHeaderLayout {
            Column(modifier = Modifier.weight(1f)) {
                SkeletonTextLine(
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION),
                )
                SkeletonTextLine(
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(SKELETON_RELEASE_FRACTION),
                )
            }
            SkeletonBlock(modifier = Modifier.size(ChevronSize), shape = CircleShape)
        }
    }
}

@Composable
private fun EpisodeCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        content = content,
    )
}

/** One merged, clickable node: TalkBack reads the title, the release line, the state and "Show downloads". */
@Composable
private fun EpisodeHeader(model: EpisodeUIState, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val rotation by animateFloatAsState(
        targetValue = if (model.isExpanded) EXPANDED_ROTATION else 0f,
        label = "episodeChevronRotation",
    )
    val stateLabel = UiText.StringResource(
        if (model.isExpanded) StringResource.cd_expanded else StringResource.cd_collapsed,
    ).asString()
    val actionLabel = UiText.StringResource(
        if (model.isExpanded) StringResource.cd_details_hide_downloads else StringResource.cd_details_show_downloads,
    ).asString()
    EpisodeHeaderLayout(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClickLabel = actionLabel, role = Role.Button, onClick = onToggle)
            .semantics { stateDescription = stateLabel },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.title.asString(),
                color = colors.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = TITLE_MAX_LINES,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = model.releasedLabel.asString(),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier
                .size(ChevronSize)
                .graphicsLayer { rotationZ = rotation },
            tint = colors.onSurfaceVariant,
        )
    }
}

/** The header's row, shared with the skeleton so the two line up. */
@Composable
private fun EpisodeHeaderLayout(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = EpisodeHeaderMinHeight)
            .padding(horizontal = BakaMitaiTheme.spacing.md, vertical = BakaMitaiTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

private fun previewSlot(label: String, tone: ResolutionTone, isAvailable: Boolean = true) = ResolutionUIModel(
    label = UiText.StringValue(label),
    tone = tone,
    magnetUri = "magnet:?xt=urn:btih:x".takeIf { isAvailable },
    torrentUrl = "https://nyaa.si/view/1/torrent".takeIf { isAvailable },
    xdccUrl = "https://subsplease.org/xdcc/?search=x".takeIf { isAvailable },
)

private fun previewEpisode(key: String, title: String, isExpanded: Boolean) = EpisodeUIState(
    key = key,
    title = UiText.StringValue(title),
    releasedLabel = UiText.StringValue("Nov 17, 2026 · 12:38 PM"),
    resolutions = persistentListOf(
        previewSlot("1080p FHD", ResolutionTone.FHD),
        previewSlot("720p HD", ResolutionTone.HD),
        previewSlot("SD", ResolutionTone.SD, isAvailable = false),
    ),
    isExpanded = isExpanded,
)

/** Samples: collapsed, expanded, a batch, a movie, a long special title. */
private class EpisodeRowPreviewProvider : PreviewParameterProvider<EpisodeUIState> {
    override val values = sequenceOf(
        previewEpisode("episode|19", "Episode 19", isExpanded = false),
        previewEpisode("episode|20", "Episode 20", isExpanded = true),
        previewEpisode("batch|01-28", "Episodes 01–28", isExpanded = false),
        previewEpisode("episode|Movie", "Movie", isExpanded = false),
        previewEpisode(
            "episode|The Counterfeit Case of Ultra 30",
            "The Counterfeit Case of Ultra 30: The Gold-Star Answer and the Detective Who Wasn't There",
            isExpanded = false,
        ),
    )
}

@AdaptivePreviews
@Composable
private fun EpisodeRowViewPreview(@PreviewParameter(EpisodeRowPreviewProvider::class) model: EpisodeUIState) {
    BakaMitaiTheme {
        Surface {
            EpisodeRowView(
                model = model,
                onToggle = {},
                onSourceClick = { _, _ -> },
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun EpisodeRowSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                EpisodeRowSkeletonView()
            }
        }
    }
}
