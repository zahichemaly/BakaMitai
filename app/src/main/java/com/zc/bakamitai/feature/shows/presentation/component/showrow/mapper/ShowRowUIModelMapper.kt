package com.zc.bakamitai.feature.shows.presentation.component.showrow.mapper

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowUIModel

/** A catalogue row; the key pairs the slug with the title, since a slug can be listed under several titles. */
fun Show.toShowRowUIModel(): ShowRowUIModel = ShowRowUIModel(
    key = "${slug.value}|$title",
    slug = slug.value,
    title = UiText.StringValue(title),
)
