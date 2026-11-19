package com.zc.bakamitai.feature.shows.presentation.component.letterchip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.shows.presentation.model.IndexLetterUIModel

private val LetterChipMinWidth = 40.dp

/** `FilterChip`'s horizontal padding around its label: 8 dp for the chip and 8 dp for the label, on each side. */
private val ChipHorizontalPadding = 32.dp

/** The label's minimum width, so a narrow letter ("I") still makes a [LetterChipMinWidth] chip, centered. */
private val LabelMinWidth = LetterChipMinWidth - ChipHorizontalPadding

/**
 * A quick-jump chip: the section's letter, filled when its section is at the top of the list. TalkBack reads the
 * letter (or "Numbers and symbols"), "selected" for the active chip, and "Jump to …" as the action.
 */
@Composable
fun LetterChipView(letter: IndexLetterUIModel, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val description = letter.description.asString()
    val jumpLabel = UiText.StringResource(StringResource.cd_all_shows_jump_to, listOf(letter.description)).asString()
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = letter.label.asString(),
                modifier = Modifier
                    .widthIn(min = LabelMinWidth)
                    .semantics { contentDescription = description },
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        // Names the chip's click action; the chip keeps its own click handler.
        modifier = modifier
            .widthIn(min = LetterChipMinWidth)
            .semantics { onClick(label = jumpLabel, action = null) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceContainerHigh,
            labelColor = colors.onSurfaceVariant,
            selectedContainerColor = colors.primary,
            selectedLabelColor = colors.onPrimary,
        ),
        border = null,
    )
}

/**
 * Loading placeholder for [LetterChipView]: a pill of the chip's size, in the chip's 48 dp touch-target row. Use it
 * inside a `SkeletonView`.
 */
@Composable
fun LetterChipSkeletonView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.minimumInteractiveComponentSize(), contentAlignment = Alignment.Center) {
        SkeletonBlock(
            modifier = Modifier.size(width = LetterChipMinWidth, height = FilterChipDefaults.Height),
            shape = CircleShape,
        )
    }
}

/** Samples with whether the chip is selected: a letter, the same selected, a narrow letter, and "#". */
private class LetterChipPreviewProvider : PreviewParameterProvider<Pair<IndexLetterUIModel, Boolean>> {
    private fun letter(char: Char) =
        IndexLetterUIModel(char, UiText.StringValue("$char"), UiText.StringValue("$char"), isPrimaryAccent = true)

    override val values = sequenceOf(
        letter('A') to false,
        letter('A') to true,
        letter('I') to false,
        IndexLetterUIModel(
            letter = '#',
            label = UiText.StringValue("#"),
            description = UiText.StringResource(StringResource.cd_all_shows_other_section),
            isPrimaryAccent = false,
        ) to false,
    )
}

@AdaptivePreviews
@Composable
private fun LetterChipViewPreview(
    @PreviewParameter(LetterChipPreviewProvider::class) sample: Pair<IndexLetterUIModel, Boolean>,
) {
    val (letter, selected) = sample
    BakaMitaiTheme {
        Surface {
            Row(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                LetterChipView(letter = letter, selected = selected, onClick = {})
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun LetterChipSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.xs)) {
                    LetterChipSkeletonView()
                    LetterChipSkeletonView()
                }
            }
        }
    }
}
