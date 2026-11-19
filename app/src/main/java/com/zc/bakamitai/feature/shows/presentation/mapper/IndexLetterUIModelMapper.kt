package com.zc.bakamitai.feature.shows.presentation.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.domain.model.ShowIndexSection
import com.zc.bakamitai.feature.shows.presentation.model.IndexLetterUIModel

/**
 * The section's letter: "A" … "Z" read as such, and "#" read "Numbers and symbols". Letters alternate their accent
 * (A primary, B secondary, …); "#" is secondary, since it comes before A.
 */
fun ShowIndexSection.toIndexLetterUIModel(): IndexLetterUIModel {
    val label = UiText.StringValue(letter.toString())
    return if (letter == ShowIndexSection.OTHER) {
        IndexLetterUIModel(
            letter = letter,
            label = label,
            description = UiText.StringResource(StringResource.cd_all_shows_other_section),
            isPrimaryAccent = false,
        )
    } else {
        IndexLetterUIModel(
            letter = letter,
            label = label,
            description = label,
            isPrimaryAccent = (letter - 'A') % 2 == 0,
        )
    }
}
