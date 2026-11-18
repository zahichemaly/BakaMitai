package com.zc.bakamitai.common.ui.component.error.mapper

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorUIModelMapperTest {

    private fun expected(id: Int, isRetryable: Boolean) = ErrorUIModel(UiText.StringResource(id), isRetryable)

    @Test
    fun `network and timeout are retryable connection errors`() {
        assertEquals(expected(StringResource.error_network, true), DomainError.Network.toErrorUIModel())
        assertEquals(expected(StringResource.error_network, true), DomainError.Timeout.toErrorUIModel())
    }

    @Test
    fun `not found and unauthorized can't be retried`() {
        assertEquals(expected(StringResource.error_not_found, false), DomainError.NotFound.toErrorUIModel())
        assertEquals(expected(StringResource.error_unauthorized, false), DomainError.Unauthorized.toErrorUIModel())
    }

    @Test
    fun `rate limiting is retryable`() {
        assertEquals(expected(StringResource.error_rate_limited, true), DomainError.RateLimited.toErrorUIModel())
    }

    @Test
    fun `everything else is a generic retryable error`() {
        val generic = expected(StringResource.error_generic, true)
        assertEquals(generic, DomainError.Server.toErrorUIModel())
        assertEquals(generic, DomainError.InvalidData.toErrorUIModel())
        assertEquals(generic, DomainError.Unknown(IllegalStateException()).toErrorUIModel())
        assertEquals(generic, IllegalArgumentException().toErrorUIModel())
    }
}
