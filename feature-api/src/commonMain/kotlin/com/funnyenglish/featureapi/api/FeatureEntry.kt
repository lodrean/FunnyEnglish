package com.funnyenglish.featureapi.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funnyenglish.core.domain.toggle.Feature

/**
 * Entry point for a feature module
 */
interface FeatureEntry {
    /**
     * Feature identifier
     */
    val feature: Feature
    
    /**
     * Order in navigation (lower = first)
     */
    val navigationOrder: Int
    
    /**
     * Label for bottom navigation
     */
    val navigationLabel: String
    
    /**
     * Main content of the feature
     */
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        onNavigate: (Any) -> Unit
    )
    
    /**
     * Navigation icon composable - use as function not property to avoid Compose compiler issues
     */
    @Composable
    fun NavigationIcon()
}

/**
 * Registry of all feature entries
 */
interface FeatureRegistry {
    fun register(entry: FeatureEntry)
    fun getEnabledFeatures(): List<FeatureEntry>
    fun getNavigationFeatures(): List<FeatureEntry>
}
