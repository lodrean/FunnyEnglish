# 02-execute — bd FunnyEnglish-wy7.7: BE кэш + ETag для /public/speaking/*

## Что сделано

Реализован двухуровневый кэш публичного speaking-контента (§4.3.3 обзора `docs/research/PROJECT-REVIEW-2026-08-28.md`):

**Server-side (Caffeine, меньше нагрузки на БД):**
- `CacheConfig`: зарегистрированы 3 кэша — `speakingPublicLibraries` (10 записей), `speakingPublicTopics` (500), `speakingPublicTopicDetails` (2000), TTL 10 мин как страховка. Имена — top-level константы `SPEAKING_PUBLIC_*`.
- Добавлена composed-аннотация `@EvictSpeakingPublicCache` (meta-`@CacheEvict` по всем трём кэшам, `allEntries = true`).
- `SpeakingContentService`: `@Cacheable` на `getPublishedLibraries` / `getPublishedTopics` / `getTopicDetail`; `@EvictSpeakingPublicCache` на всех admin-мутациях контента: create/update/delete/publish library, create/update/publish/delete topic, upsertVideo, add/update/delete/reorder questions. Инвалидация при publish — первичный механизм свежести.

**HTTP-кэш (быстрый старт веб-версии):**
- `SpeakingPublicController`: на всех трёх GET-эндпоинтах `Cache-Control: public, max-age=60` (как предлагает Part 1 §7.2) + ETag (hex-хэш DTO — data class даёт стабильный хэш; коллизия — принятый риск для read-only контента). `If-None-Match` с совпавшим ETag → `304 Not Modified` без тела (`WebRequest.checkNotModified` — работает и в MockMvc, в отличие от servlet-фильтра).

**Тесты (`SpeakingFlowIntegrationTest`, 3 новых кейса):**
- №13: ETag + Cache-Control присутствуют; повторный GET с `If-None-Match` → 304.
- №14: повторный GET detail отдаётся из Caffeine (прямая мутация через репозиторий не видна в ответе).
- №15: publish → 200 (кэш), unpublish → 404 (инвалидация), повторный publish → 200.
- В `setup()` добавлена очистка трёх кэшей через `CacheManager` — кэш живёт вне транзакции и не откатывается, иначе cross-test staleness.

## Изменённые файлы

- `backend/src/main/kotlin/com/sotospeak/config/CacheConfig.kt` — 3 кэша, константы, `@EvictSpeakingPublicCache`
- `backend/src/main/kotlin/com/sotospeak/service/speaking/SpeakingContentService.kt` — `@Cacheable`/`@EvictSpeakingPublicCache`
- `backend/src/main/kotlin/com/sotospeak/controller/speaking/SpeakingPublicController.kt` — ETag + Cache-Control + 304
- `backend/src/test/kotlin/com/sotospeak/controller/SpeakingFlowIntegrationTest.kt` — 3 теста + очистка кэшей в setup
- `memory.md` — запись в «Решения и договорённости» (+ грабля про кэш вне транзакции)

Зависимости не добавлялись: Caffeine + spring-boot-starter-cache уже были в `backend/build.gradle.kts`, `@EnableCaching` — в `CacheConfig`. `CacheAdminController` автоматически подхватит статистику новых кэшей (`/admin/cache/stats`).

## Как проверить

- Гейт драйвера: `.\gradlew.bat :backend:test` (новые кейсы в `SpeakingFlowIntegrationTest`).
- Вручную (против docker-стека):
  - `curl -i http://localhost:8080/api/public/speaking/libraries` → заголовки `ETag`, `Cache-Control: public, max-age=60`;
  - `curl -i -H "If-None-Match: <etag>" ...` → `304`;
  - `GET /api/admin/cache/stats` (admin-токен) → hit-статистика по `speakingPublic*`;
  - publish/unpublish топика в админке → публичный GET сразу отражает изменение (инвалидация).

## Замечания

- Спеки/PRD не трогались: Part 1 §7.2 прямо предлагает `Cache-Control: public, max-age=60` для public GET, поведение эндпоинтов не менялось (только добавлены заголовки и 304).
- detekt: числовые аргументы в новых `buildCache(...)` даны именованными (`ignoreNamedArgument: true` в конфиге, maxIssues: 0 с baseline).
