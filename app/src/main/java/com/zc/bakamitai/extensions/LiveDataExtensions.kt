package com.zc.bakamitai.extensions

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.zc.bakamitai.data.models.ErrorResponse
import com.zc.bakamitai.data.models.Resource
import retrofit2.Response

fun <T> MutableLiveData<Resource<T>>.setLoading() = postValue(Resource.Loading())

fun <T> MutableLiveData<Resource<T>>.setSuccess(data: T) = postValue(Resource.Success(data))

fun <T, R> MutableLiveData<Resource<T>>.setError(response: Response<R>) =
    postValue(
        Resource.Error(
            ErrorResponse(
                code = response.code(),
                message = response.message()
            )
        )
    )

fun <T> MutableLiveData<Resource<T>>.setError(throwable: Throwable) =
    postValue(
        Resource.Error(
            ErrorResponse(
                message = throwable.localizedMessage
            )
        )
    )

fun <T1, T2> MutableLiveData<Resource<T1>>.combineLoading(liveData: MutableLiveData<Resource<T2>>):
        MediatorLiveData<Boolean> {
    val mediatorLiveData = MediatorLiveData<Boolean>()
    mediatorLiveData.addSource(this) {
        mediatorLiveData.value = this.value?.isFinished() == true && liveData.value?.isFinished() == true
    }
    mediatorLiveData.addSource(liveData) {
        mediatorLiveData.value = this.value?.isFinished() == true && liveData.value?.isFinished() == true
    }
    return mediatorLiveData
}
