# Research: Rate Limiting Implementation

## Ticket
SECURITY-001

## Objective
Исследование и планирование внедрения rate limiting для защиты auth endpoints от brute force атак.

## Research Findings

### Rate Limiting Libraries for Spring Boot

#### 1. Bucket4j (Выбран)
- **Pros**: 
  - Token bucket algorithm (гибкий)
  - JCache integration (Caffeine, Redis)
  - Гибкая конфигурация
  - Хорошая документация
  - Активная поддержка
- **Cons**: 
  - Требует дополнительной конфигурации
- **Version**: 8.7.0

#### 2. Resilience4j
- **Pros**: Встроен в Spring ecosystem
- **Cons**: Более сложный для простого rate limiting

#### 3. Spring Cloud Gateway Rate Limiter
- **Pros**: Встроенный в Gateway
- **Cons**: Требует Gateway, избыточно для нашего случая

### Algorithm Selection

**Token Bucket** подходит лучше всего:
- Позволяет burst запросы (полезно для легитимных пользователей)
- Плавное пополнение токенов
- Простота понимания

### Rate Limit Configuration

| Endpoint | Limit | Window | Reasoning |
|----------|-------|--------|-----------|
| POST /auth/login | 5 | 1 minute | Защита от brute force |
| POST /auth/register | 3 | 1 minute | Защита от спама |
| POST /auth/refresh | 10 | 1 minute | Более частые запросы |
| Default | 100 | 1 minute | Общая защита |

### IP Extraction Strategy

```
Production (behind LB):
1. X-Forwarded-For header
2. First IP in chain

Development/Docker:
1. Remote address directly
```

### Response Format

**HTTP 429 Too Many Requests**
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 45 seconds.",
  "retryAfter": 45,
  "limit": 5,
  "remaining": 0
}
```

**Headers**
```
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704067200
Retry-After: 45
```

### Cache Storage

**Option 1: Caffeine (in-memory)**
- Pros: Простота, скорость
- Cons: Не shared между инстансами
- **Выбрано для начала**

**Option 2: Redis (distributed)**
- Pros: Shared между инстансами
- Cons: Дополнительная инфраструктура
- Можно добавить позже

## Affected Areas

### Backend Files
```
backend/
├── build.gradle.kts              # Add Bucket4j dependencies
├── src/main/kotlin/
│   ├── config/
│   │   ├── CacheConfig.kt        # Cache configuration for buckets
│   │   └── SecurityConfig.kt     # Add filter
│   ├── security/
│   │   └── RateLimitingFilter.kt # New filter
│   └── dto/
│       └── ErrorDto.kt           # Add rate limit error
```

### Configuration
```yaml
# application.yml
rate-limiting:
  enabled: true
  endpoints:
    login:
      capacity: 5
      refill-tokens: 1
      refill-period: 12  # seconds
    register:
      capacity: 3
      refill-tokens: 1
      refill-period: 20  # seconds
```

## Implementation Approach

1. **Add dependencies** (Bucket4j, Caffeine)
2. **Create cache configuration** (for token buckets)
3. **Create RateLimitingFilter**:
   - Extract client IP
   - Determine endpoint type
   - Check/consume tokens
   - Add rate limit headers
4. **Add to SecurityConfig**
5. **Create custom exception and handler**
6. **Write tests**

## Security Considerations

- IP spoofing через X-Forwarded-For: брать только от доверенных прокси
- Memory exhaustion: ограничить количество buckets в кэше
- Circuit breaker: если rate limiting ломается - не блокировать запросы

## Open Questions

- [ ] Какой max size для Caffeine cache? (10000 buckets?)
- [ ] Нужна ли защита от IP spoofing?
- [ ] Логировать rate limit violations?

## Recommendation

Использовать Bucket4j с Caffeine cache для начала. Настроить:
- Login: 5/minute
- Register: 3/minute
- Правильное извлечение IP
- Информативные headers и error messages
