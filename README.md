# FunnyEnglish

Кроссплатформенное приложение для изучения английского языка с геймификацией.

## Возможности

- **Тесты разных типов**: выбор текста, картинок, аудио, перетаскивание, заполнение пропусков
- **Система прогресса**: очки, уровни, звёзды (1-3 за тест)
- **Достижения**: награды за активность и успехи
- **Таблица лидеров**: соревнуйтесь с другими пользователями
- **Streak**: отслеживание ежедневной активности
- **Темы**: светлая, тёмная, системная
- **Админ-панель**: управление тестами, пользователями, аналитика

## Скриншоты

| Главная | Тест | Результат | Профиль |
|---------|------|-----------|---------|
| ![Home](docs/screenshots/home.png) | ![Test](docs/screenshots/test.png) | ![Result](docs/screenshots/result.png) | ![Profile](docs/screenshots/profile.png) |

## Технологии

| Компонент | Стек |
|-----------|------|
| **Backend** | Kotlin, Spring Boot 3, PostgreSQL, JWT, S3 |
| **Mobile** | Kotlin Multiplatform, Compose Multiplatform, Ktor, Koin |
| **Admin** | React 18, TypeScript, Material UI, TanStack Query |

## Структура проекта

```
FunnyEnglish/
├── backend/          # Spring Boot API
├── admin-web/        # React админ-панель
├── composeApp/       # Compose Multiplatform UI
├── shared/           # KMP shared модуль (API, models)
└── docs/             # Документация
```

## Быстрый старт

### Требования

- JDK 17+
- Node.js 18+
- PostgreSQL 14+
- Android Studio (для Android)

### 1. База данных

```bash
# Через Docker
docker run -d \
  --name funnyenglish-db \
  -e POSTGRES_DB=funnyenglish \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### 2. Backend

```bash
# Из корня проекта (bash)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=funnyenglish
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key-minimum-32-characters
export ADMIN_EMAIL=admin@funnyenglish.app
export ADMIN_PASSWORD=admin123

./gradlew -p backend bootRun
```

```powershell
# Из корня проекта (PowerShell)
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "funnyenglish"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:JWT_SECRET = "your-secret-key-minimum-32-characters"
$env:ADMIN_EMAIL = "admin@funnyenglish.app"
$env:ADMIN_PASSWORD = "admin123"

./gradlew -p backend bootRun
```

Backend: `http://localhost:8080`

### 3. Admin Web

```bash
cd admin-web
npm install
npm run dev
```

Admin: `http://localhost:5173`

Вход: `admin@funnyenglish.app` / `admin123`

### 4. Mobile (Desktop)

```bash
./gradlew :composeApp:run
```

### 5. Mobile (Android)

Откройте проект в Android Studio и запустите `composeApp`.

## Демо-аккаунт

```
Email: demo@funnyenglish.app
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
| `ADMIN_EMAIL` | Admin email | admin@funnyenglish.app |
| `ADMIN_PASSWORD` | Admin password | - |
| `AWS_ACCESS_KEY` | S3 access key | - |
| `AWS_SECRET_KEY` | S3 secret key | - |
| `AWS_BUCKET` | S3 bucket name | - |
| `AWS_REGION` | S3 region | eu-central-1 |

### Mobile (gradle.properties)

```properties
FUNNYENGLISH_API_BASE_URL=http://10.0.2.2:8080
```

Для физического устройства используйте IP компьютера в локальной сети.

## API

Полная документация: [docs/API.md](docs/API.md)

### Основные endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | /auth/register | Регистрация |
| POST | /auth/login | Вход |
| GET | /users/me | Текущий пользователь |
| GET | /categories | Список категорий |
| GET | /tests/{id} | Детали теста |
| POST | /tests/{id}/submit | Отправить ответы |
| GET | /leaderboard | Таблица лидеров |

## Документация

- [API Documentation](docs/API.md) - REST API
- [Architecture](docs/ARCHITECTURE.md) - Архитектура системы
- [Contributing](CONTRIBUTING.md) - Как внести вклад

## Разработка

### Запуск тестов

```bash
# Backend
./gradlew -p backend test

# Admin Web
npm --prefix admin-web test

# Mobile
./gradlew :shared:allTests
```

### Сборка

```bash
# Backend JAR
./gradlew -p backend bootJar

# Admin Web
npm --prefix admin-web run build

# Android APK
./gradlew :composeApp:assembleDebug
```

## Roadmap

- [x] Базовая функциональность MVP
- [x] Админ-панель
- [x] Система достижений
- [x] Таблица лидеров
- [x] Темная тема
- [ ] Push-уведомления
- [ ] Офлайн-режим
- [ ] OAuth (Google, VK, Telegram)
- [ ] Голосовой ввод
- [ ] Multiplayer режим

## Лицензия

MIT License - см. [LICENSE](LICENSE)
