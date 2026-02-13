package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.domain.model.Schedule
import com.zc.bakamitai.compose.features.home.domain.model.TodaySchedule
import com.zc.bakamitai.compose.features.home.presentation.components.EntryGridItem
import com.zc.bakamitai.compose.features.home.presentation.components.EntryListItem
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
@Composable
fun HomeScreen() {
    val viewModel = koinViewModel<HomeViewModel>()
    val homeUiModel by viewModel.homeUiModel.collectAsStateWithLifecycle()
    HomeContent(homeUiModel)
}

@Composable
fun HomeContent(homeUiModel: HomeUiModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HomeHeader(title = stringResource(R.string.today))
        HomeListRows(homeUiModel.todayReleases.schedule)
        HomeHeader(title = stringResource(R.string.latest_releases))
        HomeListColumns(homeUiModel.latestReleases.values.toList())
    }
}

@Composable
fun HomeHeader(title: String) {
    Text(
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
fun HomeListRows(releases: List<Schedule>) {
    LazyRow {
        items(releases) { item ->
            EntryGridItem(release = item, is12HourFormat = false) { }
        }
    }
}

@Composable
fun HomeListColumns(releases: List<Release>) {
    LazyColumn {
        items(releases) { item ->
            EntryListItem(release = item, is12HourFormat = false) { }
        }
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    val entries = mutableListOf<Release>()
    repeat(25) {
        entries.add(
            Release(
                show = "Detective Conan Detective Conan Detective Conan Detective Conan Detective Conan",
                page = 1,
                imageUrl = "https://subsplease.org/wp-content/uploads/2020/10/75199.jpg",
                time = "18:00",
                xdcc = "",
                releaseDate = "",
                downloads = emptyList(),
                episode = "12"
            )
        )
    }
}
