# 02-execute — bd FunnyEnglish-nj2.2: SEC утечка черновиков через GET /tests/**

## Что сделано

1. **Фильтр `isPublished` в публичном `TestService.getTestById`** (`backend/.../service/TestService.kt`).
   - `GET /tests/{testId}` и `GET /tests/{testId}/details` — permitAll (SecurityConfig.kt:37), при этом
     `getTestById` возвращал любой тест, включая черновики (`isPublished = false`).
   - Теперь для неопубликованного теста бросается `NoSuchElementException("Test not found")` → 404 через
     `GlobalExceptionHandler` (так же, как для несуществующего id — черновик неотличим от отсутствующего).
   - Побочный эффект закрывает и кэш-дыру: Spring **не кэширует исключения**, поэтому черновик больше
     никогда не попадает в `testDetails` (Caffeine, TTL 15 мин, CacheConfig.kt:23).
2. **Инвалидация кэша при публикации — проверена, уже корректна**: единственный путь публикации/снятия
   с публикации — `PUT /admin/tests/{id}` → `TestService.updateTest()` с
   `@CacheEvict(value = ["tests", "testDetails"], key = "#testId")` (TestService.kt:202), `createTest`
   эвиктит allEntries. Дополнительных правок не потребовалось — ключ эвикции совпадает с ключом
   `@Cacheable` (String testId). Админский `getTestByIdForAdmin` кэша не имеет и черновики отдаёт штатно.
3. **Unit-тест** `backend/src/test/kotlin/com/sotospeak/service/TestServiceTest.kt` (mockk, по образцу
   `UserServiceMergeGuestProgressTest`): черновик → NoSuchElementException; опубликованный → 200-данные;
   отсутствующий → NoSuchElementException.

## Изменённые/созданные файлы

- `backend/src/main/kotlin/com/sotospeak/service/TestService.kt` — фильтр isPublished в getTestById (+комментарий SEC).
- `backend/src/test/kotlin/com/sotospeak/service/TestServiceTest.kt` — новый unit-тест (3 кейса).

## Как проверить

- Гейт драйвера: `.\gradlew.bat :backend:test` (сам не запускал — по инструкции гейты прогоняет драйвер).
- Ручная проверка: создать тест с `isPublished=false`, анонимно `GET /api/tests/{id}` → 404;
  после `PUT /admin/tests/{id}` с `isPublished=true` → 200 без ожидания TTL кэша.

## Замечания

- Спеки/PRD не трогал (ADR-007) — правка спек не требуется, поведение соответствует §2.1 Б2.
- Соседняя (вне скоупа задачи) наблюдение: `@CacheEvict` в `updateTest` для кэша `tests` (списки)
  использует key=testId, тогда как ключи списков — `categoryId-userId`/`all-userId` → списки после
  публикации обновляются только по TTL. Утечки контента нет (детали эвиктятся, фильтр на месте),
  это функциональная сталeness — при желании завести отдельную задачу.
