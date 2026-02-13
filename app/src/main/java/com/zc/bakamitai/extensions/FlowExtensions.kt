package com.zc.bakamitai.extensions

import com.zc.bakamitai.data.models.ErrorResponse
import com.zc.bakamitai.data.models.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response

suspend fun <T> MutableStateFlow<Resource<T>>.setLoading() {
    emit(Resource.Loading())
}

suspend fun <T> MutableStateFlow<Resource<T>>.setSuccess(data: T) {
    emit(Resource.Success(data))
}

suspend fun <T, R> MutableStateFlow<Resource<T>>.setError(response: Response<R>) =
    emit(
        Resource.Error(
            ErrorResponse(
                code = response.code(),
                message = response.message()
            )
        )
    )
