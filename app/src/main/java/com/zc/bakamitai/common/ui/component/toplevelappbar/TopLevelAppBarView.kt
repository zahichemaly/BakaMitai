package com.zc.bakamitai.common.ui.component.toplevelappbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/** Content counts as scrolled under the bar past this overlap, as Material 3's `TopAppBar` does. */
private const val SCROLLED_FRACTION = 0.01f

/**
 * The top app bar of a top-level tab: "BakaMitai" with the tab's [subtitle] under it, and the settings action.
 * [bottomContent] (e.g. Schedule's day strip) is pinned under the bar and shares its background, which turns
 * `surfaceContainer` once content scrolls under it.
 *
 * @param showSettingsAction whether the settings action is shown; `false` until Settings exists.
 * @param scrollBehavior the screen's pinned scroll behavior, connected to the scrolling content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopLevelAppBarView(
    subtitle: UiText,
    showSettingsAction: Boolean,
    onSettingsClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    bottomContent: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val isScrolled by remember(scrollBehavior) {
        derivedStateOf { scrollBehavior.state.overlappedFraction > SCROLLED_FRACTION }
    }
    val containerColor = animateColorAsState(
        targetValue = if (isScrolled) colors.surfaceContainer else colors.surface,
        label = "topLevelAppBarContainer",
    )
    // The color is read in the draw phase only, so the animation never recomposes the bar.
    Column(modifier = modifier.drawBehind { drawRect(containerColor.value) }) {
        TopAppBar(
            title = { TopLevelAppBarTitle(subtitle = subtitle) },
            actions = {
                if (showSettingsAction) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = UiText.StringResource(StringResource.cd_settings).asString(),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
            scrollBehavior = scrollBehavior,
        )
        bottomContent?.invoke()
    }
}

@Composable
private fun TopLevelAppBarTitle(subtitle: UiText, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = UiText.StringResource(StringResource.app_name).asString(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = subtitle.asString(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** Samples: with the settings action, without it, and with bottom content. */
private class TopLevelAppBarPreviewProvider : PreviewParameterProvider<Pair<Boolean, Boolean>> {
    override val values = sequenceOf(true to false, false to false, false to true)
}

@OptIn(ExperimentalMaterial3Api::class)
@AdaptivePreviews
@Composable
private fun TopLevelAppBarViewPreview(
    @PreviewParameter(TopLevelAppBarPreviewProvider::class) sample: Pair<Boolean, Boolean>,
) {
    val (showSettingsAction, hasBottomContent) = sample
    BakaMitaiTheme {
        Surface {
            TopLevelAppBarView(
                subtitle = UiText.StringResource(StringResource.nav_home),
                showSettingsAction = showSettingsAction,
                onSettingsClick = {},
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                bottomContent = if (hasBottomContent) {
                    { PreviewBottomContent() }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun PreviewBottomContent(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = BakaMitaiTheme.spacing.margin),
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
    ) {
        FilterChip(
            selected = true,
            onClick = {},
            label = { Text(text = UiText.StringResource(StringResource.nav_home).asString()) },
        )
        FilterChip(
            selected = false,
            onClick = {},
            label = { Text(text = UiText.StringResource(StringResource.nav_schedule).asString()) },
        )
    }
}
