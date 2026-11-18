package com.zc.bakamitai.common.ui.component.error.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/** A failure ready to show: what went wrong and whether a retry can help. */
@Immutable
data class ErrorUIModel(val message: UiText, val isRetryable: Boolean)
