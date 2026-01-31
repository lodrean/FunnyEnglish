# FunnyEnglish API Documentation

## Base URL
```
Development: http://localhost:8080
Production: https://api.funnyenglish.app
```

## Authentication

Все защищённые endpoints требуют JWT токен в заголовке:
```
Authorization: Bearer <token>
```

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

**Errors:**
- `400` - Email уже существует
- `400` - Невалидные данные

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

---

### POST /auth/refresh
Обновить access-токен по refresh-токену.

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

## Admin Endpoints

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
  "s3Bucket": "funnyenglish",
  "s3Region": "eu-central-1",
  "maxFileSize": "10MB",
  "maxRequestSize": "10MB",
  "corsAllowedOrigins": ["https://admin.funnyenglish.app"]
}
```

---

### POST /admin/media/upload
Загрузить медиафайл.

**Content-Type:** `multipart/form-data`

**Form Fields:**
- `file` - файл
- `folder` - папка (thumbnails, questions/images, questions/audio, answers/images)

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
