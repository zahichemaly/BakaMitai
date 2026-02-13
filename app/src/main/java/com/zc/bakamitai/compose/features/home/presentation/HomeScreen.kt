package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.presentation.components.HomeHeader
import com.zc.bakamitai.compose.features.home.presentation.components.HomeListColumns
import com.zc.bakamitai.compose.features.home.presentation.components.HomeListRows
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        HomeHeader(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = UiText.StringResource(R.string.today).asString()
        )
        HomeListRows(
            modifier = Modifier.padding(horizontal = 8.dp),
            items = homeUiModel.todayReleases.schedule
        )
        HomeHeader(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = UiText.StringResource(R.string.latest_releases).asString()
        )
        HomeListColumns(
            modifier = Modifier.padding(horizontal = 8.dp),
            items = homeUiModel.latestReleases.values.toList()
        )
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
                page = "",
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
