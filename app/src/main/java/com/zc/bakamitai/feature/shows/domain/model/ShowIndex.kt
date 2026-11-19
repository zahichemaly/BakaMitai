package com.zc.bakamitai.feature.shows.domain.model

/**
 * The show catalogue as an A–Z index: [ShowIndexSection.OTHER] first, then `A` … `Z`, each with at least one show.
 */
data class ShowIndex(val sections: List<ShowIndexSection>) {

    /** The number of shows in every section. */
    val showCount: Int get() = sections.sumOf { it.shows.size }
}
