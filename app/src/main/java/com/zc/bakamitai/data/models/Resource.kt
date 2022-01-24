package com.zc.bakamitai.data.models


sealed class Resource<T>(val data: T? = null, val errorResponse: ErrorResponse? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(errorResponse: ErrorResponse) : Resource<T>(errorResponse = errorResponse)
    class Loading<T> : Resource<T>()

    fun isFinished(): Boolean = this !is Loading
}
