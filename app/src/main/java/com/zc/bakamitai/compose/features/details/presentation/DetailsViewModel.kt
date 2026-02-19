package com.zc.bakamitai.compose.features.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.compose.features.details.domain.repository.ShowDetailsRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailsViewModel(private val showDetailsRepository: ShowDetailsRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            val page = "29-sai-dokushin-chuuken-boukensha-no-nichijou"
            val response = showDetailsRepository.getShowDetails(page)
            Timber.d(response.toString())
        }
    }
}
