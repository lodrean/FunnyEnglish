package com.funnyenglish.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A domain-agnostic representation of text that can be localized.
 *
 * Use [UiText] in ViewModel states so that the domain/presentation layers stay
 * free of Android [String] resources. Resolve to a [String] inside composables
 * via [asString].
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResourceId(
        val id: StringResource,
        val args: Array<Any> = arrayOf()
    ) : UiText

    companion object {
        fun plain(value: String): UiText = DynamicString(value)
    }
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResourceId -> stringResource(id, *args)
    }
}

fun UiText.asString(locale: Locale): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResourceId -> {
            // Fallback — Compose Resources require @Composable context.
            // Callers should prefer [asString()] inside UI code.
            id.toString()
        }
    }
}
