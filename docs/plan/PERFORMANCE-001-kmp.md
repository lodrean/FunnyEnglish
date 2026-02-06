# Plan: KMP Performance Optimization

## Ticket
PERFORMANCE-001-KMP

## Status
DRAFT → APPROVED

## Overview
Optimization of Compose Multiplatform app performance.

## Approach

1. Optimize LazyColumn with keys and contentType
2. Configure Coil3 with proper caching
3. Add derivedStateOf for expensive calculations
4. Enable build optimizations

## Implementation Steps

### Step 1: LazyColumn Optimization (30 min)
- Add keys to items
- Add contentType
- Test scrolling performance

### Step 2: Coil3 Configuration (30 min)
- Configure disk cache (100MB)
- Configure memory cache
- Add placeholder and error handling

### Step 3: State Optimization (30 min)
- Find expensive calculations
- Add derivedStateOf
- Review ViewModels

### Step 4: Build Optimization (20 min)
- Enable R8/ProGuard rules
- Optimize resources

## Files to Modify

```
composeApp/
├── src/commonMain/kotlin/
│   ├── screens/HomeScreen.kt
│   ├── screens/CategoryTestsScreen.kt
│   └── di/AppModule.kt
└── build.gradle.kts
```

## Success Criteria

- [ ] LazyColumn has keys and contentType
- [ ] Coil3 configured with caches
- [ ] No obvious performance issues
- [ ] Code compiles

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking changes | Low | Test after changes |
| Memory increase | Low | Monitor heap usage |

## Dependencies
- Coil3 (already present)
