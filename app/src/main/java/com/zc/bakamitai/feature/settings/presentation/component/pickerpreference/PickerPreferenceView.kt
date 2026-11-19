package com.zc.bakamitai.feature.settings.presentation.component.pickerpreference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonTextLine
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.settings.presentation.component.settingsicon.SettingsIconView
import com.zc.bakamitai.feature.settings.presentation.mapper.toThemePicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toTimeFormatPicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toWeekStartPicker

private val RowMinHeight = 48.dp
private const val SKELETON_TITLE_FRACTION = 0.4f
private const val SKELETON_VALUE_FRACTION = 0.3f

/**
 * A setting picked from a list: its leading [icon], then its [title] with the current [value] below it. A tap opens
 * the picker; TalkBack reads one button, "Theme, System default".
 */
@Composable
fun PickerPreferenceView(
    icon: ImageVector,
    title: UiText,
    value: UiText,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val description = UiText.StringResource(StringResource.cd_settings_picker, listOf(title, value)).asString()
    PickerRowLayout(
        icon = icon,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Text(text = title.asString(), color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
        Text(text = value.asString(), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * The row shared by the view and its skeleton: the [icon], then [content] (the two lines). Its text is cleared from
 * the semantics: the row's own description reads it.
 */
@Composable
private fun PickerRowLayout(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = BakaMitaiTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconView(icon = icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics {},
            content = content,
        )
    }
}

/**
 * The row's placeholder: the same padding and height, the real [icon], a line for the title and one for the value.
 */
@Composable
fun PickerPreferenceSkeletonView(icon: ImageVector, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    PickerRowLayout(icon = icon, modifier = modifier) {
        SkeletonTextLine(style = typography.titleMedium, modifier = Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION))
        SkeletonTextLine(style = typography.bodyMedium, modifier = Modifier.fillMaxWidth(SKELETON_VALUE_FRACTION))
    }
}

@AdaptivePreviews
@Composable
private fun PickerPreferenceViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.xs),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs),
            ) {
                val weekStart = WeekStart.SUNDAY.toWeekStartPicker()
                val timeFormat = TimeFormat.TWELVE_HOUR.toTimeFormatPicker()
                val theme = ThemeMode.DARK.toThemePicker()
                PickerPreferenceView(
                    icon = Icons.Outlined.CalendarMonth,
                    title = weekStart.title,
                    value = weekStart.valueLabel,
                    onClick = {},
                )
                PickerPreferenceView(
                    icon = Icons.Outlined.Schedule,
                    title = timeFormat.title,
                    value = timeFormat.valueLabel,
                    onClick = {},
                )
                PickerPreferenceView(
                    icon = Icons.Outlined.Palette,
                    title = theme.title,
                    value = theme.valueLabel,
                    onClick = {},
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun PickerPreferenceSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.xs)) {
                Column(verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs)) {
                    PickerPreferenceSkeletonView(icon = Icons.Outlined.CalendarMonth)
                    PickerPreferenceSkeletonView(icon = Icons.Outlined.Schedule)
                }
            }
        }
    }
}
