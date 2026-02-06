# Plan: Rate Limiting Implementation

## Ticket
SECURITY-001

## Status
DRAFT → APPROVED

## Overview
Implementation of rate limiting for auth endpoints using Bucket4j.

## Approach

Создать OncePerRequestFilter который:
1. Извлекает client IP из request
2. Определяет тип endpoint (login/register/other)
3. Проверяет rate limit через Bucket4j
4. Добавляет rate limit headers
5. Возвращает 429 если лимит превышен

## Architecture Decisions

### ADR-001: Rate Limiting Library
- **Context**: Нужна библиотека для rate limiting
- **Decision**: Bucket4j с Caffeine cache
- **Consequences**: 
  - + Гибкая конфигурация
  - + Быстрая интеграция
  - - Не distributed (приемлемо для MVP)

### ADR-002: IP Extraction Strategy
- **Context**: Нужно корректно определять client IP
- **Decision**: Поддержка X-Forwarded-For с fallback на remoteAddr
- **Consequences**:
  - + Работает за load balancer
  - - Требует доверия к заголовку

## Implementation Steps

### Step 1: Dependencies (30 min)
- [ ] Add Bucket4j dependencies to build.gradle.kts
- [ ] Refresh dependencies

### Step 2: Configuration (30 min)
- [ ] Create CacheConfig.kt for bucket storage
- [ ] Add rate limiting properties to application.yml

### Step 3: RateLimitingFilter (1.5 hours)
- [ ] Create filter class
- [ ] Implement IP extraction
- [ ] Implement bucket management
- [ ] Add rate limit headers
- [ ] Handle limit exceeded

### Step 4: Security Integration (30 min)
- [ ] Add filter to SecurityConfig
- [ ] Set correct order

### Step 5: Error Handling (30 min)
- [ ] Create RateLimitExceededException
- [ ] Add handler to GlobalExceptionHandler
- [ ] Create error response DTO

### Step 6: Testing (1.5 hours)
- [ ] Unit tests for filter
- [ ] Integration tests
- [ ] E2E test script

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| IP spoofing | Medium | Validate X-Forwarded-For, доверять только внутренним прокси |
| Memory leak | Low | Caffeine max size, TTL на buckets |
| False positives | Medium | Лимиты настроены консервативно |

## Configuration

```yaml
rate-limiting:
  login:
    capacity: 5
    refill-tokens: 1
    refill-period-seconds: 12
  register:
    capacity: 3
    refill-tokens: 1
    refill-period-seconds: 20
```

## Success Criteria

- [ ] Login endpoint limited to 5/minute
- [ ] Register endpoint limited to 3/minute
- [ ] 429 response with proper headers
- [ ] All tests passing
- [ ] Works in Docker environment

## Dependencies

- Bucket4j 8.7.0
- Caffeine 3.1.8

## Notes

- Для production с load balancer - использовать Redis
- Мониторить rate limit violations
- Рассмотреть whitelist для internal IPs
