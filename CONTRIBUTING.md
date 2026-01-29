# Contributing to FunnyEnglish

## Структура проекта

```
FunnyEnglish/
├── backend/                 # Spring Boot API (Kotlin)
│   └── src/main/kotlin/com/funnyenglish/
│       ├── controller/      # REST контроллеры
│       ├── service/         # Бизнес-логика
│       ├── repository/      # JPA репозитории
│       ├── entity/          # JPA сущности
│       ├── dto/             # Data Transfer Objects
│       ├── config/          # Конфигурация (Security, CORS, etc.)
│       └── security/        # JWT аутентификация
│
├── admin-web/               # React Admin Panel
│   └── src/
│       ├── pages/           # Страницы (Dashboard, Tests, Users, etc.)
│       ├── components/      # UI компоненты
│       ├── api/             # API клиент (axios)
│       ├── store/           # Zustand stores
│       └── types/           # TypeScript типы
│
├── composeApp/              # Compose Multiplatform UI
│   └── src/
│       ├── commonMain/      # Общий код
│       │   └── kotlin/com/funnyenglish/app/
│       │       ├── screens/     # UI экраны
│       │       ├── viewmodel/   # ViewModels
│       │       ├── theme/       # Темы (Material 3)
│       │       ├── di/          # Koin DI
│       │       └── components/  # Общие компоненты
│       ├── androidMain/     # Android-специфичный код
│       ├── iosMain/         # iOS-специфичный код
│       └── desktopMain/     # Desktop-специфичный код
│
├── shared/                  # KMP Shared Module
│   └── src/
│       ├── commonMain/      # Общий код
│       │   └── kotlin/com/funnyenglish/shared/
│       │       ├── api/         # API клиент (Ktor)
│       │       ├── model/       # Data models
│       │       └── platform/    # Platform expect declarations
│       ├── androidMain/     # Android implementations
│       ├── iosMain/         # iOS implementations
│       └── desktopMain/     # Desktop implementations
│
└── docs/                    # Документация
    └── API.md               # API документация
```

## Требования

- **JDK 17+**
- **Gradle 8.x**
- **Node.js 18+** (для admin-web)
- **Android Studio** (для Android разработки)
- **PostgreSQL 14+** (для backend)

## Локальный запуск

### 1. База данных

```bash
# Docker
docker run -d \
  --name funnyenglish-db \
  -e POSTGRES_DB=funnyenglish \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Или используйте существующий PostgreSQL
```

### 2. Backend

```bash
cd backend

# Настройте переменные окружения
export DATABASE_URL=jdbc:postgresql://localhost:5432/funnyenglish
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET=your-secret-key-min-32-chars
export ADMIN_EMAIL=admin@funnyenglish.app
export ADMIN_PASSWORD=admin123

# Запуск
./gradlew bootRun
```

Backend будет доступен на `http://localhost:8080`

### 3. Admin Web

```bash
cd admin-web
npm install
npm run dev
```

Admin панель будет доступна на `http://localhost:5173`

### 4. Mobile App (Desktop)

```bash
./gradlew :composeApp:run
```

### 5. Mobile App (Android)

Откройте проект в Android Studio и запустите `composeApp` на эмуляторе или устройстве.

## Код стайл

### Kotlin

- Следуйте [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Используйте `ktlint` для форматирования
- Именование: `camelCase` для функций/переменных, `PascalCase` для классов

### TypeScript/React

- Используйте функциональные компоненты с хуками
- TypeScript strict mode
- Prettier для форматирования

### Compose

- Composable функции с заглавной буквы: `@Composable fun MyScreen()`
- State hoisting: поднимайте состояние выше
- Preview для всех экранов

## Git Workflow

### Ветки

- `main` - production-ready код
- `develop` - текущая разработка
- `feature/*` - новые фичи
- `fix/*` - исправления багов
- `hotfix/*` - срочные исправления в production

### Процесс разработки новой фичи

1. **Создать ветку от develop:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/my-feature
   ```

2. **Разработка:**
   - Пишите код
   - Делайте атомарные коммиты
   - Запускайте тесты локально

3. **Коммит изменений:**
   ```bash
   git add .
   git commit -m "feat(scope): description"
   ```

4. **Push и создание PR:**
   ```bash
   git push -u origin feature/my-feature
   # Создать Pull Request в develop
   ```

5. **После ревью - merge в develop:**
   ```bash
   git checkout develop
   git pull origin develop
   git merge feature/my-feature
   git push origin develop
   ```

6. **Удалить feature ветку:**
   ```bash
   git branch -d feature/my-feature
   git push origin --delete feature/my-feature
   ```

### Коммиты

Формат: `type(scope): description`

Типы:
- `feat` - новая функциональность
- `fix` - исправление бага
- `refactor` - рефакторинг
- `docs` - документация
- `test` - тесты
- `chore` - сборка, зависимости

Примеры:
```
feat(mobile): add bottom navigation
fix(backend): fix JWT null pointer exception
docs(api): add API documentation
test(backend): add AuthService unit tests
```

### Pull Request

1. Создайте ветку от `develop`
2. Сделайте изменения
3. Убедитесь что тесты проходят
4. Создайте PR в `develop`
5. Дождитесь code review

## Тестирование

### Backend

```bash
cd backend
./gradlew test
```

### Admin Web

```bash
cd admin-web
npm run test
```

### Mobile

```bash
./gradlew :composeApp:desktopTest
./gradlew :shared:allTests
```

## API Изменения

При изменении API:

1. Обновите backend контроллер/DTO
2. Обновите `shared/api/FunnyEnglishApi.kt`
3. Обновите `admin-web/src/api/client.ts`
4. Обновите `docs/API.md`

## Добавление нового экрана (Mobile)

1. Создайте `NewScreen.kt` в `composeApp/src/commonMain/.../screens/`
2. Создайте `NewViewModel.kt` в `viewmodel/` (если нужно)
3. Зарегистрируйте ViewModel в `di/AppModule.kt`
4. Добавьте route в `App.kt` (sealed class AppScreen)
5. Добавьте навигацию в `MainAppContent`

## Добавление нового endpoint (Backend)

1. Создайте/обновите DTO в `dto/`
2. Добавьте метод в Service
3. Добавьте endpoint в Controller
4. Добавьте в `SecurityConfig` если нужны особые права
5. Обновите API документацию

## Вопросы

Если есть вопросы, создайте Issue в репозитории.
