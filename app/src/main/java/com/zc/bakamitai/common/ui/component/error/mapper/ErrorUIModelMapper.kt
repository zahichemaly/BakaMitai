package com.zc.bakamitai.common.ui.component.error.mapper

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText

/** Maps a failure from a use case (normally a [DomainError]) to the message and retry option shown. */
fun Throwable.toErrorUIModel(): ErrorUIModel = when (this) {
    DomainError.Network,
    DomainError.Timeout,
    -> ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)

    DomainError.NotFound -> ErrorUIModel(UiText.StringResource(StringResource.error_not_found), isRetryable = false)

    DomainError.RateLimited ->
        ErrorUIModel(UiText.StringResource(StringResource.error_rate_limited), isRetryable = true)

    DomainError.Unauthorized ->
        ErrorUIModel(UiText.StringResource(StringResource.error_unauthorized), isRetryable = false)

    else -> ErrorUIModel(UiText.StringResource(StringResource.error_generic), isRetryable = true)
}
