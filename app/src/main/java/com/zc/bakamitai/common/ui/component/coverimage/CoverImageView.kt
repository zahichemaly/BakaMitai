package com.zc.bakamitai.common.ui.component.coverimage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/** Width / height of a show poster (DESIGN.md poster cards, the design's `aspect-[3/4.2]`). */
const val POSTER_ASPECT_RATIO = 3f / 4.2f

private const val PLACEHOLDER_ICON_FRACTION = 0.3f
private const val PLACEHOLDER_ICON_ALPHA = 0.6f
private val PlaceholderIconMinSize = 20.dp
private val PlaceholderIconMaxSize = 48.dp

/**
 * A show's cover art, cropped to fill. The caller gives it a size (width and [POSTER_ASPECT_RATIO]) and a
 * shape.
 * - While the cover loads, it shows a plain tonal fill.
 * - When there is no cover (`url == null`, e.g. SubsPlease's "coming soon" image) or it fails to load, it shows
 *   a placeholder poster: the same fill with an image icon in the middle, sized to the poster.
 *
 * @param url absolute cover URL, or `null` for the placeholder.
 */
@Composable
fun CoverImageView(url: String?, modifier: Modifier = Modifier) {
    val fillColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PLACEHOLDER_ICON_ALPHA)
    val icon = rememberVectorPainter(Icons.Outlined.Image)
    val density = LocalDensity.current
    val loading = remember(fillColor) { ColorPainter(fillColor) }
    val missing = remember(fillColor, iconColor, icon, density) {
        CoverPlaceholderPainter(
            fill = fillColor,
            icon = icon,
            iconTint = ColorFilter.tint(iconColor),
            minIconSizePx = with(density) { PlaceholderIconMinSize.toPx() },
            maxIconSizePx = with(density) { PlaceholderIconMaxSize.toPx() },
        )
    }
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        placeholder = loading,
        error = missing,
        fallback = missing,
        contentScale = ContentScale.Crop,
    )
}

/**
 * The "no cover" poster: [fill] with [icon] centered at about a third of the shorter side, kept between
 * [minIconSizePx] and [maxIconSizePx]. It has no intrinsic size, so it draws at whatever size the image has
 * instead of being cropped like a bitmap.
 */
private class CoverPlaceholderPainter(
    private val fill: Color,
    private val icon: VectorPainter,
    private val iconTint: ColorFilter,
    private val minIconSizePx: Float,
    private val maxIconSizePx: Float,
) : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(color = fill)
        val iconSide = (size.minDimension * PLACEHOLDER_ICON_FRACTION).coerceIn(minIconSizePx, maxIconSizePx)
        translate(left = (size.width - iconSide) / 2, top = (size.height - iconSide) / 2) {
            with(icon) { draw(size = Size(iconSide, iconSide), colorFilter = iconTint) }
        }
    }
}

private val PreviewSmallWidth = 96.dp
private val PreviewLargeWidth = 180.dp

@AdaptivePreviews
@Composable
private fun CoverImageViewPreview() {
    BakaMitaiTheme {
        Surface {
            // Surface passes its minimum size on: the Row lets each cover keep its own size.
            Row(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            ) {
                CoverImageView(
                    url = null,
                    modifier = Modifier
                        .width(PreviewSmallWidth)
                        .aspectRatio(POSTER_ASPECT_RATIO),
                )
                CoverImageView(
                    url = null,
                    modifier = Modifier
                        .width(PreviewLargeWidth)
                        .aspectRatio(POSTER_ASPECT_RATIO),
                )
            }
        }
    }
}
