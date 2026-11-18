package com.zc.bakamitai.feature.home.presentation.component.coverimage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/** Width / height of a show poster (DESIGN.md poster cards, the design's `aspect-[3/4.2]`). */
const val POSTER_ASPECT_RATIO = 3f / 4.2f

/**
 * A show's cover art, cropped to fill. The caller gives it a size (width and [POSTER_ASPECT_RATIO]) and a
 * shape. While it loads, or when there is no cover, it shows a neutral tonal fill.
 *
 * @param url absolute cover URL, or `null` for the placeholder.
 */
@Composable
fun CoverImageView(url: String?, modifier: Modifier = Modifier) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
    )
}

private val PreviewWidth = 120.dp

@AdaptivePreviews
@Composable
private fun CoverImageViewPreview() {
    BakaMitaiTheme {
        Surface {
            // Surface passes its minimum size on: the Column lets the cover keep its own size.
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                CoverImageView(
                    url = null,
                    modifier = Modifier
                        .width(PreviewWidth)
                        .aspectRatio(POSTER_ASPECT_RATIO),
                )
            }
        }
    }
}
