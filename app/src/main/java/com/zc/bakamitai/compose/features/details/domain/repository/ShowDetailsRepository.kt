package com.zc.bakamitai.compose.features.details.domain.repository

import com.zc.bakamitai.compose.features.details.domain.model.ShowDetails
import com.zc.bakamitai.data.models.Resource

interface ShowDetailsRepository {
    suspend fun getShowDetails(page: String): com.zc.bakamitai.compose.core.domain.Resource<ShowDetails>
}
