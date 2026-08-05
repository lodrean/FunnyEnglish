package com.sotospeak.core.domain.toggle

/**
 * All available features in the app.
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
