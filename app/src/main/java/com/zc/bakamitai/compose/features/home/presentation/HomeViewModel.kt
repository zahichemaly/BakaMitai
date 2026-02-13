package com.zc.bakamitai.compose.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.compose.core.domain.DateFormattedResult
import com.zc.bakamitai.compose.core.domain.Resource
import com.zc.bakamitai.compose.features.home.domain.repository.ReleaseRepository
import com.zc.bakamitai.extensions.toDateFormattedResult
import com.zc.bakamitai.extensions.toDateTime
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(private val releaseRepository: ReleaseRepository) : ViewModel() {

    private val _homeUiModel = MutableStateFlow(HomeUiModel())
    val homeUiModel: StateFlow<HomeUiModel> = _homeUiModel.asStateFlow()

    init {
        loadData(isRefresh = false)
    }

    fun onRefresh() {
        loadData(isRefresh = true)
    }

    private fun loadData(isRefresh: Boolean) {
        Timber.d("Loading data with refresh $isRefresh")

        // Only load if it's a manual refresh OR if we don't have data yet
        if (!isRefresh && (_homeUiModel.value.todayReleases.schedule.isNotEmpty() || _homeUiModel.value.latestReleases.isNotEmpty())) {
            return
        }

        viewModelScope.launch {
            _homeUiModel.update { it.copy(isLoading = true) }

            val latestDef = async { releaseRepository.getLatest() }
            val todayReleaseDef = async { releaseRepository.getToday() }
            val latestReleases = latestDef.await()
            val todayReleases = todayReleaseDef.await()

            if (latestReleases is Resource.Success && todayReleases is Resource.Success) {
                val latestReleases = latestReleases.data
                val todayReleases = todayReleases.data

                val releaseUiModels = latestReleases.mapValues { keyValue ->
                    val date = keyValue.value.releaseDate.toDateTime()?.toDateFormattedResult()
                        ?: DateFormattedResult.None
                    ReleaseUiModel(keyValue.value, date)
                }

                _homeUiModel.update {
                    HomeUiModel(
                        latestReleases = releaseUiModels,
                        todayReleases = todayReleases,
                        isLoading = false
                    )
                }
            } else {
                _homeUiModel.update { it.copy(isLoading = false) }
            }
        }
    }
}
