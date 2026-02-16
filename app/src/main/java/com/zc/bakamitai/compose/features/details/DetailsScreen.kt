package com.zc.bakamitai.compose.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.ExpandableText
import com.zc.bakamitai.compose.common.UiText

@Composable
fun DetailsScreen(detailsUiModel: DetailsUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(detailsUiModel.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = detailsUiModel.title,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .width(150.dp)
                .height(200.dp)
                .clip(CircleShape),
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = detailsUiModel.title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        //region Summary Section
        DetailsHeaderView(
            title = UiText.StringResource(R.string.synopsis).asString()
        )

        ExpandableText(
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            arrowColor = MaterialTheme.colorScheme.onSurface,
            text = detailsUiModel.summary,
            collapsedMaxLines = 5,
            expanded = false,
        )

        //endregion

        //region Downloads Section
        DetailsHeaderView(
            title = UiText.StringResource(R.string.downloads).asString()
        )

        detailsUiModel.downloads.forEach { downloadItem ->
            DownloadItemView(downloadItem)
        }
        //endregion
    }
}

@Composable
private fun DetailsHeaderView(title: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth(),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
        text = title
    )
}


@Composable
@Preview
private fun DetailsScreenTest() {
    val downloads = mutableListOf<DownloadUiModel>()
    repeat(12) { index ->
        val episode = index + 1
        val downloadUiModel = DownloadUiModel(
            title = "Episode $episode",
            urls = listOf(
                DownloadUrl(
                    res = "1080",
                    links = listOf(
                        DownloadLink(
                            source = "Magnet",
                            link = "",
                        ),
                        DownloadLink(
                            source = "Torrent",
                            link = "",
                        ),
                        DownloadLink(
                            source = "XDCC",
                            link = "",
                        )
                    )
                ),
                DownloadUrl(
                    res = "720",
                    links = listOf(
                        DownloadLink(
                            source = "Magnet",
                            link = "",
                        ),
                        DownloadLink(
                            source = "Torrent",
                            link = "",
                        ),
                        DownloadLink(
                            source = "XDCC",
                            link = "",
                        )
                    )
                ),
                DownloadUrl(
                    res = "480",
                    links = listOf(
                        DownloadLink(
                            source = "Magnet",
                            link = "",
                        ),
                        DownloadLink(
                            source = "Torrent",
                            link = "",
                        ),
                        DownloadLink(
                            source = "XDCC",
                            link = "",
                        )
                    )
                )
            )
        )
        downloads.add(downloadUiModel)
    }

    val detailsUiModel = DetailsUiModel(
        title = "Detective Conan",
        imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
        summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam tincidunt bibendum purus, sed congue sem aliquet non. Nullam tincidunt pretium gravida. Maecenas lacinia est justo, eget fringilla lectus interdum nec. Suspendisse molestie luctus elementum. Fusce id ante nec orci lobortis suscipit. Sed gravida, ligula eget sagittis sodales, risus lacus feugiat tortor, pulvinar euismod ante dolor nec mauris. Vivamus in eleifend ex. Duis eu odio sit amet purus finibus auctor. Ut a ultricies nisi. Donec elementum, nulla non finibus tincidunt, lorem nibh sollicitudin nisi, vel rhoncus metus tellus id leo. Morbi molestie nunc felis, pharetra vulputate elit feugiat et. Cras non diam maximus, pharetra nunc sit amet, tincidunt mauris. Sed at elementum sem, eget faucibus lorem. Etiam lobortis suscipit eros sed sodales. Donec luctus dictum augue, vel rhoncus purus tempus volutpat. Nunc et neque lectus.",
        downloads
    )
    DetailsScreen(detailsUiModel)
}
