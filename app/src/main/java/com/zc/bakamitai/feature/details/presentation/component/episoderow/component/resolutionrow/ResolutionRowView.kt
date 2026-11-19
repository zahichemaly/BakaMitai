package com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionTone
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionUIModel
import com.zc.bakamitai.feature.details.presentation.model.DownloadSource

private const val DISABLED_ALPHA = 0.38f
private val SourceIconSize = 18.dp
private val PillHorizontalPadding = 10.dp
private val PillVerticalPadding = 2.dp

/**
 * One resolution of an episode: its pill ("1080p FHD"), then the Magnet, Torrent and XDCC buttons, which wrap at
 * large font scales. A missing link disables its button; a resolution that isn't out has a greyed pill, "Not
 * released" and three disabled buttons, as on the site.
 *
 * @param episodeTitle read by TalkBack with each button, so an episode's nine buttons can be told apart.
 * @param onSourceClick called with the source and its link.
 */
@Composable
fun ResolutionRowView(
    model: ResolutionUIModel,
    episodeTitle: UiText,
    onSourceClick: (DownloadSource, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = BakaMitaiTheme.spacing
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ResolutionPill(label = model.label, tone = model.tone, isAvailable = model.isAvailable)
            if (!model.isAvailable) {
                Text(
                    text = UiText.StringResource(StringResource.details_resolution_unavailable).asString(),
                    modifier = Modifier.padding(start = spacing.sm),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            listOf(
                DownloadSource.MAGNET to model.magnetUri,
                DownloadSource.TORRENT to model.torrentUrl,
                DownloadSource.XDCC to model.xdccUrl,
            ).forEach { (source, url) ->
                SourceButton(
                    style = sourceStyle(source),
                    url = url,
                    description = UiText.StringResource(
                        StringResource.cd_details_source,
                        listOf(sourceLabel(source), model.label, episodeTitle),
                    ),
                    onClick = { link -> onSourceClick(source, link) },
                )
            }
        }
    }
}

@Composable
private fun ResolutionPill(label: UiText, tone: ResolutionTone, isAvailable: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when {
        !isAvailable -> colors.surfaceContainerHighest to colors.onSurface.copy(alpha = DISABLED_ALPHA)
        tone == ResolutionTone.FHD -> colors.primaryContainer to colors.onPrimaryContainer
        tone == ResolutionTone.HD -> colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.surfaceContainerHighest to colors.onSurface
    }
    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = label.asString(),
            modifier = Modifier.padding(horizontal = PillHorizontalPadding, vertical = PillVerticalPadding),
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

/** How a source's button looks. */
@Immutable
private data class SourceStyle(val label: UiText, val icon: ImageVector, val container: Color, val content: Color)

@Composable
private fun sourceStyle(source: DownloadSource): SourceStyle {
    val colors = MaterialTheme.colorScheme
    return when (source) {
        DownloadSource.MAGNET ->
            SourceStyle(sourceLabel(source), Icons.Outlined.Bolt, colors.tertiaryContainer, colors.onTertiaryContainer)

        DownloadSource.TORRENT -> SourceStyle(
            sourceLabel(source),
            Icons.Outlined.FileDownload,
            colors.secondaryContainer,
            colors.onSecondaryContainer,
        )

        DownloadSource.XDCC ->
            SourceStyle(sourceLabel(source), Icons.Outlined.Terminal, colors.surfaceContainerHighest, colors.primary)
    }
}

private fun sourceLabel(source: DownloadSource): UiText = UiText.StringResource(
    when (source) {
        DownloadSource.MAGNET -> StringResource.details_source_magnet
        DownloadSource.TORRENT -> StringResource.details_source_torrent
        DownloadSource.XDCC -> StringResource.details_source_xdcc
    },
)

/** A compact filled button, enabled when the source has a [url]. */
@Composable
private fun SourceButton(
    style: SourceStyle,
    url: String?,
    description: UiText,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = BakaMitaiTheme.spacing
    val descriptionText = description.asString()
    Button(
        onClick = { url?.let(onClick) },
        modifier = modifier.semantics { contentDescription = descriptionText },
        enabled = url != null,
        colors = ButtonDefaults.buttonColors(containerColor = style.container, contentColor = style.content),
        contentPadding = PaddingValues(horizontal = spacing.sm + spacing.xs, vertical = spacing.xs),
    ) {
        Icon(imageVector = style.icon, contentDescription = null, modifier = Modifier.size(SourceIconSize))
        Spacer(modifier = Modifier.width(spacing.xs))
        Text(text = style.label.asString(), maxLines = 1, style = MaterialTheme.typography.labelMedium)
    }
}

private fun previewResolution(label: String, tone: ResolutionTone, isAvailable: Boolean) = ResolutionUIModel(
    label = UiText.StringValue(label),
    tone = tone,
    magnetUri = "magnet:?xt=urn:btih:x".takeIf { isAvailable },
    torrentUrl = "https://nyaa.si/view/1/torrent".takeIf { isAvailable },
    xdccUrl = "https://subsplease.org/xdcc/?search=x".takeIf { isAvailable },
)

@AdaptivePreviews
@Composable
private fun ResolutionRowViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.lg),
            ) {
                listOf(
                    previewResolution("1080p FHD", ResolutionTone.FHD, isAvailable = true),
                    previewResolution("720p HD", ResolutionTone.HD, isAvailable = true),
                    previewResolution("SD", ResolutionTone.SD, isAvailable = false),
                ).forEach { resolution ->
                    ResolutionRowView(
                        model = resolution,
                        episodeTitle = UiText.StringValue("Episode 20"),
                        onSourceClick = { _, _ -> },
                    )
                }
            }
        }
    }
}
