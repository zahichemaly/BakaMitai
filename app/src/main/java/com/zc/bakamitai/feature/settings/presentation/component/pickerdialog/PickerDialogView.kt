package com.zc.bakamitai.feature.settings.presentation.component.pickerdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.settings.presentation.mapper.toThemePicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toTimeFormatPicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toWeekStartPicker
import com.zc.bakamitai.feature.settings.presentation.model.PickerOptionUIState
import com.zc.bakamitai.feature.settings.presentation.model.PickerUIState
import kotlinx.collections.immutable.ImmutableList

private val OptionMinHeight = 48.dp

/**
 * A picker setting's options in a dialog titled with the setting's name: a radio list, then Cancel. A tap on an
 * option reports it through [onOptionClick] (the caller saves it and closes the dialog); Cancel, Back and a tap
 * outside call [onDismiss].
 */
@Composable
fun <T> PickerDialogView(
    title: UiText,
    options: ImmutableList<PickerOptionUIState<T>>,
    onOptionClick: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = UiText.StringResource(StringResource.settings_picker_cancel).asString())
            }
        },
        title = { Text(text = title.asString()) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState()),
            ) {
                options.forEach { option ->
                    PickerOptionRow(option = option, onClick = { onOptionClick(option.option) })
                }
            }
        },
    )
}

/** A radio button, the label and the optional description; the whole row is the radio button for TalkBack. */
@Composable
private fun <T> PickerOptionRow(option: PickerOptionUIState<T>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = BakaMitaiTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = OptionMinHeight)
            .clip(MaterialTheme.shapes.small)
            .selectable(selected = option.isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = option.isSelected, onClick = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = option.label.asString(), color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
            option.description?.let { description ->
                Text(
                    text = description.asString(),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** Each picker: First day of week (Sunday), Time format (12-hour) and Theme (Dark, with descriptions). */
private class PickerDialogPreviewProvider : PreviewParameterProvider<PickerUIState<Any>> {
    override val values = sequenceOf(
        WeekStart.SUNDAY.toWeekStartPicker(),
        TimeFormat.TWELVE_HOUR.toTimeFormatPicker(),
        ThemeMode.DARK.toThemePicker(),
    )
}

@AdaptivePreviews
@Composable
private fun PickerDialogViewPreview(@PreviewParameter(PickerDialogPreviewProvider::class) picker: PickerUIState<Any>) {
    BakaMitaiTheme {
        PickerDialogView(title = picker.title, options = picker.options, onOptionClick = {}, onDismiss = {})
    }
}
