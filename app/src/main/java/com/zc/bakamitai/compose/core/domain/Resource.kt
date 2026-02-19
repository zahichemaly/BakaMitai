package com.zc.bakamitai.compose.core.domain


sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>

    data class Failure(val message: String, var code: Int = 500) : Resource<Nothing>
}
