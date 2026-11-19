package com.zc.bakamitai.feature.shows.presentation.component.letterheader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.component.skeleton.textLineHeight
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews
import com.zc.bakamitai.feature.shows.presentation.model.IndexLetterUIModel

private const val PRIMARY_BADGE_ALPHA = 0.15f
private const val SECONDARY_BADGE_ALPHA = 0.2f
private val LetterBadgeSize = 32.dp
private val CountPillHorizontalPadding = 10.dp
private val CountPillVerticalPadding = 2.dp
private val SkeletonCountPillWidth = 64.dp
private val PreviewWidth = 320.dp

/**
 * A section's sticky header: the letter badge and the count pill ("14 SHOWS"). Its background is opaque, so rows
 * scroll under it cleanly. TalkBack reads it as one heading: "A, 14 SHOWS" / "Numbers and symbols, 9 SHOWS".
 */
@Composable
fun LetterHeaderView(letter: IndexLetterUIModel, countLabel: UiText, modifier: Modifier = Modifier) {
    LetterHeaderRow(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .semantics(mergeDescendants = true) { heading() },
        badge = { LetterBadge(letter = letter) },
        pill = { CountPill(countLabel = countLabel) },
    )
}

/**
 * Loading placeholder for [LetterHeaderView]: a badge circle and a count pill of the same sizes. Use it inside a
 * `SkeletonView`.
 */
@Composable
fun LetterHeaderSkeletonView(modifier: Modifier = Modifier) {
    LetterHeaderRow(
        modifier = modifier,
        badge = { SkeletonBlock(modifier = Modifier.size(letterBadgeSize()), shape = CircleShape) },
        pill = {
            SkeletonBlock(
                modifier = Modifier.size(
                    width = SkeletonCountPillWidth,
                    height = textLineHeight(MaterialTheme.typography.labelSmall) + CountPillVerticalPadding * 2,
                ),
                shape = CircleShape,
            )
        },
    )
}

/**
 * The badge at the start and the pill at the end: `xs` above them, and under them `xs` plus the `xs` gap to the
 * section's card, inside the header's background.
 */
@Composable
private fun LetterHeaderRow(
    badge: @Composable () -> Unit,
    pill: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = BakaMitaiTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = spacing.xs, bottom = spacing.xs + spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        badge()
        Spacer(modifier = Modifier.weight(1f))
        pill()
    }
}

/** The letter in a tinted circle, alternating primary and secondary; TalkBack reads its description. */
@Composable
private fun LetterBadge(letter: IndexLetterUIModel, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val description = letter.description.asString()
    Box(
        modifier = modifier
            .size(letterBadgeSize())
            .background(
                color = if (letter.isPrimaryAccent) {
                    colors.primary.copy(alpha = PRIMARY_BADGE_ALPHA)
                } else {
                    colors.secondaryContainer.copy(alpha = SECONDARY_BADGE_ALPHA)
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.label.asString(),
            modifier = Modifier.semantics { contentDescription = description },
            color = if (letter.isPrimaryAccent) colors.primary else colors.secondary,
            maxLines = 1,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

/** "14 SHOWS". */
@Composable
private fun CountPill(countLabel: UiText, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.secondary,
    ) {
        Text(
            text = countLabel.asString(),
            modifier = Modifier.padding(horizontal = CountPillHorizontalPadding, vertical = CountPillVerticalPadding),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** The design's 32 dp, or more when a large font scale needs it, so the letter is never clipped. */
@Composable
@ReadOnlyComposable
private fun letterBadgeSize(): Dp = max(LetterBadgeSize, textLineHeight(MaterialTheme.typography.titleLarge))

/** Samples with their show count: a primary letter, a secondary letter with one show, and "#". */
private class LetterHeaderPreviewProvider : PreviewParameterProvider<Pair<IndexLetterUIModel, Int>> {
    override val values = sequenceOf(
        IndexLetterUIModel('A', UiText.StringValue("A"), UiText.StringValue("A"), isPrimaryAccent = true) to 14,
        IndexLetterUIModel('B', UiText.StringValue("B"), UiText.StringValue("B"), isPrimaryAccent = false) to 1,
        IndexLetterUIModel(
            letter = '#',
            label = UiText.StringValue("#"),
            description = UiText.StringResource(StringResource.cd_all_shows_other_section),
            isPrimaryAccent = false,
        ) to 9,
    )
}

@AdaptivePreviews
@Composable
private fun LetterHeaderViewPreview(
    @PreviewParameter(LetterHeaderPreviewProvider::class) sample: Pair<IndexLetterUIModel, Int>,
) {
    val (letter, count) = sample
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                LetterHeaderView(
                    letter = letter,
                    countLabel = UiText.StringPlural(PluralResource.all_shows_section_count, count, listOf(count)),
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun LetterHeaderSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                LetterHeaderSkeletonView(modifier = Modifier.width(PreviewWidth))
            }
        }
    }
}
