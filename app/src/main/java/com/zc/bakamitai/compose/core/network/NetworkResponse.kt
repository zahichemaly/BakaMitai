package com.zc.bakamitai.compose.core.network

import com.zc.bakamitai.compose.core.domain.Resource

sealed interface NetworkResponse<out T, out E> {
    data class Success<T>(val data: T) : NetworkResponse<T, Nothing>

    data class Failure<E>(val error: E?) : NetworkResponse<Nothing, E>

    companion object {
        val successCodeRange: IntRange = 200..299
    }
}

data class ErrorResponse(
    val code: Int = -1,
    val message: String? = null
)


typealias GenericResponse<T> = NetworkResponse<T, ErrorResponse>

inline fun <reified DTO, Domain> GenericResponse<DTO>.toResource(crossinline mapper: (DTO) -> Domain): Resource<Domain> {
    when(this) {
        is NetworkResponse.Failure<ErrorResponse> -> {
            val code = this.error?.code ?: 500
            val message = this.error?.message ?: "Something went wrong"
            return Resource.Failure(message, code)

        }
        is NetworkResponse.Success<*> -> {
            val dto = this.data as DTO
            val domain = mapper(dto)
            return Resource.Success(domain)
        }
    }
}
