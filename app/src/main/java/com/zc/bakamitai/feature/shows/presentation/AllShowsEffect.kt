package com.zc.bakamitai.feature.shows.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel

sealed interface AllShowsEffect {

    data class OpenDetails(val slug: String) : AllShowsEffect

    data object OpenSettings : AllShowsEffect

    /** A refresh failed while the list is shown: the list stays and the error is shown in a snackbar. */
    data class ShowError(val error: ErrorUIModel) : AllShowsEffect
}
