package com.zc.bakamitai.compose.features.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.ui.ColorBlackLight

@Composable
fun EntryListItem(
    release: Release,
    onClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick(release.page)
            },
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(release.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = release.show,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = release.show,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = release.time,
                style = MaterialTheme.typography.bodyMedium,
                color = ColorBlackLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Card(
            modifier = Modifier.align(Alignment.CenterVertically),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = UiText.StringResource(R.string.ep_, release.episode).asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(6.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewEntryListItem() {
    val release = Release(
        show = "Detective Conan",
        page = "",
        imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
        time = "07:01",
        episode = "Ep 13",
        xdcc = "",
        releaseDate = "",
        downloads = emptyList(),
    )
    EntryListItem(
        release = release,
    )
}
