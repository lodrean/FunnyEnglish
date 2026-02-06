# Tasklist: KMP Performance Optimization

## Ticket
PERFORMANCE-001-KMP

## Status
🔄 READY FOR IMPLEMENTATION

## Overview
Оптимизация производительности Compose Multiplatform приложения.

## Acceptance Criteria
- [ ] App launch time < 2 seconds
- [ ] Smooth scrolling (60 FPS) в списках
- [ ] Images load efficiently with placeholders
- [ ] No unnecessary recompositions
- [ ] Reduced APK size

## Tasks

### 1. Image Loading Optimization
- [ ] **Task 1.1**: Optimize Coil3 configuration
  - AC: Disk cache size: 100MB
  - AC: Memory cache size: 50MB
  - AC: Placeholder and error drawables
  - AC: Crossfade animation
  
- [ ] **Task 1.2**: Add image preloading
  - AC: Preload images for next screen
  - AC: Priority loading for visible items
  
- [ ] **Task 1.3**: Optimize image sizes
  - AC: Resize images to target size
  - AC: WebP format где возможно
  - AC: Quality optimization (80%)

### 2. List Optimization
- [ ] **Task 2.1**: Optimize LazyColumn
  - AC: Add keys для items
  - AC: contentType для разных типов
  - AC: LazyListState optimization
  
- [ ] **Task 2.2**: Implement pagination
  - AC: Paginated loading для больших списков
  - AC: Loading indicators
  - AC: Error states

### 3. State Management Optimization
- [ ] **Task 3.1**: Review ViewModel state
  - AC: Использовать data class для State
  - AC: Immutable collections
  - AC: Proper state flow usage
  
- [ ] **Task 3.2**: Optimize recompositions
  - AC: derivedStateOf для expensive calculations
  - AC: rememberSaveable для config changes
  - AC: key() для списков
  - AC: Пройтись lint по compose

### 4. Startup Optimization
- [ ] **Task 4.1**: Optimize App initialization
  - AC: Lazy initialization для не-критичных сервисов
  - AC: Async loading
  - AC: Splash screen optimization
  
- [ ] **Task 4.2**: Optimize DI
  - AC: Koin modules lazy load
  - AC: Reduce dependency graph

### 5. Memory Optimization
- [ ] **Task 5.1**: Profile memory usage
  - AC: Android Studio profiler
  - AC: Memory leaks detection
  - AC: Large allocations identification
  
- [ ] **Task 5.2**: Fix memory issues
  - AC: Clear unused resources
  - AC: Optimize bitmaps
  - AC: Fix potential leaks

### 6. APK Size Optimization
- [ ] **Task 6.1**: Enable R8/ProGuard
  - AC: Minification enabled
  - AC: Shrinking enabled
  - AC: Obfuscation (optional)
  
- [ ] **Task 6.2**: Remove unused resources
  - AC: Lint check
  - AC: Remove unused drawables
  - AC: Optimize assets

## Implementation Details

### Coil3 Configuration
```kotlin
// AppModule.kt
single<ImageLoader> {
    ImageLoader.Builder(context)
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizeBytes(100 * 1024 * 1024) // 100MB
                .build()
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25) // 25% of memory
                .build()
        }
        .crossfade(true)
        .build()
}
```

### LazyColumn Optimization
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = rememberLazyListState()
) {
    items(
        items = categories,
        key = { it.id },
        contentType = { "category" }
    ) { category ->
        CategoryCard(
            category = category,
            modifier = Modifier.animateItemPlacement()
        )
    }
}
```

### ViewModel State
```kotlin
// Optimized state
data class HomeState(
    val isLoading: Boolean = false,
    val categories: ImmutableList<Category> = persistentListOf(),
    val userProfile: UserProfile? = null,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()
    
    // derived state
    val hasContent = derivedStateOf { 
        _state.value.categories.isNotEmpty() 
    }
}
```

## Performance Metrics

| Metric | Before | Target | After |
|--------|--------|--------|-------|
| Cold Start | TBD | <2s | TBD |
| Warm Start | TBD | <1s | TBD |
| List Scroll | TBD | 60 FPS | TBD |
| Image Load | TBD | <500ms | TBD |
| APK Size | TBD | -20% | TBD |

## Testing

- [ ] Profile before/after
- [ ] Benchmark tests
- [ ] Memory leak detection
- [ ] Startup time measurement

## Related

- Compose Performance: https://developer.android.com/jetpack/compose/performance
- Coil Documentation: https://coil-kt.github.io/coil/
- Parent Tasklist: `docs/tasklist/IMPROVEMENTS-2025-001.md`
