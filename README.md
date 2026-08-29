# So to Speak

Кроссплатформенный **тренажёр устной английской речи**: ученик смотрит видео по теме, отвечает на вопросы голосом, а учитель проверяет записи и ставит оценку по рубрике.

## Как это работает

**Флоу ученика:** Библиотека тем (Library) → Топик → Видео (с субтитрами WebVTT или без) → Вопросы → один из двух режимов:

- **Training** — локальная тренировка: ровно 3 попытки на топик с эскалацией таймера (80с → 50с → 30с). Записи **не покидают устройство**, их можно только прослушать. Доступен гостю без регистрации.
- **Practice** — контрольная точка: одна запись на все вопросы за 30 секунд, без review — автоматически уходит учителю на сервер. Требует авторизации.

**Флоу учителя (admin-web):** управление контентом (темы, топики, видео, субтитры, вопросы) и **Grading** — inbox записей с прослушиванием и оценкой по рубрике: grammar / vocabulary / pronunciation / fluency (1–10 каждый) + общий балл + комментарий.

## Возможности

- **Guest-first**: видео и Training доступны без регистрации; регистрация требуется только при входе в Practice
- **Видеоплеер** с субтитрами WebVTT (Media3 на Android), fullscreen-режим
- **Голосовые ответы** с записью, таймером и offline-retry отправки
- **Рубричное оценивание** учителем (4 критерия × 1–10 + комментарий)
- **Админ-панель**: контент Speaking, Grading, пользователи, аналитика
- **Темы оформления**: светлая, тёмная, системная (дизайн-система Playful Coach)

## Технологии

| Компонент | Стек |
|-----------|------|
| **Backend** | Kotlin, Spring Boot 3, PostgreSQL + Flyway, JWT, S3/MinIO (видео и аудио) |
| **Mobile/Desktop/Web** | Kotlin Multiplatform, Compose Multiplatform (Android/Desktop/WASM), Ktor, Koin |
| **Admin** | React 18, TypeScript, Material UI, TanStack Query |

## Структура проекта

```
So to Speak/
├── backend/          # Spring Boot API (context-path /api)
├── admin-web/        # React админ-панель (Speaking / Grading / Users / Analytics)
├── composeApp/       # Основной KMP-модуль UI (android/desktop/wasmJs)
├── app/              # Тонкая Android-обёртка
├── shared/           # KMP shared модуль (API-клиент, модели)
└── docs/             # Документация
```

## Быстрый старт

### Требования

- Docker + Docker Compose
- Android Studio (для Android)

### Docker (всё вместе)

```bash
# Запуск всех сервисов
docker compose up -d
```

Сервисы будут доступны:
| Сервис | URL | Учетные данные |
|--------|-----|----------------|
| Admin Panel | http://localhost:3000 | `admin@sotospeak.com` / `admin123` |
| Backend API | http://localhost:8080/api | - |
| MinIO Console | http://localhost:9001 | `minioadmin` / `minioadmin` |

Все учетные данные: [CREDENTIALS.md](CREDENTIALS.md)

```bash
# Остановка
docker compose down

# Пересборка
docker compose up -d --build
```

### Desktop

```bash
./gradlew :composeApp:run
```

### Android

```bash
./gradlew :app:assembleDebug
```

Или откройте проект в Android Studio и запустите `app`.

## Демо-аккаунт

```
Email: demo@sotospeak.app
Password: demo123
```

## Конфигурация

### Backend (application.yml)

| Переменная | Описание | Default |
|------------|----------|---------|
| `DATABASE_URL` | JDBC URL | - |
| `DATABASE_USERNAME` | DB user | - |
| `DATABASE_PASSWORD` | DB password | - |
| `JWT_SECRET` | JWT signing key (min 32 chars) | - |
| `JWT_EXPIRATION` | Token expiration (ms) | 86400000 |
| `ADMIN_EMAIL` | Admin email | admin@sotospeak.com |
| `ADMIN_PASSWORD` | Admin password | - |
| `AWS_ACCESS_KEY` | S3 access key | - |
| `AWS_SECRET_KEY` | S3 secret key | - |
| `AWS_BUCKET` | S3 bucket name | - |
| `AWS_REGION` | S3 region | eu-central-1 |

### Mobile (gradle.properties)

```properties
SOTOSPEAK_API_BASE_URL=http://10.0.2.2:8080
```

Для физического устройства используйте IP компьютера в локальной сети.

## API

Полная документация: [docs/API.md](docs/API.md). Базовый путь: `/api`.

### Основные endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | /auth/register | Регистрация |
| POST | /auth/login | Вход |
| GET | /users/me | Текущий пользователь |
| GET | /public/speaking/libraries | Опубликованные темы |
| GET | /public/speaking/topics/{id} | Топик с видео и вопросами |
| POST | /speaking/submissions | Отправка practice-записи (multipart) |
| GET | /admin/speaking/* | Управление контентом (учитель) |

## Документация

- [PRD: Speaking-тренажёр](docs/prd/SPEAKING-TRAINER-001.prd.md) — требования продукта
- [API Documentation](docs/API.md) — REST API
- [Architecture](docs/ARCHITECTURE.md) — архитектура системы
- [User Guide](docs/USER_GUIDE.md) — руководство (ученик + учитель)
- [Testing](docs/TESTING.md) — runbook'и тестирования
- [Contributing](CONTRIBUTING.md) — как внести вклад

## Разработка

### Запуск тестов

```bash
# Backend
./gradlew :backend:test

# KMP (desktop target)
./gradlew :composeApp:desktopTest

# Admin Web (unit)
npm --prefix admin-web exec vitest run

# Admin Web (E2E, Playwright)
npm --prefix admin-web run test:e2e

# Mobile E2E (Maestro, нужен эмулятор)
maestro test .maestro/flows/

# API (Newman, против живого стека)
newman run api-tests/sotospeak-api-collection.json
```

### Сборка

```bash
# Backend JAR
./gradlew :backend:bootJar

# Admin Web
npm --prefix admin-web run build

# Android APK
./gradlew :app:assembleDebug
```

## Roadmap

- [x] Пивот продукта: Speaking-тренажёр (видео + голосовые ответы)
- [x] Training (3 попытки, локальные записи) и Practice (отправка учителю)
- [x] Guest-first доступ и Practice-гейтинг
- [x] Админ-панель: контент Speaking + Grading по рубрике
- [x] Дизайн-система Playful Coach
- [x] Email-верификация (staging/prod)
- [ ] OAuth (Google, VK, Telegram) — отключён до реализации верификации токенов
- [ ] Push-уведомления
- [ ] iOS

## Лицензия

MIT License - см. [LICENSE](LICENSE)
