package com.zc.bakamitai.compose.core.domain.exceptionmanager

import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.compose.core.domain.StringResource
import com.zc.bakamitai.compose.core.domain.exception.ServerException
import java.net.UnknownHostException

class ExceptionManagerImpl : ExceptionManager {
    override fun transform(exception: Throwable): Throwable {
        return when (exception) {
            is UnknownHostException -> ServerException(UiText.StringResource(StringResource.error_message_no_internet))
            else -> ServerException(UiText.StringResource(StringResource.error_message_unknown))
        }
    }
}
