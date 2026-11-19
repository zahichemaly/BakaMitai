package com.zc.bakamitai.common.ui.component.timechip.model

/** How a time chip stands out. */
enum class TimeChipTone {
    /** A scheduled time: clock icon in primary, on-surface text. */
    DEFAULT,

    /** Later today: alarm icon and text in tertiary. */
    UPCOMING,

    /** No time to show ("NOT AIRING"): crossed-out calendar, on-surface-variant. */
    MUTED,
}
