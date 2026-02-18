package com.zc.bakamitai.compose.features.details.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zc.bakamitai.compose.common.ExpandableContent
import com.zc.bakamitai.compose.common.roundedBackground


@Composable
fun DownloadItemView(downloadItem: DownloadUiModel, expanded: Boolean = false) {
    ExpandableContent(
        collapsedContent = {
            Text(
                modifier = Modifier.fillMaxSize(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                text = downloadItem.title,
                textAlign = TextAlign.Left,
            )
        },
        expandedContent = {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                downloadItem.urls.forEach { url ->
                    Text(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        text = url.res
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        url.links.forEach { link ->
                            Text(
                                modifier = Modifier
                                    .roundedBackground(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        link.onClick()
                                    }
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
        expanded = expanded,
    )
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        DownloadItemView(downloadUiModel, expanded = false)
    }
}
