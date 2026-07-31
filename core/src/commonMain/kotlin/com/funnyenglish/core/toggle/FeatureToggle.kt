package com.funnyenglish.core.toggle

import com.funnyenglish.core.settings.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Feature Toggle System for FunnyEnglish
 */

/**
 * All available features in the app
 */
enum class Feature(
    val key: String,
    val defaultValue: Boolean,
    val description: String,
    val requiresRestart: Boolean = false
) {
    // Auth features
    BIOMETRIC_AUTH("auth.biometric", true, "Biometric authentication"),
    SOCIAL_LOGIN("auth.social", true, "Social login (Google, VK, etc.)"),
    
    // Learning features
    ADAPTIVE_LESSONS("learning.adaptive", false, "Adaptive learning algorithm", true),
    SPACED_REPETITION("learning.spaced_repetition", false, "Spaced repetition for words", true),
    MICRO_LESSONS("learning.micro", true, "5-7 minute micro-lessons"),
    PRONUNCIATION("learning.pronunciation", false, "Pronunciation practice"),
    
    // Gamification features
    STREAKS("gamification.streaks", true, "Daily streaks"),
    ACHIEVEMENTS("gamification.achievements", true, "Achievement system"),
    DAILY_QUESTS("gamification.daily_quests", false, "Daily quests", true),
    LEADERBOARD("gamification.leaderboard", true, "Global leaderboard"),
    LEVELS("gamification.levels", true, "Level progression"),
    
    // Social features
    GROUPS("social.groups", true, "Student groups/classes"),
    FRIENDS("social.friends", false, "Friends system", true),
    CHAT("social.chat", false, "In-app chat", true),
    CHALLENGES("social.challenges", false, "Competitive challenges", true),
    
    // UI features
    DARK_MODE("ui.dark_mode", true, "Dark theme support"),
    ANIMATIONS("ui.animations", true, "Animations and micro-interactions"),
    HAPTICS("ui.haptics", true, "Haptic feedback"),
    ACCESSIBILITY("ui.accessibility", true, "Accessibility features"),
    
    // Content features
    VIDEO_LESSONS("content.video", false, "Video lessons", true),
    AUDIO_LESSONS("content.audio", false, "Audio lessons", true),
    STORIES("content.stories", false, "Interactive stories", true),
    
    // Admin/Debug features
    DEBUG_MENU("admin.debug_menu", false, "Debug menu (dev only)"),
    BETA_FEATURES("admin.beta", false, "Beta features (experimental)");
}

/**
 * Manager for feature toggles
 */
interface FeatureToggleManager {
    /**
     * Check if feature is enabled
     */
    fun isEnabled(feature: Feature): Boolean
    
    /**
     * Observe feature state as Flow
     */
    fun observeFeature(feature: Feature): Flow<Boolean>
    
    /**
     * Enable/disable feature locally
     */
    suspend fun setLocalOverride(feature: Feature, enabled: Boolean)
    
    /**
     * Refresh toggles from remote source
     */
    suspend fun refreshRemoteToggles()
    
    /**
     * Get all feature states
     */
    fun getAllFeatures(): Map<Feature, Boolean>
    
    /**
     * Get enabled features
     */
    fun getEnabledFeatures(): List<Feature>
}

/**
 * Implementation of FeatureToggleManager
 */
class FeatureToggleManagerImpl(
    private val localSource: LocalFeatureToggleSource,
    private val remoteSource: RemoteFeatureToggleSource
) : FeatureToggleManager {
    
    private val _remoteToggles = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val remoteToggles: StateFlow<Map<String, Boolean>> = _remoteToggles.asStateFlow()
    
    init {
        // Load local overrides
        localSource.loadOverrides()
    }
    
    override fun isEnabled(feature: Feature): Boolean {
        // Priority: Local override > Remote toggle > Default value
        val localOverride = localSource.getOverride(feature.key)
        if (localOverride != null) return localOverride
        
        val remoteValue = remoteToggles.value[feature.key]
        if (remoteValue != null) return remoteValue
        
        return feature.defaultValue
    }
    
    override fun observeFeature(feature: Feature): Flow<Boolean> {
        return remoteToggles.map { _ ->
            isEnabled(feature)
        }
    }
    
    override suspend fun setLocalOverride(feature: Feature, enabled: Boolean) {
        localSource.setOverride(feature.key, enabled)
    }
    
    override suspend fun refreshRemoteToggles() {
        try {
            val toggles = remoteSource.fetchToggles()
            _remoteToggles.value = toggles
        } catch (e: Exception) {
            // Keep existing remote toggles on error
        }
    }
    
    override fun getAllFeatures(): Map<Feature, Boolean> {
        return Feature.entries.associateWith { isEnabled(it) }
    }
    
    override fun getEnabledFeatures(): List<Feature> {
        return Feature.entries.filter { isEnabled(it) }
    }
}

/**
 * Local storage for feature toggle overrides
 * Simplified version - stores only in memory for now
 */
class LocalFeatureToggleSource {
    private val overrides = mutableMapOf<String, Boolean?>()
    
    fun loadOverrides() {
        // In-memory only for now
        // Platform-specific persistence can be added later
    }
    
    fun getOverride(key: String): Boolean? = overrides[key]
    
    fun setOverride(key: String, enabled: Boolean?) {
        if (enabled != null) {
            overrides[key] = enabled
        } else {
            overrides.remove(key)
        }
    }
}

/**
 * Remote source for feature toggles (from backend)
 */
class RemoteFeatureToggleSource(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun fetchToggles(): Map<String, Boolean> {
        return try {
            val response = httpClient.get("$baseUrl/api/features/toggles")
            response.body<Map<String, Boolean>>()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
