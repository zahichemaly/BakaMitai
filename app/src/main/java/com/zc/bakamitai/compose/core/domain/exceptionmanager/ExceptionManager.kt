package com.zc.bakamitai.compose.core.domain.exceptionmanager

interface ExceptionManager {
    fun transform(exception: Throwable): Throwable
}
