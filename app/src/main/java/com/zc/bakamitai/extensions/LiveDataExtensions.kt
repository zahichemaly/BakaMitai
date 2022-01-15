package com.zc.bakamitai.extensions

import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.ErrorResponse
import com.zc.bakamitai.data.models.Resource
import retrofit2.Response

fun <T> MutableLiveData<Resource<T>>.setLoading() {
    value = Resource.Loading()
}

fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T) {
    value = Resource.Success(data)
}

fun <T,R> MutableLiveData<Resource<T>>.setError(response: Response<R>) {
    value = Resource.Error(
        ErrorResponse(
            code = response.code(),
            message = response.message()
        )
    )
}

fun <T> MutableLiveData<Resource<T>>.setError(exception: Exception) {
    value = Resource.Error(
        ErrorResponse(
            exception = exception
        )
    )
}
