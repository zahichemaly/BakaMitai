package com.zc.bakamitai.feature.shows.presentation.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import com.zc.bakamitai.feature.shows.presentation.component.showrow.mapper.toShowRowUIModel
import com.zc.bakamitai.feature.shows.presentation.model.ShowSectionUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** One section model per letter, each counting its rows ("14 SHOWS"). */
fun ShowIndex.toSectionUIModels(): ImmutableList<ShowSectionUIModel> = sections.map { section ->
    ShowSectionUIModel(
        letter = section.toIndexLetterUIModel(),
        countLabel = UiText.StringPlural(
            PluralResource.all_shows_section_count,
            section.shows.size,
            listOf(section.shows.size),
        ),
        rows = section.shows.map { it.toShowRowUIModel() }.toImmutableList(),
    )
}.toImmutableList()

/** "Showing 1263 shows": every show listed. */
fun ShowIndex.toFooterLabel(): UiText =
    UiText.StringPlural(PluralResource.all_shows_footer_count, showCount, listOf(showCount))
