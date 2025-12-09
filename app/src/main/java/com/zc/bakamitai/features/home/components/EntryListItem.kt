package com.zc.bakamitai.features.home.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.EntryDto

@Composable
fun EntryListItem(
    entryDto: EntryDto,
    is12HourFormat: Boolean,
    onPageClicked: (String) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth(),
//            .clickable(onClick = {
//                onPageClicked(entryDto.page)
//            })
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(entryDto.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = entryDto.name,
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = entryDto.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entryDto.getFormattedTime(is12HourFormat),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = entryDto.episode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_xsmall))
            )
        }
    }
}

@Preview
@Composable
fun PreviewEntryListItem() {
    val entry = EntryDto(
        name = "Detective Conan",
        page = "https://www.detectiveconanworld.com/",
        imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
        time = "07:01",
        episode = "Ep 13"
    )
    EntryListItem(
        entryDto = entry,
        is12HourFormat = true,
        onPageClicked = {}
    )
}
