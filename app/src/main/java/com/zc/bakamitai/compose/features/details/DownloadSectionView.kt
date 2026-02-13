package com.zc.bakamitai.compose.features.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zc.bakamitai.compose.common.roundedBackground

data class DownloadUiModel(
    val title: String,
    val urls: List<DownloadUrl>
)

data class DownloadUrl(
    val res: String,
    val links: List<DownloadLink>,
)

data class DownloadLink(
    val source: String,
    val link: String,
)

@Composable
fun DownloadSectionView(itemModels: List<DownloadUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            text = "Downloads",
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            items(itemModels, key = {
                it.title
            }) { downloadItem ->
                ExpandableView(
                    collapsedContent = {
                        Text(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            text = downloadItem.title
                        )
                    },
                    expandedContent = {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            downloadItem.urls.forEach { url ->
                                Text(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    text = url.res
                                )
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    items(items = url.links) { link ->
                                        Text(
                                            modifier = Modifier
                                                .roundedBackground(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 16.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            text = link.source,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    expanded = true,
                )
            }
        }
    }
}


@Composable
@Preview
private fun DownloadSectionViewTest() {
    val downloadUiModel = DownloadUiModel(
        title = "Episode 17",
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
    DownloadSectionView(listOf(downloadUiModel))
}
