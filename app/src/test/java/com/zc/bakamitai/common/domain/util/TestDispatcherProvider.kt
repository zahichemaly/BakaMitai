package com.zc.bakamitai.common.domain.util

import kotlinx.coroutines.test.TestDispatcher

/** Runs every dispatcher on the test's [dispatcher], so `runTest` controls all of it. */
class TestDispatcherProvider(dispatcher: TestDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
}
