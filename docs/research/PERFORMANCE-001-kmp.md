# Research: KMP Performance Optimization

## Ticket
PERFORMANCE-001-KMP

## Objective
Исследование и оптимизация производительности Compose Multiplatform приложения.

## Research Findings

### Performance Metrics to Improve

| Metric | Target | Current |
|--------|--------|---------|
| App launch time | < 2s | TBD |
| List scroll | 60 FPS | TBD |
| Image load | < 500ms | TBD |
| APK size | -20% | TBD |

### KMP Best Practices

#### 1. LazyColumn Optimization
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize()
) {
    items(
        items = data,
        key = { it.id },           // Stable keys
        contentType = { it.type }  // Content type for recycling
    ) { item ->
        ListItem(item)
    }
}
```

#### 2. Image Loading (Coil3)
- Disk cache (100MB)
- Memory cache (25% of memory)
- Placeholder and error states
- Resize to target size

#### 3. State Management
- Use `derivedStateOf` for expensive calculations
- Use `rememberSaveable` for config changes
- Minimize recomposition scope

#### 4. Startup Optimization
- Lazy initialization
- Async loading
- Splash screen

### Current Issues Identified

1. **No keys in LazyColumn** - causes unnecessary recompositions
2. **No contentType** - suboptimal recycling
3. **Coil3 not optimized** - default configuration
4. **No derivedStateOf** - potential expensive calculations

## Implementation Strategy

1. **LazyColumn optimization** - Add keys and contentType
2. **Coil3 configuration** - Optimize cache settings
3. **State optimization** - Add derivedStateOf where needed
4. **Build optimization** - Enable R8/ProGuard

## Affected Areas

```
composeApp/src/commonMain/kotlin/
├── screens/
│   ├── HomeScreen.kt           # LazyColumn optimization
│   └── CategoryTestsScreen.kt  # List optimization
├── components/
│   └── ImageLoading.kt         # Coil3 config
├── di/
│   └── AppModule.kt            # Coil3 setup
└── viewmodel/
    └── *ViewModel.kt           # State optimization
```

## Recommendation

Start with LazyColumn optimization (high impact, low effort), then Coil3 configuration.
