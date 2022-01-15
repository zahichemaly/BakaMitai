package com.zc.bakamitai.data.network.repos

import com.zc.bakamitai.data.models.EntryResponse
import retrofit2.Response

interface SubsPleaseRepository {
    suspend fun getLatest(): Response<HashMap<String, EntryResponse>>
}
