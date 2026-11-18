package com.zc.bakamitai.feature.home.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel

sealed interface HomeEffect {

    data class OpenDetails(val slug: String) : HomeEffect

    data object OpenSettings : HomeEffect

    /** A reload failed while content is shown: the content stays and the error is shown in a snackbar. */
    data class ShowError(val error: ErrorUIModel) : HomeEffect
}
