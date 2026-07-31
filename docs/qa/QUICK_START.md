# Быстрый старт: QA Автоматизация для FunnyEnglish

## 🚀 MVP QA Stack (минимум для старта)

```
Unit Tests          →  Kotest (shared module)
API Tests          →  Postman + Newman
UI Tests           →  Compose Desktop Tests  
E2E Flows          →  Maestro
Visual Testing     →  Paparazzi (screenshots)
CI/CD              →  GitHub Actions
```

---

## 1. Unit Tests (Kotest) - 15 минут настройки

### Добавь в shared/build.gradle.kts:

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation("io.kotest:kotest-framework-engine:5.8.0")
            implementation("io.kotest:kotest-assertions-core:5.8.0")
            implementation("io.kotest:kotest-property:5.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
    }
}
```

### Создай первый тест:

```kotlin
// shared/src/commonTest/kotlin/com/funnyenglish/shared/GameLogicTest.kt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GameLogicTest : FunSpec({
    
    test("calculateStars returns 3 for 100% score") {
        val result = calculateStars(score = 30, maxScore = 30)
        result shouldBe 3
    }
    
    test("calculateStars returns 0 for score below 50%") {
        val result = calculateStars(score = 10, maxScore = 30)
        result shouldBe 0
    }
})
```

### Запуск:
```bash
./gradlew :shared:test
```

---

## 2. API Tests (Postman + Newman) - 20 минут

### Установи Newman:
```bash
npm install -g newman
npm install -g newman-reporter-htmlextra
```

### Создай коллекцию Postman (`api-tests.json`):

```json
{
  "info": { "name": "FunnyEnglish API Tests" },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [{ "key": "Content-Type", "value": "application/json" }],
        "url": "{{baseUrl}}/auth/login",
        "body": {
          "mode": "raw",
          "raw": "{\"email\":\"demo@funnyenglish.app\",\"password\":\"demo123\"}"
        }
      },
      "event": [{
        "listen": "test",
        "script": {
          "exec": [
            "pm.test('Status 200', () => pm.response.to.have.status(200));",
            "pm.test('Has token', () => pm.expect(pm.response.json().token).to.exist);",
            "pm.environment.set('token', pm.response.json().token);"
          ]
        }
      }]
    },
    {
      "name": "Get Categories",
      "request": {
        "method": "GET",
        "header": [{ "key": "Authorization", "value": "Bearer {{token}}" }],
        "url": "{{baseUrl}}/categories"
      }
    }
  ]
}
```

### Запуск:
```bash
newman run api-tests.json --env-var baseUrl=http://localhost:8080
```

---

## 3. Compose UI Tests - 20 минут

### Добавь в composeApp/build.gradle.kts:

```kotlin
kotlin {
    sourceSets {
        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("org.jetbrains.compose.ui:ui-test-junit4-desktop:1.6.0")
        }
    }
}
```

### Создай тест:

```kotlin
// composeApp/src/desktopTest/kotlin/TestPlayScreenTest.kt
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class TestPlayScreenTest {
    @get:Rule
    val compose = createComposeRule()
    
    @Test
    fun `selecting answer shows next button`() {
        compose.setContent {
            TestPlayScreen(
                question = Question(
                    text = "What is red?",
                    answers = listOf(
                        Answer(text = "Красный"),
                        Answer(text = "Синий")
                    )
                )
            )
        }
        
        compose.onNodeWithText("Красный").performClick()
        compose.onNodeWithText("Далее").assertExists()
    }
}
```

### Запуск:
```bash
./gradlew :composeApp:testDesktop
```

---

## 4. Maestro E2E Tests - 10 минут

### Установи Maestro:
```bash
curl -Ls "https://get.maestro.mobile.dev" | bash
```

### Создай flow (`.maestro/flows/login.yaml`):

```yaml
appId: com.funnyenglish.app
---
- launchApp
- tapOn: "Email"
- inputText: "demo@funnyenglish.app"
- tapOn: "Password"  
- inputText: "demo123"
- tapOn: "Войти"
- assertVisible: "Категории"
```

### Запуск:
```bash
maestro test .maestro/flows/login.yaml
```

### Запись flow (интерактивно):
```bash
maestro record .maestro/flows/new-flow.yaml
```

---

## 5. Visual Regression (Paparazzi) - 20 минут

### Добавь в composeApp/build.gradle.kts:

```kotlin
plugins {
    id("app.cash.paparazzi") version "1.3.1"
}
```

### Создай screenshot тест:

```kotlin
// composeApp/src/test/kotlin/ScreenshotTest.kt
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi()
    
    @Test
    fun `test play screen`() {
        paparazzi.snapshot {
            TestPlayScreen(
                question = Question(text = "Test question")
            )
        }
    }
}
```

### Запуск:
```bash
# Создать baseline скриншоты
./gradlew :composeApp:recordPaparazziDebug

# Сравнить с baseline
./gradlew :composeApp:verifyPaparazziDebug
```

---

## 6. GitHub Actions CI - 15 минут

### Создай `.github/workflows/qa.yml`:

```yaml
name: QA

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Unit Tests
        run: ./gradlew :shared:test
      
      - name: Upload Report
        uses: codecov/codecov-action@v3

  api-tests:
    runs-on: ubuntu-latest
    needs: unit-tests
    steps:
      - uses: actions/checkout@v3
      
      - name: Start Backend
        run: |
          docker compose -f docker/docker-compose.dev.yml up -d
          ./gradlew :backend:bootRun &
          sleep 30
      
      - name: API Tests
        run: |
          npm install -g newman
          newman run api-tests.json

  maestro-tests:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Install Maestro
        run: curl -Ls "https://get.maestro.mobile.dev" | bash
      
      - name: Run Flows
        run: maestro test .maestro/flows/
```

---

## 📊 AI QA Агент (продвинутый уровень)

### Docker контейнер для QA агента:

```dockerfile
# Dockerfile.qa-agent
FROM python:3.11-slim

RUN pip install \
    opencv-python \
    scikit-image \
    transformers \
    torch \
    playwright

COPY qa_agent/ /app/
WORKDIR /app

ENTRYPOINT ["python", "main.py"]
```

### Запуск агента:

```bash
# Сборка
docker build -t funnyenglish-qa -f Dockerfile.qa-agent .

# Сравнение скриншотов с дизайном
docker run --rm \
  -v $(pwd)/screenshots:/screenshots \
  -v $(pwd)/designs:/designs \
  -e OPENAI_API_KEY=$OPENAI_API_KEY \
  funnyenglish-qa \
  compare --designs=/designs --screenshots=/screenshots
```

---

## ✅ Чек-лист внедрения

### Неделя 1: Базовое покрытие
- [ ] Unit tests для `calculateScore`, `scheduleReview`
- [ ] API tests для `/auth/login`, `/tests/submit`
- [ ] CI pipeline с запуском тестов

### Неделя 2: UI покрытие  
- [ ] Compose tests для критических экранов
- [ ] Maestro flow для "happy path"
- [ ] Screenshot tests с Paparazzi

### Неделя 3: Интеграция
- [ ] Все тесты в CI
- [ ] Coverage reports (Codecov)
- [ ] PR checks обязательны

---

## 🛠️ Полезные команды

```bash
# Запуск всех тестов
./gradlew test

# Только unit tests
./gradlew :shared:test

# С coverage
./gradlew :shared:koverXmlReport

# API tests
newman run api-tests.json -r cli,htmlextra

# Maestro
maestro test flows/                    # все flows
maestro test flows/login.yaml          # один flow
maestro record flows/new.yaml          # запись

# Visual testing
./gradlew recordPaparazziDebug         # создать baseline
./gradlew verifyPaparazziDebug         # проверить
```

---

## 📈 Метрики качества

| Метрика | Целевое значение | Инструмент |
|---------|------------------|------------|
| Unit test coverage | > 70% | Kover |
| API test coverage | > 90% endpoints | Postman |
| UI test stability | > 95% | Compose Test |
| E2E success rate | > 90% | Maestro |
| Visual diff | < 1% | Paparazzi |
| CI pipeline time | < 10 min | GitHub Actions |

---

## 🎯 Следующие шаги

После MVP QA:
1. **Mutation testing** - Pitest для Java/Kotlin
2. **Contract testing** - Pact для API
3. **Chaos testing** - Gremlin для resilience
4. **AI visual testing** - собственный агент
5. **Performance testing** - JMeter или Gatling

---

Готово к запуску! 🚀
