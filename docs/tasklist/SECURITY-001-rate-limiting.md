# Tasklist: Security - Rate Limiting Implementation

## Ticket
SECURITY-001

## Status
🔄 READY FOR IMPLEMENTATION

## Overview
Внедрение rate limiting для защиты от brute force атак на auth endpoints.

## Acceptance Criteria
- [ ] Rate limiting работает на `/auth/login` и `/auth/register`
- [ ] 5 попыток login в минуту с одного IP
- [ ] 3 попытки register в минуту с одного IP
- [ ] При превышении лимита возвращается 429 Too Many Requests
- [ ] Заголовки X-RateLimit-* присутствуют в ответе
- [ ] Работает в Docker окружении

## Tasks

### 1. Setup Dependencies
- [ ] **Task 1.1**: Add Bucket4j dependency to build.gradle.kts
  - AC: `com.bucket4j:bucket4j-core:8.7.0` добавлен
  - AC: `com.bucket4j:bucket4j-jcache:8.7.0` добавлен
  
- [ ] **Task 1.2**: Add cache configuration
  - AC: Caffeine или concurrent map cache настроен
  - AC: Cache configuration в `CacheConfig.kt`

### 2. Implement Rate Limiting Filter
- [ ] **Task 2.1**: Create RateLimitingFilter class
  - AC: Фильтр extends OncePerRequestFilter
  - AC: Извлекает client IP из request
  - AC: Проверяет лимит по endpoint
  - AC: Добавляет rate limit headers
  
- [ ] **Task 2.2**: Configure buckets per endpoint
  - AC: Login bucket: 5 tokens, refill 1 per 12 seconds
  - AC: Register bucket: 3 tokens, refill 1 per 20 seconds
  - AC: Default bucket: 100 tokens per minute
  
- [ ] **Task 2.3**: Add error response
  - AC: 429 status code
  - AC: JSON error body с retry-after
  - AC: Заголовок Retry-After

### 3. Integration
- [ ] **Task 3.1**: Add filter to SecurityConfig
  - AC: Фильтр добавлен перед JWT фильтром
  - AC: Порядок фильтров корректный
  
- [ ] **Task 3.2**: Test with Docker
  - AC: Rate limiting работает в docker-compose
  - AC: IP определяется корректно (учитывать X-Forwarded-For)

### 4. Testing
- [ ] **Task 4.1**: Unit tests
  - AC: RateLimitingFilter тесты
  - AC: Bucket configuration тесты
  
- [ ] **Task 4.2**: Integration tests
  - AC: E2E тесты rate limiting
  - AC: Проверка headers
  
- [ ] **Task 4.3**: Load testing
  - AC: Скрипт для нагрузочного теста
  - AC: Проверка отказа под нагрузкой

## Files to Modify

```
backend/
├── build.gradle.kts                    # Add dependencies
├── src/main/kotlin/
│   ├── config/
│   │   ├── CacheConfig.kt              # Cache configuration
│   │   └── SecurityConfig.kt           # Add filter
│   └── security/
│       └── RateLimitingFilter.kt       # New file
└── src/test/
    └── kotlin/
        └── security/
            └── RateLimitingFilterTest.kt
```

## Technical Details

### Rate Limit Configuration
```kotlin
// Login: 5 attempts per minute
val loginBucket = Bucket.builder()
    .addLimit(Bandwidth.classic(5, Duration.ofMinutes(1)))
    .build()

// Register: 3 attempts per minute
val registerBucket = Bucket.builder()
    .addLimit(Bandwidth.classic(3, Duration.ofMinutes(1)))
    .build()
```

### Response Headers
```
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 3
X-RateLimit-Reset: 1640995200
Retry-After: 45
```

### Error Response
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 45 seconds.",
  "retryAfter": 45
}
```

## Notes

- Для production с load balancer нужно учитывать X-Forwarded-For
- Для distributed setup использовать Redis вместо local cache
- Мониторить rate limiting metrics

## Related

- Research: `docs/research/IMPROVEMENTS-2025-001.md`
- Plan: `docs/plan/IMPROVEMENTS-2025-001.md`
- Parent Tasklist: `docs/tasklist/IMPROVEMENTS-2025-001.md`
