package com.zc.bakamitai.compose.common

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.util.fastForEach

@Immutable
sealed class UiText {
    class StringValue(val text: String) : UiText()

    data class Annotated(val text: AnnotatedString) : UiText()

    class StringResource(
        @field:StringRes val resourceId: Int,
        vararg val args: Any
    ) : UiText()

    class StringPlural(
        @field:PluralsRes val resourceId: Int,
        val countId: Int,
        vararg val args: Any
    ) :
        UiText()

    class TextBuilder(val values: List<Any>) : UiText()

    class TextFunctionWrapper(val function: (List<Any>) -> String, val params: List<Any>) : UiText()

    class NestedStringResource(
        @param:StringRes val resourceId: Int,
        vararg val args: UiText
    ) : UiText()

    @VisibleForTesting
    fun asStringTesting(): String {
        return when (this) {
            is StringPlural -> "$resourceId"
            is StringResource -> "$resourceId"
            is StringValue -> text
            is TextBuilder -> values.toString()
            is TextFunctionWrapper -> function.toString()
            is NestedStringResource -> "$resourceId"
            is Annotated -> text.text
        }
    }

    @Composable
    fun asString(): String {
        return when (this) {
            is StringValue -> text
            is StringResource ->
                if (args.isEmpty()) {
                    stringResource(resourceId)
                } else {
                    stringResource(id = resourceId, formatArgs = args)
                }

            is StringPlural ->
                pluralStringResource(
                    id = resourceId,
                    count = countId,
                    formatArgs = args
                )

            is TextBuilder ->
                StringBuilder().apply {
                    values.fastForEach {
                        when (it) {
                            is UiText -> append(it.asString())
                            else -> append(it)
                        }
                    }
                }.toString()

            is TextFunctionWrapper -> {
                val resolvedParams =
                    params.map { param ->
                        when (param) {
                            is UiText -> param.asString()
                            else -> param
                        }
                    }
                return function(resolvedParams)
            }

            is NestedStringResource ->
                stringResource(
                    id = resourceId,
                    formatArgs = args.map { it.asString() }.toTypedArray()
                )

            is Annotated -> text.text
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is StringValue -> text
            is StringResource -> context.getString(resourceId, args)
            else -> ""
        }
    }

    fun asAnnotatedString(): AnnotatedString {
        return when (this) {
            is Annotated -> text
            else -> AnnotatedString("")
        }
    }
}

