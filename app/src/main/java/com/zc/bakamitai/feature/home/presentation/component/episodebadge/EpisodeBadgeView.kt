package com.zc.bakamitai.feature.home.presentation.component.episodebadge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel

private const val REGULAR_CONTAINER_ALPHA = 0.2f
private const val SPECIAL_CONTAINER_ALPHA = 0.3f
private val BadgeVerticalPadding = 2.dp

/**
 * An episode pill: primary for a regular episode, tertiary for a batch, a movie or anything non-numeric.
 * TalkBack reads its content description ("Episode 08") instead of the label.
 */
@Composable
fun EpisodeBadgeView(model: EpisodeBadgeUIModel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val contentColor = if (model.isSpecial) colors.tertiary else colors.primary
    val containerColor = if (model.isSpecial) {
        colors.tertiaryContainer.copy(alpha = SPECIAL_CONTAINER_ALPHA)
    } else {
        colors.primary.copy(alpha = REGULAR_CONTAINER_ALPHA)
    }
    val description = model.contentDescription.asString()
    Surface(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = model.label.asString(),
            modifier = Modifier.padding(horizontal = BakaMitaiTheme.spacing.sm, vertical = BadgeVerticalPadding),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@AdaptivePreviews
@Composable
private fun EpisodeBadgeViewPreview() {
    BakaMitaiTheme {
        Surface {
            Row(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
            ) {
                EpisodeBadgeView(
                    EpisodeBadgeUIModel(
                        label = UiText.StringResource(StringResource.home_episode, listOf("08")),
                        contentDescription = UiText.StringResource(StringResource.cd_episode, listOf("08")),
                        isSpecial = false,
                    ),
                )
                EpisodeBadgeView(
                    EpisodeBadgeUIModel(
                        label = UiText.StringResource(StringResource.home_episode_batch, listOf("01-24")),
                        contentDescription = UiText.StringResource(StringResource.cd_episode_batch, listOf("01-24")),
                        isSpecial = true,
                    ),
                )
                EpisodeBadgeView(
                    EpisodeBadgeUIModel(
                        label = UiText.StringResource(StringResource.home_episode_movie),
                        contentDescription = UiText.StringResource(StringResource.cd_episode_movie),
                        isSpecial = true,
                    ),
                )
            }
        }
    }
}
