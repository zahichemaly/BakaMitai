package com.zc.bakamitai.common.data.util

import com.zc.bakamitai.common.domain.model.DomainError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class SafeDataStoreCallTest {

    @Test
    fun `success is wrapped`() = runTest {
        assertEquals(Result.success(1), safeDataStoreCall { 1 })
    }

    @Test
    fun `IOException maps to Unknown with the cause`() = runTest {
        val failure = IOException("disk full")

        val error = safeDataStoreCall { throw failure }.exceptionOrNull()

        assertIs<DomainError.Unknown>(error)
        assertSame(failure, error.cause)
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        assertFailsWith<CancellationException> { safeDataStoreCall { throw CancellationException("cancelled") } }
    }

    @Test
    fun `other exceptions propagate`() = runTest {
        assertFailsWith<IllegalStateException> { safeDataStoreCall { error("bug") } }
    }
}
