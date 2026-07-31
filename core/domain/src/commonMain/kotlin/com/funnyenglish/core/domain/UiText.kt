package com.funnyenglish.core.domain

/**
 * Wrapper for strings that originate from — or could originate from — a string resource.
 * Used for error messages and other localizable text in the presentation layer.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(val id: String, val args: List<Any> = emptyList()) : UiText
}
