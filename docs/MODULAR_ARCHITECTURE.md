# Многомодульная архитектура KMP с Feature Toggles

## Обзор

So to Speak использует многомодульную архитектуру на базе Kotlin Multiplatform с поддержкой **Feature Toggles** - системы динамического включения/выключения функциональности.

## Структура модулей

```
So to Speak/
├── core/                   # Базовая инфраструктура
│   ├── toggle/            # Feature toggle система
│   ├── network/           # HTTP клиенты
│   ├── settings/          # Настройки
│   └── di/                # Core DI module
│
├── feature-api/           # API для feature модулей
│   ├── navigation/        # Навигация между модулями
│   ├── api/               # Интерфейсы feature entry
│   └── di/                # Feature API DI
│
├── feature-home/          # Feature: Главный экран
├── feature-auth/          # Feature: Авторизация
├── feature-tests/         # Feature: Тесты
├── feature-groups/        # Feature: Группы (классы)
├── feature-gamification/  # Feature: Геймификация
├── feature-profile/       # Feature: Профиль
│
├── app/                   # Сборочный модуль
│   └── App.kt             # Точка входа + регистрация feature
│
├── backend/               # Spring Boot backend
└── shared/                # Legacy shared module (deprecated)
```

## Feature Toggle Система

### Enum Feature

```kotlin
enum class Feature(
    val key: String,
    val defaultValue: Boolean,
    val description: String,
    val requiresRestart: Boolean = false
) {
    // Auth
    BIOMETRIC_AUTH("auth.biometric", true, "Biometric authentication"),
    SOCIAL_LOGIN("auth.social", true, "Social login"),
    
    // Learning
    ADAPTIVE_LESSONS("learning.adaptive", false, "Adaptive learning", true),
    MICRO_LESSONS("learning.micro", true, "Micro-lessons"),
    
    // Gamification
    STREAKS("gamification.streaks", true, "Daily streaks"),
    ACHIEVEMENTS("gamification.achievements", true, "Achievements"),
    DAILY_QUESTS("gamification.daily_quests", false, "Daily quests", true),
    
    // Social
    GROUPS("social.groups", true, "Student groups"),
    FRIENDS("social.friends", false, "Friends", true),
    CHAT("social.chat", false, "Chat", true),
    
    // Content
    VIDEO_LESSONS("content.video", false, "Video lessons", true),
    AUDIO_LESSONS("content.audio", false, "Audio lessons", true)
}
```

### Использование в коде

```kotlin
// Проверка feature
val toggleManager: FeatureToggleManager = get()

if (toggleManager.isEnabled(Feature.GROUPS)) {
    // Show groups UI
}

// Reactive observation
toggleManager.observeFeature(Feature.DAILY_QUESTS)
    .collect { isEnabled ->
        // Update UI
    }

// Local override (for testing)
viewModelScope.launch {
    toggleManager.setLocalOverride(Feature.ADAPTIVE_LESSONS, true)
}
```

### Условное отображение UI

```kotlin
@Composable
fun HomeScreen() {
    val toggleManager = koinInject<FeatureToggleManager>()
    
    Column {
        // Always visible
        WelcomeSection()
        
        // Conditional features
        if (toggleManager.isEnabled(Feature.STREAKS)) {
            StreakWidget()
        }
        
        if (toggleManager.isEnabled(Feature.DAILY_QUESTS)) {
            DailyQuestsPanel()
        }
        
        if (toggleManager.isEnabled(Feature.GROUPS)) {
            GroupsPreview()
        }
    }
}
```

## Создание нового Feature модуля

### 1. Создать модуль

```kotlin
// settings.gradle.kts
include(":feature-mynew")
```

```kotlin
// feature-mynew/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    // ... targets
    
    sourceSets {
        commonMain.dependencies {
            api(project(":feature-api"))
            // other deps
        }
    }
}
```

### 2. Добавить Feature в enum

```kotlin
// core/src/.../toggle/Feature.kt
enum class Feature(...) {
    // ... existing
    MY_NEW_FEATURE("mynew.key", false, "Description", true)
}
```

### 3. Создать Feature Entry

```kotlin
// feature-mynew/src/.../MyNewFeatureEntry.kt
class MyNewFeatureEntry : FeatureEntry {
    override val feature = Feature.MY_NEW_FEATURE
    override val navigationOrder = 5
    override val navigationLabel = "Новая фича"
    
    @Composable
    override fun Content(modifier: Modifier, onNavigate: (Any) -> Unit) {
        MyNewScreen()
    }
}
```

### 4. Регистрация в App

```kotlin
// app/src/.../App.kt
val features = listOf(
    HomeFeatureEntry(),
    GroupsFeatureEntry(),
    MyNewFeatureEntry() // Add here
).filter { 
    toggleManager.isEnabled(it.feature) 
}
```

## Feature Toggle API (Backend)

### Endpoint для получения toggles

```kotlin
@RestController
@RequestMapping("/api/features")
class FeatureToggleController {
    
    @GetMapping("/toggles")
    fun getFeatureToggles(
        @AuthenticationPrincipal user: UserPrincipal
    ): Map<String, Boolean> {
        return mapOf(
            "learning.adaptive" to true,
            "gamification.daily_quests" to isBetaUser(user),
            "social.friends" to isInTestGroup(user, "friends_beta")
        )
    }
}
```

### A/B Testing через toggles

```kotlin
// Assign user to test group
fun assignTestGroup(userId: String): String {
    val hash = userId.hashCode() % 100
    return when {
        hash < 50 -> "control"    // 50% - control
        else -> "treatment"        // 50% - new feature
    }
}

// Use in toggle decision
fun isFeatureEnabled(feature: Feature, userId: String): Boolean {
    return when (feature) {
        Feature.ADAPTIVE_LESSONS -> {
            assignTestGroup(userId) == "treatment"
        }
        else -> feature.defaultValue
    }
}
```

## Интеграция в AIDD Pipeline

### Gate 2: Research - Feature Toggle Assessment

```markdown
## Feature Toggle Checklist

- [ ] Feature added to `Feature` enum
- [ ] Default value defined
- [ ] `requiresRestart` flag set correctly
- [ ] Remote toggle endpoint updated (if needed)
- [ ] A/B test plan (if applicable)
```

### Gate 3: Plan - Module Structure

```markdown
## Module Plan

### New Modules
- `feature-[name]` - Main feature module

### Modified Modules
- `core` - Add Feature enum
- `feature-api` - Add navigation routes (if needed)
- `app` - Register feature

### Feature Toggle Strategy
- Default: disabled/enabled
- Rollout: percentage-based
- Target: beta users/all users
```

### Gate 5: Tasklist - Feature Toggle Tasks

```markdown
## Feature Toggle Tasks

- [ ] Add Feature to enum
- [ ] Create feature module
- [ ] Implement FeatureEntry
- [ ] Add conditional UI in other modules
- [ ] Backend toggle endpoint (if needed)
- [ ] QA: Test with feature on/off
- [ ] QA: Test rollout scenarios
```

## Best Practices

### 1. Feature Granularity

```kotlin
// ✅ Good: Small, focused features
enum class Feature {
    DAILY_QUESTS,      // Single feature
    STREAK_FREEZE,     // Sub-feature
    STREAK_RECOVERY    // Sub-feature
}

// ❌ Bad: Monolithic features
enum class Feature {
    GAMIFICATION,      // Too broad
    SOCIAL             // Too broad
}
```

### 2. Default Values

```kotlin
// New features: disabled by default
NEW_FEATURE("key", false, "...", true)

// Stable features: enabled by default
STABLE_FEATURE("key", true, "...")
```

### 3. Testing

```kotlin
@Test
fun testFeatureToggle() {
    // Test with feature disabled
    toggleManager.setLocalOverride(Feature.NEW_FEATURE, false)
    composeTestRule.onNodeWithText("New Feature").assertDoesNotExist()
    
    // Test with feature enabled
    toggleManager.setLocalOverride(Feature.NEW_FEATURE, true)
    composeTestRule.onNodeWithText("New Feature").assertExists()
}
```

### 4. Migration Path

```kotlin
// Phase 1: Feature toggle (default: false)
FEATURE_V2("v2", false, "New version", true)

// Phase 2: Rollout to 10%
// Remote toggle: 10% users get true

// Phase 3: Full rollout (default: true)
FEATURE_V2("v2", true, "New version", true)

// Phase 4: Remove toggle
// Delete from enum, always use v2
```

## Мониторинг

### Analytics

```kotlin
class FeatureAnalytics(
    private val analytics: Analytics
) {
    fun trackFeatureEnabled(feature: Feature) {
        analytics.track("feature_enabled", mapOf(
            "feature_key" to feature.key,
            "feature_name" to feature.name
        ))
    }
    
    fun trackFeatureUsed(feature: Feature, action: String) {
        analytics.track("feature_used", mapOf(
            "feature_key" to feature.key,
            "action" to action
        ))
    }
}
```

### Dashboard

```kotlin
@Composable
fun FeatureToggleDashboard() {
    val toggleManager = koinInject<FeatureToggleManager>()
    val features = toggleManager.getAllFeatures()
    
    LazyColumn {
        items(features.toList()) { (feature, enabled) ->
            FeatureToggleItem(
                feature = feature,
                enabled = enabled,
                onToggle = { 
                    toggleManager.setLocalOverride(feature, it)
                }
            )
        }
    }
}
```

---

## Чеклист внедрения новой фичи

1. [ ] Добавить Feature в enum
2. [ ] Создать feature-модуль
3. [ ] Реализовать FeatureEntry
4. [ ] Добавить условное отображение в UI
5. [ ] Обновить backend toggle endpoint
6. [ ] Написать тесты с feature on/off
7. [ ] Добавить аналитику
8. [ ] Документировать в CHANGELOG
