package com.zc.bakamitai.compose.features.home.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.common.roundedBackground
import com.zc.bakamitai.compose.core.domain.DateFormattedResult
import com.zc.bakamitai.compose.core.domain.asString
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.presentation.ReleaseUiModel

@Composable
fun EntryListItem(
    item: ReleaseUiModel, onClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                onClick(item.release.page)
            }
            .padding(8.dp), verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.release.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.release.show,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.release.show,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = "Airing time",
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn
                    )
                )
                Text(
                    text = item.formattedDate.asString(),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                )
            }
        }
        Text(
            text = UiText.StringResource(R.string.ep_, item.release.episode).asString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .roundedBackground(MaterialTheme.colorScheme.primary)
                .align(Alignment.CenterVertically)
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
fun PreviewEntryListItem() {
    val releaseUiModel = ReleaseUiModel(
        release = Release(
            show = "Detective Conan",
            page = "",
            imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
            time = "07:01",
            episode = "13",
            xdcc = "",
            releaseDate = "Fri, 13 Feb 2026 04:02:03 +0200",
            downloads = emptyList(),
        ), formattedDate = DateFormattedResult.None
    )
    EntryListItem(
        item = releaseUiModel,
    )
}
