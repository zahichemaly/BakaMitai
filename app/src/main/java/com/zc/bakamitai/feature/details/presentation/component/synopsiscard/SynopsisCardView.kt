package com.zc.bakamitai.feature.details.presentation.component.synopsiscard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private const val SYNOPSIS_COLLAPSED_LINES = 3
private const val EXPANDED_ROTATION = 180f
private const val SKELETON_TITLE_FRACTION = 0.35f
private const val SKELETON_LAST_LINE_FRACTION = 0.7f
private const val SKELETON_TOGGLE_FRACTION = 0.25f
private const val SKELETON_FULL_LINES = 2
private val SectionIconSize = 20.dp
private val ToggleIconSize = 16.dp

/**
 * The synopsis card: collapsed to 3 lines, with "Read more" / "Read less" when the text is longer. A show without
 * a synopsis reads "No synopsis yet." and has no toggle.
 *
 * @param synopsis paragraphs separated by a blank line, or `null`.
 */
@Composable
fun SynopsisCardView(synopsis: UiText?, isExpanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    SynopsisCard(modifier = modifier) {
        SynopsisTitle()
        Spacer(modifier = Modifier.height(BakaMitaiTheme.spacing.sm))
        if (synopsis == null) {
            Text(
                text = UiText.StringResource(StringResource.details_synopsis_missing).asString(),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            // Whether the collapsed text is cut is only known after layout: purely visual state.
            var hasOverflow by remember(synopsis) { mutableStateOf(false) }
            Text(
                text = synopsis.asString(),
                color = colors.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = if (isExpanded) Int.MAX_VALUE else SYNOPSIS_COLLAPSED_LINES,
                onTextLayout = { result -> if (!isExpanded) hasOverflow = result.hasVisualOverflow },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (hasOverflow || isExpanded) {
                SynopsisToggle(isExpanded = isExpanded, onClick = onToggle)
            }
        }
    }
}

/**
 * Loading placeholder for [SynopsisCardView]: the same card, with the title, three lines of text and the toggle.
 * Use it inside a `SkeletonView`.
 */
@Composable
fun SynopsisCardSkeletonView(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    SynopsisCard(modifier = modifier) {
        SkeletonTextLine(style = typography.headlineSmall, modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION))
        Spacer(modifier = Modifier.height(BakaMitaiTheme.spacing.sm))
        repeat(SKELETON_FULL_LINES) {
            SkeletonTextLine(style = typography.bodyMedium, modifier = Modifier.fillMaxWidth())
        }
        SkeletonTextLine(style = typography.bodyMedium, modifier = Modifier.fillMaxWidth(SKELETON_LAST_LINE_FRACTION))
        SkeletonTextLine(
            style = typography.labelLarge,
            modifier = Modifier
                .padding(vertical = BakaMitaiTheme.spacing.md)
                .fillMaxWidth(SKELETON_TOGGLE_FRACTION),
        )
    }
}

/** The card behind both the synopsis and its skeleton. */
@Composable
private fun SynopsisCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(BakaMitaiTheme.spacing.md),
        ) {
            content()
        }
    }
}

@Composable
private fun SynopsisTitle(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.AutoStories,
            contentDescription = null,
            modifier = Modifier.size(SectionIconSize),
            tint = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(BakaMitaiTheme.spacing.sm))
        Text(
            text = UiText.StringResource(StringResource.details_synopsis_title).asString(),
            modifier = Modifier.semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun SynopsisToggle(isExpanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) EXPANDED_ROTATION else 0f,
        label = "synopsisToggleRotation",
    )
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
    ) {
        val label = UiText.StringResource(
            if (isExpanded) StringResource.details_synopsis_read_less else StringResource.details_synopsis_read_more,
        )
        Text(
            text = label.asString(),
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.width(BakaMitaiTheme.spacing.xs))
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier
                .size(ToggleIconSize)
                .graphicsLayer { rotationZ = rotation },
        )
    }
}

private val previewSynopsis = UiText.StringValue(
    "The demon king has been defeated, and the victorious hero party returns home before disbanding. The " +
        "four—mage Frieren, hero Himmel, priest Heiter, and warrior Eisen—reminisce about their decade-long " +
        "journey as the moment to bid each other farewell arrives.\n\nBefore his death, Heiter manages to foist " +
        "a young human apprentice called Fern onto Frieren.",
)

/** Samples: collapsed, expanded, missing. */
private class SynopsisCardPreviewProvider : PreviewParameterProvider<Pair<UiText?, Boolean>> {
    override val values = sequenceOf(previewSynopsis to false, previewSynopsis to true, null to false)
}

@AdaptivePreviews
@Composable
private fun SynopsisCardViewPreview(
    @PreviewParameter(SynopsisCardPreviewProvider::class) sample: Pair<UiText?, Boolean>,
) {
    BakaMitaiTheme {
        Surface {
            SynopsisCardView(
                synopsis = sample.first,
                isExpanded = sample.second,
                onToggle = {},
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun SynopsisCardSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                SynopsisCardSkeletonView()
            }
        }
    }
}
