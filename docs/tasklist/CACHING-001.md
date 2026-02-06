# Tasklist: Caching Layer Implementation

## Ticket
CACHING-001

## Status
🔄 READY FOR IMPLEMENTATION

## Overview
Внедрение кэширования для read-heavy данных (categories, tests, user profiles).

## Acceptance Criteria
- [ ] Categories кэшируются на 1 час
- [ ] Tests list кэшируется на 30 минут
- [ ] User profile кэшируется на 5 минут
- [ ] Cache invalidation работает при обновлениях
- [ ] Cache metrics доступны
- [ ] Cache hit rate > 50%

## Tasks

### 1. Setup Caching Infrastructure
- [ ] **Task 1.1**: Add Caffeine dependency
  - AC: `com.github.ben-manes.caffeine:caffeine:3.1.8` в build.gradle.kts
  - AC: `org.springframework.boot:spring-boot-starter-cache`
  
- [ ] **Task 1.2**: Create CacheConfig
  - AC: @EnableCaching annotation
  - AC: CacheManager bean
  - AC: Separate cache configurations per type

### 2. Implement Cache for Categories
- [ ] **Task 2.1**: Add @Cacheable to CategoryService
  - AC: `getAllCategories()` кэшируется
  - AC: Cache name: "categories"
  - AC: TTL: 1 hour
  
- [ ] **Task 2.2**: Add @CacheEvict
  - AC: `createCategory()` инвалидирует кэш
  - AC: `updateCategory()` инвалидирует кэш
  - AC: `deleteCategory()` инвалидирует кэш

### 3. Implement Cache for Tests
- [ ] **Task 3.1**: Add @Cacheable to TestService
  - AC: `getPublishedTests()` кэшируется
  - AC: `getTestsByCategory()` кэшируется
  - AC: Cache name: "tests"
  - AC: TTL: 30 minutes
  
- [ ] **Task 3.2**: Add @CacheEvict
  - AC: При публикации теста инвалидировать
  - AC: При обновлении теста инвалидировать

### 4. Implement Cache for User Profiles
- [ ] **Task 4.1**: Add @Cacheable to UserService
  - AC: `getUserProfile()` кэшируется
  - AC: Cache name: "userProfiles"
  - AC: TTL: 5 minutes
  - AC: Key: userId
  
- [ ] **Task 4.2**: Add @CacheEvict
  - AC: При обновлении профиля
  - AC: При начислении XP
  - AC: При обновлении streak

### 5. Add Cache Metrics
- [ ] **Task 5.1**: Expose cache metrics
  - AC: Hit rate per cache
  - AC: Miss rate
  - AC: Eviction count
  - AC: Size
  
- [ ] **Task 5.2**: Add monitoring
  - AC: Micrometer metrics
  - AC: Actuator endpoint для кэша

### 6. Testing
- [ ] **Task 6.1**: Unit tests
  - AC: Cache configuration тесты
  - AC: Cache eviction тесты
  
- [ ] **Task 6.2**: Integration tests
  - AC: End-to-end cache flow
  - AC: Performance comparison
  
- [ ] **Task 6.3**: Load testing
  - AC: Cache hit rate measurement
  - AC: Memory usage

## Configuration

```kotlin
@Configuration
@EnableCaching
class CacheConfig {
    
    @Bean
    fun cacheManager(): CacheManager {
        val caches = listOf(
            buildCache("categories", Duration.ofHours(1)),
            buildCache("tests", Duration.ofMinutes(30)),
            buildCache("userProfiles", Duration.ofMinutes(5))
        )
        return SimpleCacheManager().apply {
            setCaches(caches)
        }
    }
    
    private fun buildCache(name: String, ttl: Duration): CaffeineCache {
        return CaffeineCache(name, Caffeine.newBuilder()
            .expireAfterWrite(ttl)
            .maximumSize(1000)
            .recordStats()
            .build())
    }
}
```

## Usage Example

```kotlin
@Service
class CategoryService(private val categoryRepository: CategoryRepository) {
    
    @Cacheable("categories")
    fun getAllCategories(): List<CategoryDto> {
        return categoryRepository.findAll().map { it.toDto() }
    }
    
    @CacheEvict(value = ["categories"], allEntries = true)
    fun createCategory(dto: CreateCategoryDto): CategoryDto {
        // ... create logic
    }
}
```

## Files to Modify

```
backend/src/main/kotlin/
├── config/
│   └── CacheConfig.kt                    # New
├── service/
│   ├── CategoryService.kt                # Add caching
│   ├── TestService.kt                    # Add caching
│   └── UserService.kt                    # Add caching
└── FunnyEnglishApplication.kt            # @EnableCaching
```

## Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Categories Hit Rate | >80% | After 1 hour warmup |
| Tests Hit Rate | >60% | After 30 min warmup |
| User Profiles Hit Rate | >40% | After 5 min warmup |
| Memory Usage | <100MB | Per cache |

## Related

- Caffeine Documentation: https://github.com/ben-manes/caffeine/wiki
- Spring Cache Abstraction: https://docs.spring.io/spring-framework/reference/integration/cache.html
- Parent Tasklist: `docs/tasklist/IMPROVEMENTS-2025-001.md`
