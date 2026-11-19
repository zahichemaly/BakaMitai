package com.zc.bakamitai.common.ui.text

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalTime

/**
 * Every user-visible text in `presentation`, and the only code that resolves string resources
 * (conventions skill §2.5). Resolve it as late as possible: [asString] in composition, `asString(context)`
 * outside it (snackbars from a Route effect, share sheets).
 *
 * Format arguments may themselves be [UiText]; they are resolved before formatting. Times are [TimeValue]s: they
 * resolve on the user's clock format ([LocalUse24HourClock] in composition, a parameter outside it).
 */
@Immutable
sealed interface UiText {

    /** Text that is already final: API data, user input. */
    @Immutable
    data class StringValue(val value: String) : UiText

    /** A string resource: `UiText.StringResource(StringResource.x, listOf(arg))`. */
    @Immutable
    data class StringResource(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    /** A plurals resource picked by [quantity]; [args] fill its placeholders (usually `listOf(quantity)`). */
    @Immutable
    data class StringPlural(@param:PluralsRes val id: Int, val quantity: Int, val args: List<Any> = emptyList()) :
        UiText

    /**
     * A time of day, formatted when it's resolved: "9:30 PM" or "21:30", following the *Time format* setting. Times
     * are never preformatted strings, so every shown time changes as soon as the setting does.
     */
    @Immutable
    data class TimeValue(val time: LocalTime) : UiText

    @Composable
    fun asString(): String = when (this) {
        is StringValue -> value

        is StringResource -> {
            val formatArgs = args.map { if (it is UiText) it.asString() else it }
            if (formatArgs.isEmpty()) {
                androidx.compose.ui.res.stringResource(id)
            } else {
                androidx.compose.ui.res.stringResource(id, *formatArgs.toTypedArray())
            }
        }

        is StringPlural -> {
            val formatArgs = args.map { if (it is UiText) it.asString() else it }
            androidx.compose.ui.res.pluralStringResource(id, quantity, *formatArgs.toTypedArray())
        }

        is TimeValue -> time.toClockText(LocalUse24HourClock.current)
    }

    /**
     * Resolves this text outside composition.
     *
     * @param use24HourClock the clock for [TimeValue]s; a Route passes `LocalUse24HourClock.current`.
     */
    fun asString(context: Context, use24HourClock: Boolean = false): String = when (this) {
        is StringValue -> value

        is StringResource -> {
            val formatArgs = args.map { if (it is UiText) it.asString(context, use24HourClock) else it }
            if (formatArgs.isEmpty()) {
                context.getString(id)
            } else {
                context.getString(id, *formatArgs.toTypedArray())
            }
        }

        is StringPlural -> {
            val formatArgs = args.map { if (it is UiText) it.asString(context, use24HourClock) else it }
            context.resources.getQuantityString(id, quantity, *formatArgs.toTypedArray())
        }

        is TimeValue -> time.toClockText(use24HourClock)
    }
}
