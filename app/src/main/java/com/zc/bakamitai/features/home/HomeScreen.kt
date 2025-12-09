package com.zc.bakamitai.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.dtos.EntryDto
import com.zc.bakamitai.features.home.components.EntryGridItem
import com.zc.bakamitai.features.home.components.EntryListItem

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
@Composable
fun HomeScreen() {
    HomeContent()
}

@Composable
fun HomeContent(
    upcomingEntries: List<EntryDto> = emptyList(),
    latestEntries: List<EntryDto> = emptyList()
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HomeHeader(title = stringResource(R.string.today))
        HomeListRows(upcomingEntries)
        HomeHeader(title = stringResource(R.string.latest_releases))
        HomeListColumns(latestEntries)
    }
}

@Composable
fun HomeHeader(title: String) {
    Text(
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
fun HomeListRows(entries: List<EntryDto>) {
    LazyRow {
        items(entries) { item ->
            EntryGridItem(entryDto = item, is12HourFormat = false) { }
        }
    }
}

@Composable
fun HomeListColumns(entries: List<EntryDto>) {
    LazyColumn {
        items(entries) { item ->
            EntryListItem(entryDto = item, is12HourFormat = false) { }
        }
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    val entries = mutableListOf<EntryDto>()
    repeat(25) {
        entries.add(
            EntryDto(
                name = "Detective Conan Detective Conan Detective Conan Detective Conan Detective Conan",
                page = "https://www.detectiveconanworld.com/",
                imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
                time = "18:00",
                aired = true
            )
        )
    }
    HomeContent(
        upcomingEntries = entries,
        latestEntries = entries
    )
}
