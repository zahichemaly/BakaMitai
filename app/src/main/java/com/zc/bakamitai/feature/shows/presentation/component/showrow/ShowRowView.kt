package com.zc.bakamitai.feature.shows.presentation.component.showrow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowPosition
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowUIModel

private const val TITLE_MAX_LINES = 2
private const val SKELETON_TITLE_FRACTION = 0.6f
private val RowVerticalPadding = 14.dp
private val ChevronSize = 20.dp

/**
 * A show's title in its section's card. Each row draws its [position]'s slice of the card (corners, inner padding,
 * the divider above it), so a section's rows can be separate lazy items. The title takes up to 2 lines. With
 * [onClick], the row has a ripple and a chevron; without, it's plain text (until the Details screen exists).
 */
@Composable
fun ShowRowView(
    model: ShowRowUIModel,
    position: ShowRowPosition,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ShowRowCard(position = position, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = BakaMitaiTheme.spacing.md, vertical = RowVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.title.asString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = TITLE_MAX_LINES,
                style = MaterialTheme.typography.titleMedium,
            )
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(ChevronSize),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

/**
 * Loading placeholder for [ShowRowView]: the same card slice and padding, with one title line of
 * [titleWidthFraction] of the width. Use it inside a `SkeletonView`.
 */
@Composable
fun ShowRowSkeletonView(
    position: ShowRowPosition,
    modifier: Modifier = Modifier,
    titleWidthFraction: Float = SKELETON_TITLE_FRACTION,
) {
    ShowRowCard(position = position, modifier = modifier) {
        SkeletonTextLine(
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = BakaMitaiTheme.spacing.md, vertical = RowVerticalPadding)
                .fillMaxWidth(titleWidthFraction),
        )
    }
}

/**
 * The row's slice of its section's card: the card color, the corners and the inner padding of its [position], a
 * divider above every row but the first, and after the last row the gap to the next section (outside the card).
 */
@Composable
private fun ShowRowCard(position: ShowRowPosition, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val spacing = BakaMitaiTheme.spacing
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = if (position.isLast) spacing.lg else 0.dp)
            .background(colors.surfaceContainerLow, position.cardShape(MaterialTheme.shapes.large))
            .padding(
                start = spacing.xs,
                end = spacing.xs,
                top = if (position.isFirst) spacing.xs else 0.dp,
                bottom = if (position.isLast) spacing.xs else 0.dp,
            ),
    ) {
        if (!position.isFirst) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = spacing.md),
                thickness = DividerDefaults.Thickness,
                color = colors.surfaceContainer,
            )
        }
        content()
    }
}

/** The card's corners that this position draws. */
private fun ShowRowPosition.cardShape(card: CornerBasedShape): Shape = when (this) {
    ShowRowPosition.FIRST -> card.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize)
    ShowRowPosition.MIDDLE -> RectangleShape
    ShowRowPosition.LAST -> card.copy(topStart = ZeroCornerSize, topEnd = ZeroCornerSize)
    ShowRowPosition.SINGLE -> card
}

private val previewRows = listOf(
    ShowRowUIModel("chainsaw-man|Chainsaw Man", "chainsaw-man", UiText.StringValue("Chainsaw Man")),
    ShowRowUIModel(
        key = "long|long",
        slug = "long",
        title = UiText.StringValue(
            "Tatoeba Last Dungeon Mae no Mura no Shounen ga Joban no Machi de Kurasu Youna Monogatari",
        ),
    ),
    ShowRowUIModel("clannad|Clannad", "clannad", UiText.StringValue("Clannad")),
)

/** Samples with whether the rows are clickable: a three-row card, then a single-row card. */
private class ShowRowPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@AdaptivePreviews
@Composable
private fun ShowRowViewPreview(@PreviewParameter(ShowRowPreviewProvider::class) clickable: Boolean) {
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                previewRows.forEachIndexed { index, row ->
                    ShowRowView(
                        model = row,
                        position = ShowRowPosition.of(index, previewRows.size),
                        onClick = if (clickable) ({}) else null,
                    )
                }
                ShowRowView(model = previewRows.first(), position = ShowRowPosition.SINGLE, onClick = null)
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun ShowRowSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                Column {
                    ShowRowPosition.entries.filter { it != ShowRowPosition.SINGLE }.forEach { position ->
                        ShowRowSkeletonView(position = position)
                    }
                    ShowRowSkeletonView(position = ShowRowPosition.SINGLE, titleWidthFraction = 1f)
                }
            }
        }
    }
}
