package com.zc.bakamitai.common.domain.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

/**
 * Coroutine dispatchers, injected so tests can swap in a `TestDispatcher`. Nothing else references
 * `Dispatchers.*` (except `ObserveAsEvents`). Ktor and DataStore are main-safe already: switch only for
 * blocking or CPU-heavy work.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

@Single
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
