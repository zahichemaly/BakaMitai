package com.zc.bakamitai.feature.schedule.presentation.component.dayheader.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter

/** A status filter chip with its count, "Aired (5)". */
@Immutable
data class StatusFilterChipUIModel(val filter: StatusFilter, val label: UiText)
