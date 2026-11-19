package com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.mapper.toTimeFormatSegments
import com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.mapper.toWeekStartSegments
import com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.model.SegmentUIState
import kotlinx.collections.immutable.ImmutableList

/** The track: the segments' 48 dp touch height, with the selected 40 dp pill centered in it. */
private val TrackHeight = 48.dp
private val TrackHorizontalPadding = 4.dp
private val SegmentSpacing = 4.dp
private const val QUALIFIER_ALPHA = 0.8f

/**
 * A two-choice preference as a pill track: the selected segment is a filled primary pill. It's a radio group for
 * TalkBack ("Monday (Default), selected, 1 of 2"); a tap reports the segment's value.
 */
@Composable
fun <T> SegmentedPreferenceView(
    segments: ImmutableList<SegmentUIState<T>>,
    onSegmentClick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val segmentColors = SegmentedButtonDefaults.colors(
        activeContainerColor = colors.primary,
        activeContentColor = colors.onPrimary,
        activeBorderColor = Color.Transparent,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = colors.onSurfaceVariant,
        inactiveBorderColor = Color.Transparent,
    )
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.surfaceContainer, shape = CircleShape)
            .padding(horizontal = TrackHorizontalPadding),
        space = SegmentSpacing,
    ) {
        segments.forEach { segment ->
            SegmentedButton(
                selected = segment.isSelected,
                onClick = { onSegmentClick(segment.value) },
                shape = CircleShape,
                colors = segmentColors,
                icon = {},
                label = { SegmentLabel(segment = segment) },
            )
        }
    }
}

/** The label, then its dimmed qualifier, which ellipsizes first at large font scales. */
@Composable
private fun <T> SegmentLabel(segment: SegmentUIState<T>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = segment.label.asString(), maxLines = 1, style = MaterialTheme.typography.labelMedium)
        segment.qualifier?.let { qualifier ->
            Text(
                text = qualifier.asString(),
                modifier = Modifier.weight(1f, fill = false),
                color = LocalContentColor.current.copy(alpha = QUALIFIER_ALPHA),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** The track's placeholder: a pill of the same height. */
@Composable
fun SegmentedPreferenceSkeletonView(modifier: Modifier = Modifier) {
    SkeletonBlock(
        modifier = modifier
            .fillMaxWidth()
            .height(TrackHeight),
        shape = CircleShape,
    )
}

@AdaptivePreviews
@Composable
private fun SegmentedPreferenceViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            ) {
                SegmentedPreferenceView(segments = WeekStart.MONDAY.toWeekStartSegments(), onSegmentClick = {})
                SegmentedPreferenceView(segments = WeekStart.SUNDAY.toWeekStartSegments(), onSegmentClick = {})
                SegmentedPreferenceView(segments = TimeFormat.TWELVE_HOUR.toTimeFormatSegments(), onSegmentClick = {})
                SegmentedPreferenceView(
                    segments = TimeFormat.TWENTY_FOUR_HOUR.toTimeFormatSegments(),
                    onSegmentClick = {},
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun SegmentedPreferenceSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                SegmentedPreferenceSkeletonView()
            }
        }
    }
}
