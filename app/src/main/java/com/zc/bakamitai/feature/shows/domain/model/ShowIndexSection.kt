package com.zc.bakamitai.feature.shows.domain.model

import com.zc.bakamitai.common.domain.model.Show

/**
 * One letter of the catalogue and its shows, sorted.
 *
 * @property letter `'A'`..`'Z'`, or [OTHER] for titles that start with a digit, a symbol or a non-ASCII letter.
 * @property shows never empty: a letter without shows has no section.
 */
data class ShowIndexSection(val letter: Char, val shows: List<Show>) {

    companion object {
        /** The section of titles that don't start with `A`–`Z`, listed first. */
        const val OTHER = '#'
    }
}
