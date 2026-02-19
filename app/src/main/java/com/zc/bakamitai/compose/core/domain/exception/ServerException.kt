package com.zc.bakamitai.compose.core.domain.exception

import com.zc.bakamitai.compose.common.UiText
import java.io.IOException

open class ServerException(val error: UiText) : IOException()
