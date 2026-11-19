package com.zc.bakamitai.feature.details.presentation.mapper

import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.model.ShowOverview
import com.zc.bakamitai.feature.details.presentation.model.DetailsHeaderUIModel

fun ShowOverview.toHeaderUIModel(): DetailsHeaderUIModel = DetailsHeaderUIModel(
    title = UiText.StringValue(show.title),
    coverUrl = show.coverUrl,
    synopsis = synopsis?.let { UiText.StringValue(it) },
)
