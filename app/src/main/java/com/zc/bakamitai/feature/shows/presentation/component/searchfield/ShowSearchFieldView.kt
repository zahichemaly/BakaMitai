package com.zc.bakamitai.feature.shows.presentation.component.searchfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

/**
 * The catalogue's search field: a 56 dp pill that filters as the user types. Its container is a step above the
 * top bar's tint and brightens while it has focus. The clear button appears once there is text and keeps the
 * keyboard open; the keyboard's Search key hides it. TalkBack announces a search field, with the placeholder as
 * its hint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowSearchFieldView(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current
    SearchBarDefaults.InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { keyboardController?.hide() },
        // Never expanded: the list under the field is the result, so there is no search view to open.
        expanded = false,
        onExpandedChange = {},
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = UiText.StringResource(StringResource.all_shows_search_placeholder).asString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = UiText.StringResource(StringResource.cd_all_shows_clear_search)
                            .asString(),
                    )
                }
            }
        } else {
            null
        },
        colors = SearchBarDefaults.inputFieldColors(
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            focusedLeadingIconColor = colors.primary,
            unfocusedLeadingIconColor = colors.primary,
            focusedTrailingIconColor = colors.onSurfaceVariant,
            unfocusedTrailingIconColor = colors.onSurfaceVariant,
            focusedPlaceholderColor = colors.outline,
            unfocusedPlaceholderColor = colors.outline,
            // One step above the bar, which turns surfaceContainer once the list scrolls under it.
            focusedContainerColor = colors.surfaceContainerHighest,
            unfocusedContainerColor = colors.surfaceContainerHigh,
        ),
    )
}

/** Samples: empty (the placeholder), and with text (the clear button). */
private class ShowSearchFieldPreviewProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("", "conan")
}

@AdaptivePreviews
@Composable
private fun ShowSearchFieldViewPreview(@PreviewParameter(ShowSearchFieldPreviewProvider::class) query: String) {
    BakaMitaiTheme {
        Surface {
            Column(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                ShowSearchFieldView(query = query, onQueryChange = {}, onClearClick = {})
            }
        }
    }
}
