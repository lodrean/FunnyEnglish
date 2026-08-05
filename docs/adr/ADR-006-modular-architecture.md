# ADR-006: Modular Architecture with Feature Toggles

## Status
Accepted

## Context
So to Speak has grown from a simple app to a complex platform with multiple features:
- Authentication (email, social, biometric)
- Test system (5 question types)
- Gamification (streaks, achievements, quests)
- Social (groups, future: friends, chat)
- Adaptive learning (ML-based)

The monolithic `composeApp` and `shared` modules have become:
- Difficult to maintain
- Hard to test in isolation
- Slow build times
- Risky deployments (all or nothing)
- No way to disable features dynamically

## Decision
Adopt a **modular architecture** with **Feature Toggle** system:

1. **Core Module** (`core/`)
   - Base infrastructure (DI, network, settings)
   - Feature toggle system
   - Shared utilities

2. **Feature API Module** (`feature-api/`)
   - Contracts between modules
   - Navigation framework
   - FeatureEntry interface

3. **Feature Modules** (`feature-*/`)
   - One module per major feature
   - Self-contained (UI + ViewModel + DI)
   - Optional dependency on other features
   - Can be toggled on/off at runtime

4. **App Module** (`app/`)
   - Assembles all features
   - Initializes DI
   - Registers navigation

## Consequences

### Positive
- **Incremental releases**: Deploy features independently
- **A/B testing**: Enable features for subset of users
- **Faster builds**: Build only changed modules
- **Better testing**: Test features in isolation
- **Team scaling**: Different teams own different modules
- **Rollback safety**: Disable broken features instantly
- **Code clarity**: Clear boundaries between features

### Negative
- **Initial complexity**: More modules to manage
- **Dependency management**: Need to track inter-feature deps
- **Navigation complexity**: Cross-feature navigation needs care
- **Build configuration**: More Gradle setup

### Neutral
- **Learning curve**: Team needs to understand modular patterns
- **Tooling**: IDE support for multi-module projects

## Implementation Plan

### Phase 1: Core Infrastructure (Week 1-2)
- Create `core` module
- Create `feature-api` module
- Implement Feature Toggle system
- Set up module dependencies

### Phase 2: Extract First Feature (Week 3)
- Create `feature-home`
- Migrate HomeScreen from composeApp
- Test feature toggle integration

### Phase 3: Extract Remaining Features (Week 4-6)
- `feature-auth`
- `feature-tests`
- `feature-groups` (already built)
- `feature-gamification`
- `feature-profile`

### Phase 4: Cleanup (Week 7)
- Remove legacy composeApp
- Update documentation
- CI/CD for multi-module builds

## Feature Toggle Strategy

### Toggle Levels
1. **Build-time**: Compile flags (for dev builds)
2. **Local**: User preferences (can override)
3. **Remote**: From backend (percentage rollout)
4. **Default**: Enum default value

### Priority
```
Local override > Remote toggle > Default value
```

### Use Cases
- **Beta testing**: Enable for beta users only
- **Gradual rollout**: 1% → 10% → 50% → 100%
- **Kill switch**: Instantly disable broken features
- **Premium features**: Enable for paid users only
- **A/B testing**: Different variants for different users

## Example: Adding New Feature

```kotlin
// 1. Add to Feature enum
enum class Feature {
    SMART_REMINDERS("smart_reminders", false, "AI reminders", true)
}

// 2. Create module
// feature-reminders/

// 3. Implement entry point
class RemindersFeatureEntry : FeatureEntry {
    override val feature = Feature.SMART_REMINDERS
    // ...
}

// 4. Register in app
val features = listOf(
    // ...
    RemindersFeatureEntry()
).filter { toggleManager.isEnabled(it.feature) }
```

## Related ADRs
- ADR-001: Technology Stack
- ADR-002: Kotlin Multiplatform
- ADR-003: Compose Multiplatform

## References
- [MODULAR_ARCHITECTURE.md](../MODULAR_ARCHITECTURE.md)
- [TEMPLATE_FEATURE_MODULE.md](../TEMPLATE_FEATURE_MODULE.md)
- [Feature Toggles (Martin Fowler)](https://martinfowler.com/articles/feature-toggles.html)
