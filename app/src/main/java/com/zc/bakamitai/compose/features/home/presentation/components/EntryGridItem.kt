package com.zc.bakamitai.compose.features.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
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
import com.zc.bakamitai.compose.features.home.domain.model.Schedule
import com.zc.bakamitai.compose.ui.ColorGreyLight

@Composable
fun EntryGridItem(
    item: Schedule, onClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        onClick = {
            onClick(item.page)
        },
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(item.imageUrl).crossfade(true)
                        .build(),
                    //placeholder = painterResource(R.mipmap.ic_launcher),
                    contentDescription = item.title,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                val airedText: String
                val airedTextColor: Color

                if (item.aired) {
                    airedText = UiText.StringResource(R.string.aired).asString()
                    airedTextColor = colorResource(R.color.color_aired)
                } else {
                    airedText = UiText.StringResource(R.string.not_aired).asString()
                    airedTextColor = colorResource(R.color.color_not_aired)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Aired/Not Aired text
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .roundedBackground(ColorGreyLight)
                            .padding(horizontal = 8.dp),
                        text = airedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = airedTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .roundedBackground(ColorGreyLight)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_time),
                            contentDescription = "Airing time",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        )
                    }

                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    modifier = Modifier.defaultMinSize(minHeight = 14.dp),
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
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
        ), Schedule(
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
                item = entry
            )
        }
    }
}
