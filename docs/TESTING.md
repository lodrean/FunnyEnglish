# So to Speak Testing Guide

Руководство по тестированию приложения So to Speak с использованием Docker.

## Быстрый старт

```bash
# Запуск всех тестов
./scripts/run-tests.sh all

# Только unit-тесты
./scripts/run-tests.sh unit

# Только интеграционные тесты
./scripts/run-tests.sh integration
```

На Windows:
```powershell
.\scripts\run-tests.ps1 all
# или
.\scripts\run-tests.bat all
```

---

## Архитектура тестирования

### Unit-тесты
- **База данных**: H2 (встроенная, in-memory)
- **Профиль Spring**: `test`
- **Конфигурация**: `application-test.yml`
- **Цель**: Тестирование бизнес-логики без внешних зависимостей

### Интеграционные тесты
- **База данных**: PostgreSQL (контейнер)
- **S3 хранилище**: MinIO (контейнер)
- **Профиль Spring**: `integration-test`
- **Конфигурация**: `application-integration-test.yml`
- **Цель**: Тестирование API и взаимодействия с внешними сервисами

---

## Docker Compose файлы

| Файл | Назначение |
|------|------------|
| `docker-compose.test.yml` | Запуск unit-тестов с H2 БД |
| `docker-compose.integration-test.yml` | Запуск интеграционных тестов с PostgreSQL + MinIO |
| `docker-compose.dev.yml` | Инфраструктура для разработки |
| `docker-compose.staging.yml` | Изолированное тестовое окружение для приёмки MVP (свои БД/MinIO, Mailpit, ddl-auto=validate). Подробнее — секция «Staging» в `DOCKER.md`; seed-контент: `scripts/seed-speaking-content.sh` |

## Приёмочное тестирование MVP

Сценарий go/no-go владельца (ученик + учитель, прогон на staging) — `docs/qa/MVP_ACCEPTANCE.md`.
Admin-креды для E2E против не-dev окружений: `TEST_ADMIN_EMAIL`/`TEST_ADMIN_PASSWORD`
(admin-web e2e) и `--env-var admin_password=...` (Newman); staging — `docker/.env.staging`.

---

## Запуск тестов вручную

### Unit-тесты

```bash
# Сборка и запуск
docker compose -f docker/docker-compose.test.yml up --build --abort-on-container-exit

# Только пересборка без запуска
docker compose -f docker/docker-compose.test.yml build test-runner

# Запуск без пересборки
docker compose -f docker/docker-compose.test.yml run --rm test-runner
```

### Интеграционные тесты

```bash
# Полный цикл: запуск + тесты + очистка
docker compose -f docker/docker-compose.integration-test.yml up --build --abort-on-container-exit
docker compose -f docker/docker-compose.integration-test.yml down -v

# Запуск без очистки (для отладки)
docker compose -f docker/docker-compose.integration-test.yml up --build

# Остановка и очистка
docker compose -f docker/docker-compose.integration-test.yml down -v
```

---

## E2E: WASM-приложение (e2e-cmp)

Playwright-тесты speaking-клиента (Compose Multiplatform, wasmJs). Подробнее — `e2e-cmp/README.md`.

**Ключевая особенность — canvas-only:** CMP 1.7.1 (wasmJs) рендерит всё в единственный `<canvas>`,
семантика и testTag'и в DOM **не экспонируются**. Поэтому:
- тесты — **координатные клики** (калиброванные позиции в `e2e-cmp/tests/helpers.ts`, viewport 1280x720)
  + pixel-diff скриншоты clipped-регионов + assertion'ы на отсутствие console.error и HTTP 5xx к `/api`;
- на проекте **Mobile Chrome координатные тесты скипаются** (хелпер `skipOnMobile`).

**Запуск** (dev-сервер уже поднят на 8081):

```bash
cd e2e-cmp
npm install && npx playwright install

# против уже запущенного dev-сервера (composeApp wasmJs на 8081)
SKIP_WEB_SERVER=true npx playwright test

# или пусть Playwright сам поднимет webServer (без SKIP_WEB_SERVER)
npx playwright test
```

**Known issue:** gradle wasm-задачи (`wasmJsBrowserDevelopmentRun` и др.) требуют
`--no-configuration-cache` — известная проблема сериализации config cache:

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun --no-configuration-cache
```

## E2E: Admin-web (admin-web/e2e)

**Грабля портов:** `vite.config.ts` — порт **3000**, а `playwright.config.ts` (webServer/baseURL)
ожидает **5173**. Два рабочих варианта:

```bash
cd admin-web

# Вариант 1: dev-сервер на 5173 (под конфиг Playwright)
npm run dev -- --port 5173 --strictPort
SKIP_WEB_SERVER=1 npx playwright test

# Вариант 2: против docker-админки (порт 3000)
ADMIN_URL=http://localhost:3000 SKIP_WEB_SERVER=1 npx playwright test
```

## API-коллекция (api-tests, Newman)

Postman-коллекция `api-tests/sotospeak-api-collection.json` + раннер `run-tests.js`
(окружения: `local` → :8080, `test` → :8081, `staging`):

```bash
cd api-tests
npm install --no-save newman
node run-tests.js                 # TEST_ENV=local (default)
TEST_ENV=test node run-tests.js   # против test-окружения
```

Также в `api-tests/` — PowerShell-скрипты `integration-tests.ps1`, `e2e-backend-tests.ps1`.

---

## Получение отчетов

### Автоматически

```bash
./scripts/get-test-reports.sh
```

### Вручную

```bash
# Создать директорию
mkdir -p test-reports

# Извлечь отчеты из контейнера
docker cp sotospeak-test-runner:/app/backend/build/reports test-reports/

# Открыть HTML отчет
open test-reports/tests/test/index.html  # macOS
xdg-open test-reports/tests/test/index.html  # Linux
start test-reports/tests/test/index.html  # Windows
```

---

## Тестовые данные

### Unit-тесты (H2)
- Автоматически создаются таблицы при старте
- Данные удаляются после каждого теста (`ddl-auto: create-drop`)
- Flyway отключен

### Интеграционные тесты (PostgreSQL)
- База данных: `sotospeak_integration`
- Миграции Flyway применяются автоматически
- Данные сохраняются между запусками (volume)

---

## Настройка окружения

### Переменные окружения

| Переменная | Значение по умолчанию | Описание |
|------------|----------------------|----------|
| `DB_HOST` | postgres-test | Хост PostgreSQL |
| `DB_PORT` | 5432 | Порт PostgreSQL |
| `DB_NAME` | sotospeak_test | Имя БД |
| `JWT_SECRET` | test-jwt-secret... | JWT ключ |
| `S3_ENDPOINT` | http://minio-test:9000 | MinIO endpoint |

### Локальные порты

| Сервис | Порт | Описание |
|--------|------|----------|
| PostgreSQL (test) | 5433 | Тестовая БД |
| MinIO API (test) | 9010 | S3 API |
| MinIO Console (test) | 9011 | Web консоль |

---

## Примеры тестов

### Пример Unit-теста (Kotlin)

```kotlin
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    lateinit var userService: UserService

    @Test
    fun `should create user`() {
        // Given
        val request = CreateUserRequest(
            email = "test@test.com",
            password = "password123"
        )

        // When
        val user = userService.createUser(request)

        // Then
        assertNotNull(user.id)
        assertEquals("test@test.com", user.email)
    }
}
```

### Пример API теста (cURL)

```bash
# Health check
curl http://localhost:8080/api/health

# Login (Demo User)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@sotospeak.app","password":"demo123"}'
```

---

## Отладка тестов

### Просмотр логов

```bash
# Логи контейнера с тестами
docker logs -f sotospeak-test-runner

# Логи backend при интеграционных тестах
docker logs -f sotospeak-backend-integration

# Логи PostgreSQL
docker logs -f sotospeak-postgres-test
```

### Интерактивный режим

```bash
# Войти в контейнер для отладки
docker compose -f docker/docker-compose.test.yml run --rm test-runner sh

# Запустить конкретный тест
./gradlew :backend:test --tests "UserServiceTest.should create user" --no-daemon
```

---

## CI/CD интеграция

### GitHub Actions пример

```yaml
name: Tests

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run unit tests
        run: ./scripts/run-tests.sh unit
      
      - name: Upload reports
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-reports
          path: backend/build/reports/tests/

  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run integration tests
        run: ./scripts/run-tests.sh integration
      
      - name: Upload reports
        uses: actions/upload-artifact@v4
        with:
          name: integration-test-reports
          path: backend/build/reports/tests/
```

---

## Устранение неполадок

### Контейнеры не запускаются

```bash
# Проверить статус
docker compose -f docker/docker-compose.test.yml ps

# Пересобрать без кэша
docker compose -f docker/docker-compose.test.yml build --no-cache

# Очистить volumes
docker compose -f docker/docker-compose.test.yml down -v
```

### Порт уже занят

```bash
# Найти процесс на порту 5433
lsof -i :5433  # macOS/Linux
netstat -ano | findstr 5433  # Windows

# Использовать другой порт в docker-compose.yml
```

### Gradle daemon проблемы

```bash
# Очистить Gradle daemon
./gradlew --stop
docker volume rm sotospeak_test_gradle_cache
```

---

## Полезные команды

```bash
# Очистка всех тестовых контейнеров и volumes
docker compose -f docker/docker-compose.test.yml down -v
docker compose -f docker/docker-compose.integration-test.yml down -v

# Удалить все неиспользуемые volumes
docker volume prune

# Пересборка образа без кэша
docker build --no-cache -f docker/Dockerfile.test ..
```
