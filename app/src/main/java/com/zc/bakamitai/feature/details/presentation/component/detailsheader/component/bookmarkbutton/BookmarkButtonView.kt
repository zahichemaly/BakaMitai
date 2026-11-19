package com.zc.bakamitai.feature.details.presentation.component.detailsheader.component.bookmarkbutton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonBlock
import com.zc.bakamitai.common.ui.component.skeleton.SkeletonView
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import com.zc.bakamitai.common.ui.util.AdaptivePreviews

private val BookmarkButtonHeight = 48.dp
private val BookmarkIconSize = 20.dp

/**
 * The show's full-width bookmark toggle: a filled primary "Bookmarked" button when saved, a tonal "Bookmark" one
 * otherwise. TalkBack reads its state and the action a tap performs.
 */
@Composable
fun BookmarkButtonView(isBookmarked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val stateLabel = UiText.StringResource(
        if (isBookmarked) StringResource.cd_details_bookmarked else StringResource.cd_details_not_bookmarked,
    ).asString()
    val actionLabel = UiText.StringResource(
        if (isBookmarked) StringResource.cd_details_remove_bookmark else StringResource.cd_details_add_bookmark,
    ).asString()
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BookmarkButtonHeight)
            .semantics {
                stateDescription = stateLabel
                onClick(label = actionLabel, action = null)
            },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isBookmarked) colors.primary else colors.surfaceContainerHigh,
            contentColor = if (isBookmarked) colors.onPrimary else colors.onSurface,
        ),
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(BookmarkIconSize),
        )
        Spacer(modifier = Modifier.width(BakaMitaiTheme.spacing.sm))
        Text(
            text = UiText.StringResource(
                if (isBookmarked) StringResource.details_bookmarked else StringResource.details_bookmark,
            ).asString(),
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Loading placeholder for [BookmarkButtonView]: a full-width pill of its height. Use it inside a `SkeletonView`. */
@Composable
fun BookmarkButtonSkeletonView(modifier: Modifier = Modifier) {
    SkeletonBlock(
        modifier = modifier
            .fillMaxWidth()
            .height(BookmarkButtonHeight),
        shape = CircleShape,
    )
}

@AdaptivePreviews
@Composable
private fun BookmarkButtonViewPreview() {
    BakaMitaiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(BakaMitaiTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.md),
            ) {
                BookmarkButtonView(isBookmarked = false, onClick = {})
                BookmarkButtonView(isBookmarked = true, onClick = {})
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun BookmarkButtonSkeletonViewPreview() {
    BakaMitaiTheme {
        Surface {
            SkeletonView(modifier = Modifier.padding(BakaMitaiTheme.spacing.md)) {
                BookmarkButtonSkeletonView()
            }
        }
    }
}
