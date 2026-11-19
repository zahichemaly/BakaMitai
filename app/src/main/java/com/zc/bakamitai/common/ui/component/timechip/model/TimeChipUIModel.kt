package com.zc.bakamitai.common.ui.component.timechip.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/** A small time pill on a poster: "9:30 PM", "TODAY 9:30 PM", "NOT AIRING". */
@Immutable
data class TimeChipUIModel(val label: UiText, val tone: TimeChipTone)
