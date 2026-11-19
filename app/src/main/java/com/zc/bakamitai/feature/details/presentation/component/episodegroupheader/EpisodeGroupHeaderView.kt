package com.zc.bakamitai.feature.details.presentation.component.episodegroupheader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/** A group's label above its rows, "BATCHES" or "EPISODES", shown when the show has batches. */
@Composable
fun EpisodeGroupHeaderView(label: UiText, modifier: Modifier = Modifier) {
    Text(
        text = label.asString(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = BakaMitaiTheme.spacing.sm)
            .semantics { heading() },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
}

@AdaptivePreviews
@Composable
private fun EpisodeGroupHeaderViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                EpisodeGroupHeaderView(label = UiText.StringResource(StringResource.details_group_batches))
                EpisodeGroupHeaderView(label = UiText.StringResource(StringResource.details_group_episodes))
            }
        }
    }
}
