# Research: Caching Layer Implementation

## Ticket
CACHING-001

## Objective
Исследование и планирование внедрения кэширования для read-heavy данных.

## Research Findings

### Cache Providers

#### 1. Caffeine (Выбран)
- **Pros**:
  - Высокая производительность (лучше в бенчмарках)
  - Гибкая конфигурация (TTL, максимальный размер)
  - Встроенная статистика
  - Интеграция со Spring Cache
  - Не требует внешней инфраструктуры
- **Cons**:
  - Не shared между инстансами
  - Потеря кэша при рестарте

#### 2. Redis
- **Pros**: Shared между инстансами, persistence
- **Cons**: Требует инфраструктуры, сетевая задержка

### Caching Strategy

#### What to Cache
| Data | Frequency | Mutability | Cache TTL |
|------|-----------|------------|-----------|
| Categories | Read-heavy | Low | 1 hour |
| Tests list | Read-heavy | Medium | 30 min |
| User profile | Read-heavy | High | 5 min |
| Leaderboard | Read-heavy | High | 1 min |

#### Cache Configuration
```kotlin
// Categories: 1 hour, max 100 entries
// Tests: 30 min, max 200 entries  
// User profiles: 5 min, max 1000 entries
// Leaderboard: 1 min, max 10 entries
```

### Spring Cache Annotations

```kotlin
@Cacheable("categories")      // Cache result
@CacheEvict("categories")     // Remove from cache
@CachePut("categories")       // Update cache
@Caching(...)                 // Multiple operations
```

### Implementation Approach

1. **Configure Caffeine CacheManager**
2. **Add @Cacheable to read methods**
3. **Add @CacheEvict to write methods**
4. **Add cache metrics endpoint**
5. **Monitor hit rates**

## Affected Areas

```
backend/src/main/kotlin/com/funnyenglish/
├── config/
│   └── CacheConfig.kt          # Cache manager configuration
├── service/
│   ├── CategoryService.kt      # Add caching
│   ├── TestService.kt          # Add caching
│   ├── UserService.kt          # Add caching
│   └── LeaderboardService.kt   # Add caching
└── controller/
    └── CacheAdminController.kt # Cache metrics endpoint
```

## Open Questions

- [ ] Какой hit rate считать хорошим? (>50%?)
- [ ] Нужен ли кэш для других сущностей?
- [ ] Добавить cache warming?

## Recommendation

Использовать Caffeine с Spring Cache abstraction. Начать с categories и tests (read-heavy, low mutation).
