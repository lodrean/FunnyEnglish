# Plan: Improvements 2025 Q1 - Best Practices Implementation

## Ticket
IMPROVEMENTS-2025-001

## Status
DRAFT

## Overview
План внедрения best practices для Spring Boot, KMP и UX на основе исследований 2025 года.

## Approach

Фазовый подход с приоритизацией по критичности:
- **Phase 1**: Security & Stability (Must have)
- **Phase 2**: Performance (Should have)
- **Phase 3**: UX Enhancements (Nice to have)
- **Phase 4**: Architecture Improvements (Technical debt)

## Architecture Decisions

### ADR-001: Cache Provider Selection
- **Context**: Нужно выбрать cache provider для backend
- **Decision**: Использовать Caffeine для начала, Redis для production scaling
- **Consequences**: 
  - + Простота разработки
  - + Нет дополнительной инфраструктуры
  - - Не shared между инстансами
  - - Потеря кэша при рестарте

### ADR-002: Rate Limiting Strategy
- **Context**: Защита от brute force на auth endpoints
- **Decision**: Bucket4j в приложении, не на gateway
- **Consequences**:
  - + Простота деплоя
  - + Легче тестировать
  - - Не защищает от DDoS
  - - Нет централизованного управления

### ADR-003: Kotlin Serialization Migration
- **Context**: Spring Boot 4 рекомендует Kotlin Serialization
- **Decision**: Постепенная миграция, endpoint за endpoint
- **Consequences**:
  - + Меньше риска
  - + Можно откатить
  - - Дольше процесс
  - - Временно две библиотеки

### ADR-004: Accessibility Implementation
- **Context**: Нужна accessibility поддержка
- **Decision**: WCAG 2.1 AA как цель, начать с critical flows
- **Consequences**:
  - + Более широкая аудитория
  - + Лучшее SEO
  - - Время на внедрение
  - - Тестирование на screen readers

## Implementation Steps

### Phase 1: Security & Stability (Week 1-2)

#### Week 1: Security Enhancements
1. **Add rate limiting**
   - Добавить Bucket4j dependency
   - Создать RateLimitingFilter
   - Настроить лимиты для auth endpoints
   - Написать тесты

2. **Implement secure headers**
   - Создать SecurityHeadersFilter
   - Настроить CSP
   - Добавить HSTS
   - Проверить на securityheaders.com

3. **Add audit logging**
   - Создать AuditLoggingFilter
   - Настроить маскирование sensitive данных
   - Добавить async логирование

#### Week 2: Stability Improvements
1. **Fix XP calculation edge case**
   - Проанализировать текущий код
   - Добавить edge case тесты
   - Исправить баг

2. **Add proper error handling**
   - Global exception handler review
   - Добавить кастомные exceptions
   - Улучшить error messages

### Phase 2: Performance (Week 3-4)

#### Week 3: Caching
1. **Setup caching infrastructure**
   - Добавить Caffeine dependency
   - Создать CacheConfig
   - Настроить метрики

2. **Implement cache for read-heavy data**
   - Categories cache (TTL: 1 hour)
   - Tests list cache (TTL: 30 min)
   - User profile cache (TTL: 5 min)

3. **Add cache eviction**
   - @CacheEvict на update методах
   - Инвалидация по событиям

#### Week 4: Database & Monitoring
1. **Database optimization**
   - Анализ запросов с помощью pg_stat_statements
   - Добавить недостающие индексы
   - Оптимизировать N+1 queries

2. **Add metrics**
   - Custom business metrics
   - JVM metrics
   - Cache metrics

### Phase 3: UX Enhancements (Week 5-6)

#### Week 5: Accessibility
1. **Add content descriptions**
   - Проверить все интерактивные элементы
   - Добавить semantics
   - semantic { contentDescription = "..." }

2. **Improve color contrast**
   - Аудит текущих цветов
   - Обновить цвета для WCAG AA
   - Тестировать с Color Contrast Analyzer

3. **Touch target sizing**
   - Проверить минимум 48dp
   - Увеличить где нужно

#### Week 6: Animations & Micro-interactions
1. **Add haptic feedback**
   - Haptic feedback на correct answer
   - Achievement unlock haptic
   - Error haptic

2. **Improve loading states**
   - Skeleton screens
   - Branded loading messages
   - Progressive loading

3. **Enhanced celebrations**
   - More confetti variations
   - Sound effects (optional)
   - Share achievement cards

### Phase 4: Architecture (Week 7-8)

#### Week 7: Kotlin Serialization Migration
1. **Setup Kotlin Serialization**
   - Добавить dependencies
   - Настроить Kotlin Serialization module
   - Создать тестовый endpoint

2. **Migrate DTOs gradually**
   - Начать с internal DTOs
   - Потом public API
   - Убедиться в backward compatibility

#### Week 8: Testing & Documentation
1. **Add comprehensive tests**
   - ViewModel tests
   - Repository tests
   - UI tests для critical flows

2. **Update documentation**
   - API docs
   - ADRs
   - Deployment guide

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Kotlin Serialization breaking changes | High | Gradual migration, feature flags |
| Cache inconsistency | Medium | Proper eviction, TTL tuning |
| Accessibility testing time | Medium | Start with automated checks |
| Performance regression | Medium | Load testing before deploy |

## Dependencies

### Technical
- Bucket4j 8.x
- Caffeine 3.x
- Kotlin Serialization 1.6+
- Micrometer 1.12+

### Infrastructure
- Redis (optional, for distributed cache)
- Prometheus/Grafana (for metrics)

## Success Criteria

- [ ] All security headers present
- [ ] Rate limiting working
- [ ] Cache hit rate > 50%
- [ ] API p95 latency < 200ms
- [ ] Accessibility score > 90%
- [ ] Test coverage > 70%
- [ ] Zero security vulnerabilities

## Questions for Review

1. Приоритеты в порядке?
2. Достаточно ли 2 недели на Phase 1?
3. Нужен ли Redis сразу или можно отложить?
4. Какие метрики наиболее важны для мониторинга?
