package com.zc.bakamitai.compose.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.compose.core.domain.Resource
import com.zc.bakamitai.compose.features.home.domain.model.LatestRelease
import com.zc.bakamitai.compose.features.home.domain.repository.ReleaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val releaseRepository: ReleaseRepository): ViewModel() {

    private val _homeUiModel = MutableStateFlow(HomeUiModel())
    val homeUiModel: StateFlow<HomeUiModel> = _homeUiModel.asStateFlow()

    init {
        viewModelScope.launch {
            val latestDef = async { releaseRepository.getLatest() }
            val todayReleaseDef = async { releaseRepository.getToday() }
            val latestReleases = latestDef.await()
            val todayReleases = todayReleaseDef.await()

            if (latestReleases is Resource.Success && todayReleases is Resource.Success) {
                _homeUiModel.update { HomeUiModel(
                    latestReleases = latestReleases.data,
                    todayReleases = todayReleases.data
                ) }
            }
        }
    }
}
