# So to Speak API Documentation

## Base URL
```
Development: http://localhost:8080
Production: https://api.sotospeak.app
```

## Authentication

Все защищённые endpoints требуют JWT токен в заголовке:
```
Authorization: Bearer <token>
```

**Модель доступа speaking-тренажёра:**
- `/public/*` — публичные (гость может читать контент, без токена)
- `/speaking/*` — только авторизованные пользователи (practice-записи)
- `/admin/*` — роль `ADMIN`

---

## Speaking Trainer API

Основной API продукта: ученик смотрит короткие видео с субтитрами и отвечает
на вопросы голосом. Контроллеры: `backend/.../controller/speaking/`
(`SpeakingPublicController`, `SpeakingSubmissionController`, `SpeakingAdminController`).
Все пути ниже — с учётом context-path `/api` (маппинги в контроллерах БЕЗ `/api`).

**Лимиты:**
- practice-аудио: **≤ 5 МБ**, `durationSec` 1..60 (валидация в `PracticeSubmissionService`)
- загрузка видео/субтитров: до **200 МБ** (`client_max_body_size 200m` в nginx, `spring.servlet.multipart.max-file-size=200MB`)

### Public (гость, без авторизации)

#### GET /public/speaking/libraries
Список опубликованных библиотек.

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "title": "Разговорный английский",
    "description": "...",
    "coverUrl": "https://cdn.example.com/sotospeak/covers/lib1.jpg",
    "topicCount": 5
  }
]
```
`topicCount` — только опубликованные и не удалённые топики.

---

#### GET /public/speaking/libraries/{id}/topics
Список опубликованных топиков библиотеки.

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "title": "Знакомство в кафе",
    "description": "...",
    "durationSeconds": 45,
    "questionCount": 3,
    "hasSubtitles": true
  }
]
```
`durationSeconds` — `null`, если видео ещё не загружено.

---

#### GET /public/speaking/topics/{id}
Детали топика: видео + вопросы (отсортированы по `displayOrder`).

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "libraryId": "uuid",
  "title": "Знакомство в кафе",
  "description": "...",
  "video": {
    "videoUrl": "https://cdn.example.com/sotospeak/videos/t1.mp4",
    "subtitleUrl": "https://cdn.example.com/sotospeak/subs/t1.vtt",
    "durationSeconds": 45
  },
  "questions": [
    { "id": "uuid", "text": "What did the customer order?", "displayOrder": 0 }
  ]
}
```
`video` и `subtitleUrl` могут быть `null`. Субтитры — WebVTT, публичный URL через `S3_PUBLIC_URL`.

---

### User (авторизованный ученик)

#### POST /speaking/submissions
Отправить practice-запись учителю на проверку. Только для авторизованных (у гостя practice залочен).

**Content-Type:** `multipart/form-data`

**Form Fields:**
- `file` — аудиофайл записи (≤ 5 МБ)
- `topicId` — UUID топика
- `durationSec` — длительность записи в секундах (1..60)

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "topicId": "uuid",
  "topicTitle": "Знакомство в кафе",
  "audioUrl": "https://cdn.example.com/sotospeak/practice/u1.webm",
  "durationSec": 28,
  "status": "NEW",
  "grade": null,
  "createdAt": "2026-08-01T10:00:00Z"
}
```

**Errors:**
- `400` — `durationSec` вне 1..60 или файл > 5 МБ
- `401` — не авторизован

---

#### GET /speaking/submissions/my
Мои practice-записи со статусами проверки.

**Response:** `200 OK` — массив `SubmissionResponse` (как выше).
`status`: `NEW` (на проверке) | `REVIEWED` (проверено, поле `grade` заполнено):
```json
{
  "grade": {
    "grammar": 8,
    "vocabulary": 7,
    "pronunciation": 9,
    "fluency": 8,
    "total": 8.0,
    "comment": "Хорошая интонация, поработайте над артиклями",
    "reviewerName": "Teacher Anna",
    "createdAt": "2026-08-01T12:00:00Z",
    "updatedAt": "2026-08-01T12:00:00Z"
  }
}
```
Критерии оценки 1..10 каждый; `total` — авто-усреднённый балл.

---

### Admin (роль ADMIN)

Все эндпоинты `/admin/speaking/*` требуют роль `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).

#### Libraries (CRUD)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/admin/speaking/libraries` | Все библиотеки (включая неопубликованные) |
| POST | `/admin/speaking/libraries` | Создать библиотеку → `201` |
| PUT | `/admin/speaking/libraries/{id}` | Обновить (частично) |
| DELETE | `/admin/speaking/libraries/{id}` | Удалить → `204` |

**CreateLibraryRequest:**
```json
{
  "title": "Новая библиотека",
  "description": "...",
  "coverUrl": "https://...",
  "displayOrder": 0,
  "isPublished": false
}
```
**UpdateLibraryRequest** — те же поля, все опциональные.

**AdminLibraryResponse** = `LibraryResponse` + `displayOrder`, `isPublished`, `createdAt`, `updatedAt`.

#### Topics (CRUD)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/admin/speaking/topics?libraryId={uuid}` | Топики библиотеки (вкл. удалённые/черновики) |
| POST | `/admin/speaking/topics` | Создать топик → `201` |
| PUT | `/admin/speaking/topics/{id}` | Обновить (частично) |
| DELETE | `/admin/speaking/topics/{id}` | Soft delete (идемпотентно) → `204` |

**CreateTopicRequest:**
```json
{
  "libraryId": "uuid",
  "title": "Новый топик",
  "description": "...",
  "displayOrder": 0,
  "isPublished": false
}
```

**AdminTopicResponse** = детали топика + `isDeleted`, `video`, `questions`, `createdAt/updatedAt`.

#### Video (upsert)

**PUT /admin/speaking/topics/{id}/video** — привязать/заменить видео топика.
Файл предварительно загружается через `POST /admin/media/upload` (видео до 200 МБ).

```json
{
  "videoUrl": "https://cdn.example.com/sotospeak/videos/t1.mp4",
  "subtitleUrl": "https://cdn.example.com/sotospeak/subs/t1.vtt",
  "durationSeconds": 45
}
```
`videoUrl` обязателен (≤ 500 символов), `durationSeconds` ≥ 1, `subtitleUrl` опционален (WebVTT).

#### Questions (CRUD)

| Метод | Путь | Описание |
|---|---|---|
| POST | `/admin/speaking/topics/{id}/questions` | Добавить вопрос → `201` |
| PUT | `/admin/speaking/questions/{id}` | Обновить вопрос |
| DELETE | `/admin/speaking/questions/{id}` | Удалить → `204` |

**CreateSpeakingQuestionRequest:**
```json
{ "text": "What did the customer order?", "displayOrder": 0 }
```

#### Grading inbox (проверка записей учителем)

**GET /admin/speaking/submissions** — список записей с фильтрами.

**Query Parameters:**
- `status` — `NEW` | `REVIEWED`
- `userId`, `topicId` — UUID-фильтры
- `dateFrom`, `dateTo` — ISO-даты (`dateTo` включительно)
- `page` (default 0), `size` (default 20, max 100)

**Response:** `200 OK` — `Page<AdminSubmissionResponse>` (сортировка `createdAt DESC`):
```json
{
  "content": [
    {
      "id": "uuid",
      "userId": "uuid",
      "userEmail": "student@example.com",
      "userDisplayName": "Иван Петров",
      "topicId": "uuid",
      "topicTitle": "Знакомство в кафе",
      "audioUrl": "https://...",
      "durationSec": 28,
      "status": "NEW",
      "grade": null,
      "createdAt": "2026-08-01T10:00:00Z"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

**POST /admin/speaking/submissions/{id}/grade** — выставить оценку → `201`.
**PUT /admin/speaking/submissions/{id}/grade** — исправить оценку → `200`.

**GradeSubmissionRequest:**
```json
{
  "grammar": 8,
  "vocabulary": 7,
  "pronunciation": 9,
  "fluency": 8,
  "comment": "Комментарий учителю..."
}
```
Все критерии обязательны, 1..10; `comment` ≤ 5000 символов. После оценки статус записи → `REVIEWED`.

---

### Guest Events (аналитика гостей)

#### POST /public/guest-events
Приём обезличенных событий гостевых пользователей (публичный, rate limit).
Источник: `GuestEventController`.

**Request:**
```json
{
  "events": [
    {
      "anonymousId": "uuid",
      "type": "SESSION_STARTED",
      "testId": null,
      "score": null,
      "maxScore": null,
      "timeSpentSeconds": null,
      "clientTimestamp": "2026-08-01T09:00:00Z"
    }
  ]
}
```
- batch 1..50 событий
- `type`: `SESSION_STARTED` | `TEST_COMPLETED`
- `score`/`maxScore`/`timeSpentSeconds` — опциональны; события с невалидным `anonymousId` или `score > maxScore` отбрасываются (клиенту не доверяем)
- `clientTimestamp` — клиентское время (ISO-8601); если отсутствует — серверное

**Response:** `200 OK`
```json
{ "accepted": 1 }
```

---

## Client Logs API

Сквозное логирование WARN/ERROR с клиентов (OpenSpec `add-client-logging`, bd `i01`).
Контроллеры: `backend/.../controller/ClientLogController.kt` (`ClientLogController`,
`AdminLogController`). Таблица `client_logs` (миграция V23). Пути — с учётом
context-path `/api`.

**Лимиты:** batch 1..50 записей; `message` обрезается до 4 КБ, `stackTrace` — до 16 КБ;
`tag` ≤ 100, `platform` ≤ 20, `appVersion` ≤ 50.

### Public (без авторизации, rate limit по IP)

#### POST /public/logs
Пакетная отправка клиентских логов. Невалидные записи (неизвестный `level`,
невалидный UUID `anonymousId`) отбрасываются поштучно — пакет не отклоняется целиком.

**Request:**
```json
{
  "logs": [
    {
      "timestamp": "2026-08-05T12:00:00Z",
      "level": "WARN|ERROR|INFO|DEBUG",
      "tag": "HttpClient",
      "message": "HTTP call failed",
      "stackTrace": "... (optional)",
      "platform": "android|desktop|wasm|admin-web",
      "appVersion": "1.0.0-qa (optional)",
      "anonymousId": "uuid (optional, guestId устройства; у admin-web отсутствует)"
    }
  ]
}
```

**Response:** `200 OK`
```json
{ "accepted": 1 }
```

Ошибки: `400` — пустой batch, batch > 50, нарушение bean-валидации полей.

---

### Admin (ROLE_ADMIN)

#### GET /admin/logs
Просмотр логов с фильтрами и пагинацией (Spring Page).

**Query params:** `level` (WARN/ERROR/…), `platform`, `from`/`to` (ISO-8601 datetime,
напр. `2026-08-05T00:00:00Z`), `q` (поиск подстроки в message/tag), `page` (≥0,
дефолт 0), `size` (1..100, дефолт 20).

**Response:** `200 OK` — Spring Page:
```json
{
  "content": [
    {
      "id": "uuid",
      "anonymousId": "uuid | null",
      "level": "ERROR",
      "tag": "HttpClient",
      "message": "HTTP call failed",
      "stackTrace": null,
      "platform": "android",
      "appVersion": "1.0.0-qa",
      "clientTimestamp": "2026-08-05T12:00:00Z",
      "createdAt": "2026-08-05T12:00:05Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

Ошибки: `403` — не-админ. UI: страница «Client Logs» в admin-web (`/logs`).

---

## Auth Endpoints

### POST /auth/register
Регистрация нового пользователя.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "displayName": "Иван Петров"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Иван Петров",
    "level": 1,
    "totalPoints": 0,
    "currentStreak": 0,
    "role": "USER"
  }
}
```

**При включённой email-верификации** (`EMAIL_VERIFICATION_ENABLED=true`) токен НЕ выдаётся —
на email отправляется письмо со ссылкой подтверждения, login возможен после неё:
```json
{
  "user": { "id": "uuid", "email": "user@example.com", "displayName": "Иван Петров" },
  "emailSent": true,
  "token": null
}
```

**Errors:**
- `400` - Email уже существует
- `400` - Невалидные данные

---

### GET /auth/verify-email?token=...
Подтверждение email по ссылке из письма (публичный, `EMAIL_VERIFICATION_ENABLED=true`).
Ответ — HTML-страница «Почта подтверждена!» либо «Ссылка недействительна» (истёкший/
использованный/невалидный токен — детали не раскрываются). Токен одноразовый, TTL 24ч.

**Response:** `200 OK` (text/html) — и при успехе, и при ошибке токена.
**Errors:** `404` - флаг верификации выключен.

---

### POST /auth/resend-verification
Повторная отправка письма верификации (публичный, rate-limited).

**Request:**
```json
{ "email": "user@example.com" }
```

**Response:** `200 OK` — всегда (anti-enumeration: одинаковый ответ для существующего,
несуществующего и уже подтверждённого email). Для неподтверждённого — старые токены
инвалидируются, выдаётся новый и отправляется письмо.
**Errors:** `404` - флаг выключен; `429` - rate limit.

---

### POST /auth/login
Вход в систему.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Иван Петров",
    "level": 5,
    "totalPoints": 1250,
    "currentStreak": 7,
    "role": "USER"
  }
}
```

**Errors:**
- `401` - Неверный email или пароль
- `403` - `EMAIL_NOT_VERIFIED` — email не подтверждён (при `EMAIL_VERIFICATION_ENABLED=true`;
  запросить письмо повторно — `POST /auth/resend-verification`)

---

### POST /auth/refresh
Обновить access-токен. Отдельных refresh-токенов нет: принимается access-токен (в т.ч. истёкший) — обмен возможен в пределах окна `app.jwt.refresh-window` (по умолчанию 7 дней после `exp`, env `JWT_REFRESH_WINDOW`). KMP-клиент вызывает автоматически при 401 и повторяет исходный запрос.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** `200 OK` - аналогично `/auth/login`

**Errors:**
- `400` - Невалидный или истёкший refresh токен

---

### POST /auth/oauth/{provider}
OAuth авторизация (Google, VK, Telegram).

**Path Parameters:**
- `provider` - `google` | `vk` | `telegram`

**Request:**
```json
{
  "token": "oauth_access_token",
  "email": "user@gmail.com",
  "displayName": "John Doe"
}
```

**Response:** `200 OK` - аналогично `/auth/login`

---

## User Endpoints

⚠️ **Частично legacy**: `GET /users/me` используется speaking-клиентом (профиль/авторизация);
эндпоинты прогресса/достижений ниже — legacy (не используется speaking-клиентом, судьба решается владельцем).

### GET /users/me
Получить текущего пользователя.

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "displayName": "Иван Петров",
  "avatarUrl": "https://...",
  "level": 5,
  "totalPoints": 1250,
  "currentStreak": 7,
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

### GET /users/me/profile
⚠️ Legacy (не используется speaking-клиентом, судьба решается владельцем).

Получить профиль с детальной статистикой.

**Response:** `200 OK`
```json
{
  "user": { ... },
  "stats": {
    "testsCompleted": 25,
    "totalStars": 68,
    "perfectScores": 12,
    "currentLevel": 5,
    "pointsToNextLevel": 150
  },
  "achievements": [
    {
      "id": "uuid",
      "code": "FIRST_TEST",
      "name": "Первые шаги",
      "description": "Пройдите первый тест",
      "icon": "🎯",
      "earned": true
    }
  ]
}
```

---

### GET /users/me/progress
⚠️ Legacy (не используется speaking-клиентом, судьба решается владельцем).

Получить прогресс по всем тестам.

**Response:** `200 OK`
```json
[
  {
    "testId": "uuid",
    "testTitle": "Животные - уровень 1",
    "score": 8,
    "maxScore": 10,
    "stars": 2,
    "bestScore": 9,
    "attemptsCount": 3,
    "lastAttemptAt": "2024-01-20T15:30:00Z"
  }
]
```

---

### GET /users/me/progress/summary
⚠️ Legacy (не используется speaking-клиентом, судьба решается владельцем).

Сводка прогресса по категориям.

**Response:** `200 OK`
```json
{
  "totalTests": 50,
  "completedTests": 25,
  "totalStars": 68,
  "maxStars": 150,
  "categoriesProgress": [
    {
      "categoryId": "uuid",
      "categoryName": "Животные",
      "testsCount": 10,
      "completedCount": 8,
      "totalStars": 22,
      "maxStars": 30
    }
  ]
}
```

---

### GET /users/me/achievements
⚠️ Legacy (не используется speaking-клиентом, судьба решается владельцем).

Получить достижения пользователя.

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "code": "PERFECT_SCORE",
    "name": "Перфекционист",
    "description": "Получите 100% на любом тесте",
    "icon": "⭐",
    "pointsReward": 50,
    "earned": true,
    "earnedAt": "2024-01-18T12:00:00Z"
  }
]
```

---

## Achievements Endpoints

⚠️ **Legacy** (не используется speaking-клиентом, судьба решается владельцем).

### GET /achievements
Получить список всех достижений. Если передан токен, поле `earned` будет отражать прогресс пользователя.

**Headers:** `Authorization: Bearer <token>` (опционально)

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "code": "FIRST_TEST",
    "name": "Первые шаги",
    "description": "Пройдите первый тест",
    "iconUrl": "https://...",
    "pointsReward": 50,
    "earned": true
  }
]
```

---

## Category Endpoints

⚠️ **Legacy** (не используется speaking-клиентом, судьба решается владельцем).

### GET /categories
Получить все категории с прогрессом пользователя.

**Headers:** `Authorization: Bearer <token>` (опционально)

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "name": "Животные",
    "description": "Изучайте названия животных",
    "iconUrl": "https://...",
    "testsCount": 10,
    "completedCount": 8,
    "totalStars": 22
  }
]
```

---

### GET /categories/{categoryId}/tests
Получить тесты категории.

**Path Parameters:**
- `categoryId` - UUID категории

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "categoryId": "uuid",
    "title": "Животные - уровень 1",
    "description": "Базовые названия животных",
    "thumbnailUrl": "https://...",
    "difficulty": "EASY",
    "pointsReward": 10,
    "questionsCount": 10,
    "userProgress": {
      "completed": true,
      "bestScore": 9,
      "maxScore": 10,
      "stars": 2,
      "percentage": 90
    }
  }
]
```

---

## Test Endpoints

⚠️ **Legacy** (не используется speaking-клиентом, судьба решается владельцем).

### GET /tests
Получить все опубликованные тесты.

**Query Parameters:**
- `categoryId` (опционально) - фильтр по категории

**Response:** `200 OK` - массив тестов (см. `/categories/{id}/tests`)

---

### GET /tests/{testId}
Получить детали теста с вопросами.

**Path Parameters:**
- `testId` - UUID теста

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "categoryId": "uuid",
  "title": "Животные - уровень 1",
  "description": "Базовые названия животных",
  "difficulty": "EASY",
  "pointsReward": 10,
  "timeLimitSeconds": 300,
  "questions": [
    {
      "id": "uuid",
      "type": "TEXT_SELECT",
      "text": "Как по-английски 'кошка'?",
      "audioUrl": null,
      "imageUrl": null,
      "points": 1,
      "answers": [
        {
          "id": "uuid",
          "text": "Cat",
          "imageUrl": null,
          "audioUrl": null,
          "matchTarget": null
        },
        {
          "id": "uuid",
          "text": "Dog",
          "imageUrl": null,
          "audioUrl": null,
          "matchTarget": null
        }
      ]
    }
  ]
}
```

**Question Types:**
- `TEXT_SELECT` - выбор текстового ответа
- `IMAGE_SELECT` - выбор картинки
- `AUDIO_SELECT` - выбор после прослушивания аудио
- `DRAG_DROP_IMAGE` - сопоставление картинок со словами
- `FILL_BLANK` - заполнение пропуска

---

### POST /tests/{testId}/submit
Отправить ответы на тест.

**Path Parameters:**
- `testId` - UUID теста

**Request:**
```json
{
  "answers": {
    "question_uuid_1": ["answer_uuid_1"],
    "question_uuid_2": ["answer_uuid_2", "answer_uuid_3"],
    "question_uuid_3": {
      "answer_uuid_4": "target_word_1",
      "answer_uuid_5": "target_word_2"
    }
  },
  "timeSpentSeconds": 180
}
```

**Response:** `200 OK`
```json
{
  "score": 8,
  "maxScore": 10,
  "percentage": 80,
  "stars": 2,
  "pointsEarned": 18,
  "isNewBestScore": true,
  "levelUp": {
    "newLevel": 6,
    "newTitle": "Знаток"
  },
  "newAchievements": [
    {
      "id": "uuid",
      "code": "TESTS_10",
      "name": "Десятка",
      "description": "Пройдите 10 тестов",
      "icon": "🔟",
      "pointsReward": 100
    }
  ]
}
```

**Stars Calculation:**
- 3 звезды: 95%+
- 2 звезды: 80-94%
- 1 звезда: 60-79%
- 0 звёзд: <60%

---

## Leaderboard Endpoint

⚠️ **Legacy** (не используется speaking-клиентом, судьба решается владельцем).

### GET /leaderboard
Получить таблицу лидеров.

**Query Parameters:**
- `limit` (default: 10) - количество записей

**Response:** `200 OK`
```json
{
  "entries": [
    {
      "rank": 1,
      "userId": "uuid",
      "displayName": "Мария С.",
      "avatarUrl": "https://...",
      "level": 15,
      "totalPoints": 5280
    }
  ],
  "userRank": 42,
  "usersAbove": [
    { "rank": 41, "displayName": "Пётр К.", ... }
  ],
  "usersBelow": [
    { "rank": 43, "displayName": "Анна В.", ... }
  ]
}
```

---

## Admin Endpoints (legacy)

⚠️ **Legacy** (не используется speaking-клиентом, судьба решается владельцем).
Актуальный админ-API speaking-тренажёра — см. раздел «Speaking Trainer API → Admin» выше.
Исключение: `POST /admin/media/upload` и `DELETE /admin/media` активно используются
admin-web для загрузки видео/субтитров/обложек speaking-контента.

Все admin endpoints требуют роль `ADMIN`.

### GET /admin/tests
Получить все тесты (включая неопубликованные).

### GET /admin/tests/{testId}
Получить детали теста для редактирования (включая isCorrect у ответов).

### POST /admin/tests
Создать новый тест.

**Request:**
```json
{
  "categoryId": "uuid",
  "title": "Новый тест",
  "description": "Описание теста",
  "thumbnailUrl": "https://...",
  "difficulty": "MEDIUM",
  "pointsReward": 15,
  "timeLimitSeconds": 300,
  "isPublished": false,
  "displayOrder": 1,
  "questions": [
    {
      "type": "TEXT_SELECT",
      "text": "Вопрос?",
      "audioUrl": null,
      "imageUrl": null,
      "displayOrder": 0,
      "points": 1,
      "answers": [
        {
          "text": "Правильный ответ",
          "imageUrl": null,
          "audioUrl": null,
          "isCorrect": true,
          "displayOrder": 0,
          "matchTarget": null
        },
        {
          "text": "Неправильный ответ",
          "isCorrect": false,
          "displayOrder": 1
        }
      ]
    }
  ]
}
```

### PUT /admin/tests/{testId}
Обновить тест.

### DELETE /admin/tests/{testId}
Удалить тест.

---

### GET /admin/users
Получить список пользователей.

**Query Parameters:**
- `q` - поиск по имени/email
- `role` - фильтр по роли (USER, ADMIN)

### GET /admin/users/{userId}
Получить детали пользователя с полной статистикой.

---

### GET /admin/analytics
Получить общую аналитику.

**Response:** `200 OK`
```json
{
  "totalTests": 50,
  "totalUsers": 1250,
  "totalCompletions": 8450,
  "totalAchievements": 3200
}
```

### GET /admin/analytics/daily-activity
Получить дневную активность пользователей.

**Query Parameters:**
- `days` (default: 7) - количество дней

**Response:** `200 OK`
```json
[
  {
    "date": "2024-01-20",
    "newUsers": 12,
    "testsCompleted": 45,
    "achievementsEarned": 8
  }
]
```

### GET /admin/analytics/activity
Алиас для `/admin/analytics/daily-activity`.

---

### GET /admin/analytics/levels
Распределение пользователей по уровням.

**Response:** `200 OK`
```json
[
  { "level": 1, "users": 120 },
  { "level": 2, "users": 85 }
]
```

### GET /admin/analytics/popular-tests
Получить популярные тесты.

**Query Parameters:**
- `limit` (default: 5)

### GET /admin/analytics/recent-activity
Получить последнюю активность.

**Query Parameters:**
- `limit` (default: 10)

---

### GET /admin/settings
Получить настройки для admin панели.

**Response:** `200 OK`
```json
{
  "s3Endpoint": "https://s3.amazonaws.com",
  "s3Bucket": "sotospeak",
  "s3Region": "eu-central-1",
  "maxFileSize": "10MB",
  "maxRequestSize": "10MB",
  "corsAllowedOrigins": ["https://admin.sotospeak.app"]
}
```

---

### POST /admin/media/upload
Загрузить медиафайл. **Активно используется** speaking-админкой (видео, субтитры, обложки).

**Content-Type:** `multipart/form-data`

**Form Fields:**
- `file` - файл (видео до 200 МБ — лимит nginx `client_max_body_size 200m`)
- `folder` - папка (thumbnails, questions/images, questions/audio, answers/images, videos, subtitles)

**Response:** `200 OK`
```json
{
  "url": "https://s3.amazonaws.com/bucket/folder/filename.jpg"
}
```

### DELETE /admin/media
Удалить медиафайл.

**Query Parameters:**
- `url` - URL файла для удаления

---

## Error Responses

Все ошибки возвращаются в формате:
```json
{
  "error": "Error type",
  "message": "Описание ошибки"
}
```

**HTTP Status Codes:**
- `400` - Bad Request (невалидные данные)
- `401` - Unauthorized (требуется авторизация)
- `403` - Forbidden (недостаточно прав)
- `404` - Not Found (ресурс не найден)
- `500` - Internal Server Error
