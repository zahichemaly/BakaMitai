package com.zc.bakamitai.feature.details.presentation.component.downloadsheader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private const val PREVIEW_COUNT = 20
private val SectionIconSize = 20.dp

/**
 * "Episodes & Downloads", with the episode count ("20 EPISODES") at the end once the episodes are loaded. Static
 * chrome: the loading skeleton shows it for real, without the count.
 */
@Composable
fun DownloadsHeaderView(countLabel: UiText?, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.DownloadForOffline,
            contentDescription = null,
            modifier = Modifier.size(SectionIconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(BakaMitaiTheme.spacing.sm))
        Text(
            text = UiText.StringResource(StringResource.details_section_downloads).asString(),
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
        countLabel?.let { label ->
            Text(
                text = label.asString(),
                modifier = Modifier.padding(start = BakaMitaiTheme.spacing.sm),
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun DownloadsHeaderViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.lg),
            ) {
                DownloadsHeaderView(
                    countLabel = UiText.StringPlural(
                        PluralResource.details_episode_count,
                        PREVIEW_COUNT,
                        listOf(PREVIEW_COUNT),
                    ),
                )
                DownloadsHeaderView(countLabel = null)
            }
        }
    }
}
