package com.zc.bakamitai.feature.schedule.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel

sealed interface ScheduleEffect {

    data class OpenDetails(val slug: String) : ScheduleEffect

    data object OpenSettings : ScheduleEffect

    /** A reload failed while content is shown: the content stays and the error is shown in a snackbar. */
    data class ShowError(val error: ErrorUIModel) : ScheduleEffect
}
