package com.sotospeak.core.domain.repository

/**
 * Abstract token provider — implemented by the data layer (e.g. settings-based).
 */
interface TokenProvider {
    fun getToken(): String?
    fun setToken(token: String?)
}
