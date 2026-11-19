package com.zc.bakamitai.common.ui.component.emptystate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private val IconContainerSize = 64.dp
private val IconSize = 32.dp
private val MessageMaxWidth = 320.dp

/**
 * A list's "nothing to show" message: [icon] in a tonal circle, [title] (a TalkBack heading) and [message], with
 * an optional [action] (e.g. a reset button) under them. It fills the width and centers its content, so it can
 * sit as an item of a lazy list or grid.
 */
@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: UiText,
    message: UiText,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = BakaMitaiTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(IconContainerSize)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(IconSize),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        Text(
            text = title.asString(),
            modifier = Modifier.semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = message.asString(),
            modifier = Modifier.widthIn(max = MessageMaxWidth),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        action?.invoke()
    }
}

/** Samples: whether the empty state has an action. */
private class EmptyStatePreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@AdaptivePreviews
@Composable
private fun EmptyStateViewPreview(@PreviewParameter(EmptyStatePreviewProvider::class) hasAction: Boolean) {
    BakaMitaiTheme {
        Surface {
            if (hasAction) {
                EmptyStateView(
                    icon = Icons.Outlined.CalendarViewDay,
                    title = UiText.StringResource(StringResource.schedule_filter_empty_title),
                    message = UiText.StringResource(StringResource.schedule_filter_empty_message),
                    action = {
                        Button(onClick = {}) {
                            Text(text = UiText.StringResource(StringResource.schedule_filter_reset).asString())
                        }
                    },
                )
            } else {
                EmptyStateView(
                    icon = Icons.Outlined.SearchOff,
                    title = UiText.StringResource(StringResource.all_shows_no_results_title),
                    message = UiText.StringResource(StringResource.all_shows_no_results_message),
                )
            }
        }
    }
}
