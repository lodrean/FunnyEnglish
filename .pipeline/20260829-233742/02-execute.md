# 02-execute — bd FunnyEnglish-wy7.5: BE контрактные дыры speaking API

## Что сделано

Закрыты контрактные дыры admin speaking API (Part 3 §3.2–3.3, ревью §2.3 предложение 4).
Все маппинги БЕЗ `/api` (context-path). Доступ — `ROLE_ADMIN` (класс-уровневый `@PreAuthorize`).

Новые endpoint'ы в `SpeakingAdminController` (`/admin/speaking`):

| Метод | Путь | Ответ | Назначение |
|---|---|---|---|
| GET | `/topics/{id}` | `200 AdminTopicResponse` (вкл. черновики и soft-deleted, `isDeleted`), 404 | deep-link топика без N+1-агрегации |
| GET | `/submissions/{id}` | `200 AdminSubmissionResponse`, 404 | deep-link `/grading/submissions/:id` («Запись не найдена» чинится) |
| GET | `/submissions/count?status=NEW` | `200 {count: number}`; status опционален (без него — total) | badge в сайдбаре вместо count-через-пагинацию |
| PATCH | `/topics/{id}/publish` | body `{isPublished}` → `200 AdminTopicResponse` | точечный publish/unpublish без полного PUT |
| PATCH | `/libraries/{id}/publish` | body `{isPublished}` → `200 AdminLibraryResponse` | симметрично спеке Part 3 §3.3 |
| POST | `/topics/{id}/questions/reorder` | body `{questionIds: [uuid,…]}` (полный упорядоченный список) → `204`; неполный/чужой набор id или невалидный UUID → 400 | batch-reorder вместо цепочки PUT |

Детали реализации:
- DTO: `PublishRequest`, `ReorderSpeakingQuestionsRequest` (questionIds — UUID строками, паттерн `CreateTopicRequest.libraryId`), `SubmissionCountResponse(count: Long)` — в `SpeakingDtos.kt`.
- `PracticeSubmissionRepository.countByStatus(status)` — derived query; `findByIdWithDetails` уже существовал и переиспользован.
- Сервисы: `SpeakingContentService.getTopic/publishLibrary/publishTopic/reorderQuestions` (displayOrder = индекс в списке, валидация полноты набора id через `require` → 400), `PracticeSubmissionService.getSubmission/countSubmissions` (audioUrl нормализуется через `MediaUrlService`, как в inbox).
- Ошибки: `NoSuchElementException` → 404, `IllegalArgumentException` → 400 (существующий `GlobalExceptionHandler`).
- Миграции БД НЕ требуются (новых полей нет).

## Изменённые файлы

- `backend/src/main/kotlin/com/sotospeak/controller/speaking/SpeakingAdminController.kt` — 6 новых endpoint'ов
- `backend/src/main/kotlin/com/sotospeak/service/speaking/SpeakingContentService.kt` — getTopic, publishLibrary, publishTopic, reorderQuestions
- `backend/src/main/kotlin/com/sotospeak/service/speaking/PracticeSubmissionService.kt` — getSubmission, countSubmissions
- `backend/src/main/kotlin/com/sotospeak/dto/SpeakingDtos.kt` — 3 новых DTO
- `backend/src/main/kotlin/com/sotospeak/repository/speaking/PracticeSubmissionRepository.kt` — countByStatus
- `backend/src/test/kotlin/com/sotospeak/controller/SpeakingFlowIntegrationTest.kt` — 4 новых интеграционных теста (№9–12): GET topic by id (draft + 404), PATCH publish/unpublish (вкл. контрактный тест `isPublished`, грабля №18), batch-reorder (204 + порядок + 400 на неполный набор), GET submission by id + count по статусам NEW/REVIEWED + 404

## Как проверить

```bash
.\gradlew.bat :backend:test --tests "com.sotospeak.controller.SpeakingFlowIntegrationTest"
```

Ручная проверка (backend на 8080, admin JWT):
```bash
curl -H "Authorization: Bearer $T" http://localhost:8080/api/admin/speaking/submissions/count?status=NEW
curl -H "Authorization: Bearer $T" http://localhost:8080/api/admin/speaking/topics/{id}
curl -X PATCH -H "Authorization: Bearer $T" -H "Content-Type: application/json" \
  -d '{"isPublished":true}' http://localhost:8080/api/admin/speaking/topics/{id}/publish
curl -X POST -H "Authorization: Bearer $T" -H "Content-Type: application/json" \
  -d '{"questionIds":["<q2>","<q1>"]}' http://localhost:8080/api/admin/speaking/topics/{id}/questions/reorder
```

## Примечания / за пределами scope

- Сборки/тесты НЕ запускались (гейт `:backend:test` прогоняет драйвер).
- Спеки не тронуты (ADR-007): реализация соответствует уже существующей спеке Part 3 §3.2–3.3. Таблица расхождений §3.4 («контрактный адаптер») и адаптер `admin-web/src/api/speakingApi.ts` (N+1 в `getAllSpeakingTopics`/`getTopicQuestions`, publish-через-PUT, reorder-цепочка, deep-link из кэша) — предмет отдельной FE-задачи; при её реализации таблицу §3.4 нужно будет обновить с согласования владельца.
- Batch-reorder на уровне ТОПИКОВ/библиотек в спеке не имеет request-shape (§3.2 определяет только `ReorderSpeakingQuestionsRequest`); admin-web такой reorder сейчас не использует — не реализовано. Если понадобится — нужна правка спеки (ADR-007).
