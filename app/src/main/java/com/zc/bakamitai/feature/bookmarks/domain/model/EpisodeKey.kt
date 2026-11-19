package com.zc.bakamitai.feature.bookmarks.domain.model

/** "07", "12.5". */
private val NumberRegex = Regex("""^\d+(\.\d+)?$""")

/** A revision suffix after a number: the "v2" of "1216v2". */
private val RevisionSuffixRegex = Regex("""(?<=\d)v\d+$""", RegexOption.IGNORE_CASE)

/** A batch range, "01-24" or "01 - 28". */
private val BatchRangeRegex = Regex("""^\d+\s*-\s*\d+$""")

/**
 * An episode label without its revision, so a re-release ("1216v2") is the same episode as the original ("1216").
 * Specials ("Egghead SP11") and "Movie" are kept as they are, without a [number].
 */
@JvmInline
value class EpisodeKey(val value: String) {

    /** The episode number ("07" → 7.0, "12.5" → 12.5), or `null` for a special, a movie or anything else. */
    val number: Double? get() = value.takeIf { NumberRegex.matches(it) }?.toDouble()

    companion object {
        /** The key of a label as SubsPlease writes it: trimmed, without a trailing revision. */
        fun of(label: String): EpisodeKey = EpisodeKey(label.trim().replace(RevisionSuffixRegex, ""))
    }
}

/** Whether this episode label is a batch range ("01-24"): a batch repeats episodes and never notifies. */
fun String.isBatchRange(): Boolean = BatchRangeRegex.matches(trim())
