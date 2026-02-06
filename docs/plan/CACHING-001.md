# Plan: Caching Layer Implementation

## Ticket
CACHING-001

## Status
DRAFT → APPROVED

## Overview
Implementation of Caffeine caching for read-heavy data.

## Approach

1. Configure CacheManager with multiple cache configurations
2. Add @Cacheable to service methods
3. Add @CacheEvict on mutations
4. Create cache metrics endpoint
5. Monitor and tune

## Architecture Decisions

### ADR-001: Cache Provider
- **Context**: Нужен cache provider
- **Decision**: Caffeine (in-memory)
- **Consequences**:
  - + Простота, скорость
  - + Spring integration
  - - Не shared (приемлемо для MVP)

### ADR-002: Cache Names and TTL
- **Context**: Какие данные кэшировать
- **Decision**: 4 caches с разными TTL
- **Consequences**:
  - categories: 1h (stable)
  - tests: 30m (semi-stable)
  - userProfiles: 5m (frequently updated)
  - leaderboard: 1m (very dynamic)

## Implementation Steps

### Step 1: Update CacheConfig (30 min)
- Configure multiple caches with different TTL
- Set maximum sizes
- Enable statistics

### Step 2: CategoryService (30 min)
- @Cacheable on getAllCategories()
- @CacheEvict on create/update/delete

### Step 3: TestService (30 min)
- @Cacheable on getPublishedTests()
- @Cacheable on getTestsByCategory()
- @CacheEvict on publish/update

### Step 4: UserService (30 min)
- @Cacheable on getUserProfile()
- @CacheEvict on update

### Step 5: LeaderboardService (20 min)
- @Cacheable on getLeaderboard()
- Short TTL (1 minute)

### Step 6: Cache Metrics (30 min)
- Create CacheAdminController
- Expose hit rates
- Manual cache eviction endpoint

### Step 7: Testing (30 min)
- Unit tests
- Integration tests
- Hit rate verification

## Configuration

```kotlin
CacheConfig:
  - categories: 1h, max 100
  - tests: 30m, max 200
  - userProfiles: 5m, max 1000
  - leaderboard: 1m, max 10
```

## Success Criteria

- [ ] Categories cached (1h TTL)
- [ ] Tests cached (30m TTL)
- [ ] User profiles cached (5m TTL)
- [ ] Cache metrics available
- [ ] Hit rate > 50%
- [ ] All tests passing

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Stale data | Medium | Proper TTL tuning |
| Memory usage | Low | Max size limits |
| Cache misses | Low | Monitor and adjust |

## Dependencies
- Caffeine 3.1.8 (already added)
- Spring Cache (already configured)
