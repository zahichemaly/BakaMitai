package com.zc.bakamitai.compose.features.home.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.features.home.domain.model.Schedule

@Composable
fun EntryGridItem(
    release: Schedule,
    is12HourFormat: Boolean,
    onPageClicked: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        onClick = {
            onPageClicked(release.page)
        },
        modifier = Modifier
            .width(200.dp)
            .height(300.dp)
            .padding(dimensionResource(id = R.dimen.spacing_small)),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(release.imageUrl)
                    .crossfade(true)
                    .build(),
                //placeholder = painterResource(R.mipmap.ic_launcher),
                contentDescription = release.title,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.spacing_small))
            ) {
                Text(
                    text = release.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
                Text(
                    text = release.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
                val airedText: String
                val airedTextColor: Color

                if (release.aired) {
                    airedText = stringResource(R.string.aired)
                    airedTextColor = colorResource(R.color.color_aired)
                } else {
                    airedText = stringResource(R.string.not_aired)
                    airedTextColor = colorResource(R.color.color_not_aired)
                }
                Text(
                    text = airedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = airedTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewEntryGridItem() {
    val releases = listOf(
        Schedule(
            title = "Detective Conan Detective Conan Detective Conan Detective Conan Detective Conan",
            page = "https://www.detectiveconanworld.com/",
            imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
            time = "18:00",
            aired = true,
        ),
        Schedule(
            title = "Detective Conan",
            page = "https://www.detectiveconanworld.com/",
            imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
            time = "18:00",
            aired = false,
        )
    )
    Column {
        releases.forEach { entry ->
            EntryGridItem(
                release = entry,
                is12HourFormat = entry.aired,
                onPageClicked = {}
            )
        }
    }
}
