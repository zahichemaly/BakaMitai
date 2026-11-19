package com.zc.bakamitai.common.data.util

import com.zc.bakamitai.common.domain.model.DomainError
import java.io.IOException

/**
 * Runs a DataStore write and maps its failure (an `IOException`: a full disk, an unwritable file) to
 * [DomainError.Unknown], the DataStore twin of [safeApiCall]. Cancellation and any other exception propagate.
 */
suspend fun <T> safeDataStoreCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: IOException) {
    Result.failure(DomainError.Unknown(e))
}
