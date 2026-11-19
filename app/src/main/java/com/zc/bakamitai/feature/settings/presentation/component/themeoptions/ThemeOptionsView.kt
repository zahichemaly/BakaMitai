package com.zc.bakamitai.feature.settings.presentation.component.themeoptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.skeleton.textLineHeight
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.settings.presentation.component.themeoptions.mapper.toThemeOptionUIStates
import com.zc.bakamitai.feature.settings.presentation.component.themeoptions.model.ThemeOptionUIState
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import kotlinx.collections.immutable.ImmutableList

private val ThemeOptionMinHeight = 52.dp
private val IconCircleSize = 36.dp
private val IconSize = 20.dp
private val CheckSize = 24.dp
private const val UNSELECTED_ROW_ALPHA = 0.5f
private const val SELECTED_ICON_ALPHA = 0.2f
private const val DESCRIPTION_MAX_LINES = 2
private const val SKELETON_ROW_COUNT = 3

/**
 * The theme choices as a radio list: each row has an icon in a circle, a title and a description; the selected row
 * is tinted, with a primary icon and a check.
 */
@Composable
fun ThemeOptionsView(
    options: ImmutableList<ThemeOptionUIState>,
    onOptionClick: (ThemeOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
    ) {
        options.forEach { option ->
            ThemeOptionRow(option = option, onClick = { onOptionClick(option.option) })
        }
    }
}

@Composable
private fun ThemeOptionRow(option: ThemeOptionUIState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = BakaMitaiTheme.spacing
    val rowColor = if (option.isSelected) {
        colors.surfaceContainer
    } else {
        colors.surfaceContainer.copy(alpha = UNSELECTED_ROW_ALPHA)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ThemeOptionMinHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(rowColor)
            .selectable(selected = option.isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemeOptionIcon(icon = option.icon, isSelected = option.isSelected)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title.asString(),
                color = colors.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = option.description.asString(),
                color = colors.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = DESCRIPTION_MAX_LINES,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (option.isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(CheckSize),
                tint = colors.primary,
            )
        }
    }
}

/** The option's icon in a circle: primary on a translucent primary circle when selected. */
@Composable
private fun ThemeOptionIcon(icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val circleColor = if (isSelected) colors.primary.copy(alpha = SELECTED_ICON_ALPHA) else colors.surfaceContainer
    Box(
        modifier = modifier
            .size(IconCircleSize)
            .background(color = circleColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(IconSize),
            tint = if (isSelected) colors.primary else colors.onSurfaceVariant,
        )
    }
}

/** Three row-shaped blocks, as tall as a row with one line of title and one of description. */
@Composable
fun ThemeOptionsSkeletonView(modifier: Modifier = Modifier) {
    val spacing = BakaMitaiTheme.spacing
    val typography = MaterialTheme.typography
    val textHeight = textLineHeight(typography.titleSmall) + textLineHeight(typography.bodySmall)
    val rowHeight = max(ThemeOptionMinHeight, max(IconCircleSize, textHeight) + spacing.sm * 2)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        repeat(SKELETON_ROW_COUNT) {
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun ThemeOptionsViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.lg),
            ) {
                ThemeMode.entries.forEach { mode ->
                    ThemeOptionsView(options = mode.toThemeOptionUIStates(), onOptionClick = {})
                }
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun ThemeOptionsSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                ThemeOptionsSkeletonView()
            }
        }
    }
}
