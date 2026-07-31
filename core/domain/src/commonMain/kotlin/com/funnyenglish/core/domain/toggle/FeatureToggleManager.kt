package com.funnyenglish.core.domain.toggle

import kotlinx.coroutines.flow.Flow

/**
 * Manager for feature toggles.
 */
interface FeatureToggleManager {
    /**
     * Check if feature is enabled.
     */
    fun isEnabled(feature: Feature): Boolean

    /**
     * Observe feature state as Flow.
     */
    fun observeFeature(feature: Feature): Flow<Boolean>

    /**
     * Enable/disable feature locally.
     */
    suspend fun setLocalOverride(feature: Feature, enabled: Boolean)

    /**
     * Refresh toggles from remote source.
     */
    suspend fun refreshRemoteToggles()

    /**
     * Get all feature states.
     */
    fun getAllFeatures(): Map<Feature, Boolean>

    /**
     * Get enabled features.
     */
    fun getEnabledFeatures(): List<Feature>
}
