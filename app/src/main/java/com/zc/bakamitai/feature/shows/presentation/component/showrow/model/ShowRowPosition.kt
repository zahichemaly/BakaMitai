package com.zc.bakamitai.feature.shows.presentation.component.showrow.model

/** Where a row sits in its section's card, which decides its slice of the card's shape and padding. */
enum class ShowRowPosition {
    FIRST,
    MIDDLE,
    LAST,
    SINGLE,
    ;

    /** Whether the row starts the card: top corners and padding, no divider above it. */
    val isFirst: Boolean get() = this == FIRST || this == SINGLE

    /** Whether the row ends the card: bottom corners and padding, then the gap to the next section. */
    val isLast: Boolean get() = this == LAST || this == SINGLE

    companion object {
        /** The position of the row at [index] in a section of [size] rows. */
        fun of(index: Int, size: Int): ShowRowPosition = when {
            size <= 1 -> SINGLE
            index == 0 -> FIRST
            index == size - 1 -> LAST
            else -> MIDDLE
        }
    }
}
