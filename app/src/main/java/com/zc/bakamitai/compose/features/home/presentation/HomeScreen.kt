package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.features.details.DetailsUiModel
import com.zc.bakamitai.compose.features.details.navigateToDetailsScreen
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.presentation.components.HomeHeader
import com.zc.bakamitai.compose.features.home.presentation.components.HomeListColumns
import com.zc.bakamitai.compose.features.home.presentation.components.HomeListRows
import com.zc.bakamitai.compose.navigation.MAIN_GRAPH
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
@Composable
fun HomeScreen(navController: NavController) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(MAIN_GRAPH)
    }
    val viewModel = koinViewModel<HomeViewModel>(viewModelStoreOwner = parentEntry)
    val homeUiModel by viewModel.homeUiModel.collectAsStateWithLifecycle()

    HomeContent(
        homeUiModel = homeUiModel,
        onRefresh = viewModel::onRefresh,
        onItemClick = { page ->
            navController.navigateToDetailsScreen(
                params = DetailsUiModel(
                    title = "Test",
                    summary = "Test",
                    imageUrl = "",
                    downloads = emptyList()
                )
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeUiModel: HomeUiModel,
    onRefresh: () -> Unit,
    onItemClick: (String) -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = homeUiModel.isLoading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (homeUiModel.hasData()) {
                item {
                    HomeHeader(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = UiText.StringResource(R.string.today).asString()
                    )
                }
                item {
                    HomeListRows(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        items = homeUiModel.todayReleases.schedule,
                        onItemClick = onItemClick
                    )
                }
                item {
                    HomeHeader(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = UiText.StringResource(R.string.latest_releases).asString()
                    )
                }
                item {
                    HomeListColumns(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        items = homeUiModel.latestReleases.values.toList(),
                        onItemClick = onItemClick
                    )
                }
            }
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
                show = "Detective Conan",
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
