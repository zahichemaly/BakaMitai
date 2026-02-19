package com.zc.bakamitai.compose.features.home.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.core.domain.StringResource
import com.zc.bakamitai.compose.features.details.navigation.DetailsNavRoute
import com.zc.bakamitai.compose.features.home.domain.model.Release
import com.zc.bakamitai.compose.features.home.presentation.component.HomeHeader
import com.zc.bakamitai.compose.features.home.presentation.component.HomeListColumns
import com.zc.bakamitai.compose.features.home.presentation.component.HomeListRows
import com.zc.bakamitai.compose.navigation.Navigator
import org.koin.androidx.compose.koinViewModel

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
@Composable
fun HomeScreen(navigator: Navigator) {
    // In Navigation 3, we can use the local ViewModel or a shared one. 
    // For now, let's use the standard koinViewModel. 
    // If MAIN_GRAPH scoping was essential, further adjustments with NavEntry might be needed.
    val viewModel = koinViewModel<HomeViewModel>()
    val homeUiModel by viewModel.homeUiModel.collectAsStateWithLifecycle()

    HomeContent(
        homeUiModel = homeUiModel,
        onRefresh = viewModel::onRefresh,
        onItemClick = { page ->
            navigator.navigate(
                DetailsNavRoute(
                    title = "Test",
                    summary = "Test",
                    imageUrl = ""
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
                        title = UiText.StringResource(StringResource.today).asString()
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
                        title = UiText.StringResource(StringResource.latest_releases).asString()
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
