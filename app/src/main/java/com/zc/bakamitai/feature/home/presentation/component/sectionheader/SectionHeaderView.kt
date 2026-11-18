package com.zc.bakamitai.feature.home.presentation.component.sectionheader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private val SectionIconSize = 20.dp
private val SectionPillHorizontalPadding = 8.dp
private val SectionPillVerticalPadding = 2.dp
private const val PREVIEW_UPCOMING_COUNT = 4

/**
 * A Home section title: an accent icon, the title (a TalkBack heading) and an optional pill such as a count.
 * It shows the same in the content and in its skeleton.
 */
@Composable
fun SectionHeaderView(
    icon: ImageVector,
    accentColor: Color,
    title: UiText,
    pill: UiText?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(SectionIconSize),
            tint = accentColor,
        )
        Text(
            text = title.asString(),
            modifier = Modifier.semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
        pill?.let {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = accentColor,
            ) {
                Text(
                    text = it.asString(),
                    modifier = Modifier.padding(
                        horizontal = SectionPillHorizontalPadding,
                        vertical = SectionPillVerticalPadding,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun SectionHeaderViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            ) {
                SectionHeaderView(
                    icon = Icons.Outlined.Schedule,
                    accentColor = MaterialTheme.colorScheme.primary,
                    title = UiText.StringResource(StringResource.home_section_upcoming),
                    pill = UiText.StringPlural(
                        PluralResource.home_section_upcoming_count,
                        PREVIEW_UPCOMING_COUNT,
                        listOf(PREVIEW_UPCOMING_COUNT),
                    ),
                )
                SectionHeaderView(
                    icon = Icons.Outlined.Movie,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    title = UiText.StringResource(StringResource.home_section_latest),
                    pill = null,
                )
            }
        }
    }
}
