package com.zc.bakamitai.compose.features.details.domain.repository

import com.zc.bakamitai.compose.features.details.domain.model.ShowDetails

interface ShowDetailsRepository {
    suspend fun getShowDetails(page: String): ShowDetails
}
