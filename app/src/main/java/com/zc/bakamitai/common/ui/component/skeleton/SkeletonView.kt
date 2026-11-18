package com.zc.bakamitai.common.ui.component.skeleton

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private const val SHIMMER_DURATION_MILLIS = 1_100
private const val SHIMMER_PAUSE_MILLIS = 400

/** How far the band leans: vertical drift per unit of horizontal travel. */
private const val SHIMMER_TILT = 0.25f
private val ShimmerBandWidth = 240.dp

/**
 * The loading state of a screen or a section. Lay out [content] like the real content, from [SkeletonBlock]s,
 * [SkeletonTextLine]s and components' `[Name]SkeletonView`s; one highlight band then sweeps across all of it.
 *
 * - The band is drawn only over what [content] draws (`BlendMode.SrcAtop` in an offscreen layer), so it follows
 *   the placeholders' shapes. Don't give [content] a background: put the [SkeletonView] on the screen's surface.
 * - The animation is read in the draw phase only: it never recomposes or re-lays out [content].
 * - TalkBack reads the whole skeleton as one "Loading" item.
 */
@Composable
fun SkeletonView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val highlight = BakaMitaiTheme.extendedColors.shimmerHighlight
    val bandColors = remember(highlight) { listOf(Color.Transparent, highlight, Color.Transparent) }
    val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        // Sweep first, then pause: the first sweep starts at once, since most loads are short.
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = SHIMMER_DURATION_MILLIS + SHIMMER_PAUSE_MILLIS
                0f at 0 using LinearEasing
                1f at SHIMMER_DURATION_MILLIS
            },
        ),
        label = "shimmerProgress",
    )
    val loadingDescription = UiText.StringResource(StringResource.cd_loading).asString()
    Box(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = loadingDescription }
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                // A point (x, y) is inside the band when x + y·tilt is in [start, start + span].
                val band = ShimmerBandWidth.toPx()
                val span = band * (1 + SHIMMER_TILT * SHIMMER_TILT)
                val travel = size.width + size.height * SHIMMER_TILT + span
                val start = -span + travel * progress
                drawRect(
                    brush = Brush.linearGradient(
                        colors = bandColors,
                        start = Offset(start, 0f),
                        end = Offset(start + band, band * SHIMMER_TILT),
                    ),
                    blendMode = BlendMode.SrcAtop,
                )
            },
    ) {
        content()
    }
}

/**
 * A placeholder shape for content that is still loading: give it the size and [shape] of what it stands in for.
 * It shimmers only inside a [SkeletonView].
 */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.extraSmall) {
    Box(modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = shape))
}

/**
 * A placeholder for one line of text in [style]: exactly as tall as the line (so it follows the font scale),
 * with a rounded bar the height of the font. Set its width with [modifier], e.g. `fillMaxWidth(0.6f)`.
 */
@Composable
fun SkeletonTextLine(style: TextStyle, modifier: Modifier = Modifier) {
    val barHeight = with(LocalDensity.current) { fontSizeOf(style).toDp() }
    Box(modifier = modifier.height(textLineHeight(style)), contentAlignment = Alignment.CenterStart) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            shape = CircleShape,
        )
    }
}

/**
 * The height of one line of text in [style] at the current density and font scale. Use it to size skeletons of
 * text-based shapes such as chips and badges: line height plus the real component's vertical padding.
 */
@Composable
@ReadOnlyComposable
fun textLineHeight(style: TextStyle): Dp {
    val fontSize = fontSizeOf(style)
    val lineHeight = when {
        style.lineHeight.isSp -> style.lineHeight
        style.lineHeight.isEm -> (fontSize.value * style.lineHeight.value).sp
        else -> fontSize
    }
    return with(LocalDensity.current) { lineHeight.toDp() }
}

private val FallbackFontSize = 14.sp

private fun fontSizeOf(style: TextStyle) = if (style.fontSize.isSp) style.fontSize else FallbackFontSize

private val PreviewAvatarSize = 56.dp
private const val PREVIEW_SHORT_LINE = 0.5f

@AdaptivePreviews
@Composable
private fun SkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                Column(verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md)) {
                        SkeletonBlock(modifier = Modifier.size(PreviewAvatarSize), shape = CircleShape)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SkeletonTextLine(style = MaterialTheme.typography.titleMedium)
                            SkeletonTextLine(
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(PREVIEW_SHORT_LINE),
                            )
                        }
                    }
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PreviewAvatarSize * 2),
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        }
    }
}
