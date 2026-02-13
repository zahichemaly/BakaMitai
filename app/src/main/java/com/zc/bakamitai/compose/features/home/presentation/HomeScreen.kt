package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.presentation.components.HomeHeader
import com.zc.bakamitai.compose.features.home.presentation.components.HomeListColumns
import com.zc.bakamitai.compose.features.home.presentation.components.HomeListRows
import com.zc.bakamitai.compose.navigation.HomeGraph
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
@Composable
fun HomeScreen(navController: NavController) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(HomeGraph.route)
    }
    val viewModel = koinViewModel<HomeViewModel>(viewModelStoreOwner = parentEntry)
    val homeUiModel by viewModel.homeUiModel.collectAsStateWithLifecycle()

    HomeContent(
        homeUiModel = homeUiModel,
        onRefresh = viewModel::onRefresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    homeUiModel: HomeUiModel,
    onRefresh: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }

    LaunchedEffect(homeUiModel.isLoading) {
        if (homeUiModel.isLoading) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                HomeHeader(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = UiText.StringResource(R.string.today).asString()
                )
            }
            item {
                HomeListRows(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    items = homeUiModel.todayReleases.schedule
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
                    items = homeUiModel.latestReleases.values.toList()
                )
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState
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
