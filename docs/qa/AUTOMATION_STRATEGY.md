# Стратегия автоматизации QA для So to Speak

## 🎯 Цели автоматизации

1. **Сократить time-to-market** — быстрый feedback loop для разработчиков
2. **Повысить качество** — catch bugs before users do
3. **Снизить ручной труд** — освободить QA для исследовательского тестирования
4. **Обеспечить стабильность** — регрессионное тестирование при каждом изменении

---

## 📊 Уровни автоматизации (Testing Pyramid)

```
        /\
       /  \
      / E2E\          UI Tests (Compose Test, Maestro)
     /--------\       ~10% test coverage
    /          \
   / Integration \   API Tests, DB Tests
  /--------------\    ~20% test coverage
 /                \
/    Unit Tests    \  Business logic tests
/--------------------\ ~70% test coverage
```

---

## 🤖 AI-Powered QA Агент (Visual Testing)

### Концепция: "QA Агент с глазами"

Агент, который:
- Сравнивает скриншоты реализации с дизайном (Figma)
- Проверяет пользовательские flow автоматически
- Генерирует отчеты о визуальных regressions
- Сам обучается на примерах

### Архитектура QA Агента

```kotlin
// QA Agent Architecture

class QAVisualAgent(
    private val designComparator: DesignComparator,
    private val flowExecutor: UserFlowExecutor,
    private val reportGenerator: ReportGenerator
) {
    
    // 1. Сравнение с дизайном
    suspend fun compareWithDesign(
        screenName: String,
        implementationScreenshot: Screenshot,
        figmaDesign: FigmaFrame
    ): DesignComparisonReport {
        val layoutAnalysis = designComparator.analyzeLayout(
            implementation = implementationScreenshot,
            design = figmaDesign
        )
        
        return DesignComparisonReport(
            pixelMatch = layoutAnalysis.pixelMatchScore,
            componentPositions = layoutAnalysis.positionDeviations,
            colorAccuracy = layoutAnalysis.colorDifferences,
            typographyIssues = layoutAnalysis.fontMismatches,
            recommendations = generateRecommendations(layoutAnalysis)
        )
    }
    
    // 2. Автоматический flow тестинг
    suspend fun executeUserFlow(
        flow: UserFlow,
        appInstance: TestApp
    ): FlowExecutionReport {
        val results = flow.steps.map { step ->
            val screenshot = appInstance.captureScreen()
            val element = appInstance.findElement(step.target)
            
            element.interact(step.action)
            
            FlowStepResult(
                step = step,
                screenshot = screenshot,
                success = appInstance.verifyState(step.expectedState),
                executionTime = step.duration
            )
        }
        
        return FlowExecutionReport(
            flow = flow,
            steps = results,
            successRate = results.count { it.success } / results.size.toFloat(),
            criticalPathTime = results.sumOf { it.executionTime.inWholeMilliseconds }
        )
    }
}
```

---

## 🛠️ Инструменты для реализации

### 1. Unit Tests (Kotlin Multiplatform)

```kotlin
// shared/src/commonTest/kotlin/.../GameLogicTest.kt
class GameLogicTest {
    
    @Test
    fun `calculateScore returns correct stars for perfect result`() = runTest {
        // Given
        val answers = listOf(
            AnswerResult(correct = true),
            AnswerResult(correct = true),
            AnswerResult(correct = true)
        )
        
        // When
        val result = calculateScore(answers, maxScore = 30)
        
        // Then
        assertEquals(3, result.stars)
        assertEquals(30, result.score)
    }
    
    @Test
    fun `spaced repetition schedules next review correctly`() = runTest {
        val scheduler = SpacedRepetitionScheduler()
        val card = FlashCard(id = "1", interval = 1.days)
        
        val nextReview = scheduler.scheduleNext(
            card = card,
            result = ReviewResult.EASY
        )
        
        assertEquals(4.days, nextReview.interval) // FSRS algorithm
    }
}
```

### 2. Integration Tests (Backend)

```kotlin
// backend/src/test/kotlin/.../TestIntegrationTest.kt
@SpringBootTest
@AutoConfigureMockMvc
class TestIntegrationTest {
    
    @Autowired
    lateinit var mockMvc: MockMvc
    
    @Test
    fun `submit test results updates user progress`() {
        // Arrange
        val submitRequest = SubmitTestRequest(
            testId = "test-123",
            answers = listOf(
                SubmitAnswer(questionId = "q1", selectedAnswerIds = listOf("a1"))
            )
        )
        
        // Act & Assert
        mockMvc.post("/tests/test-123/submit") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
            content = objectMapper.writeValueAsString(submitRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.score") { exists() }
            jsonPath("$.stars") { value(greaterThanOrEqualTo(0)) }
            jsonPath("$.isNewBestScore") { isBoolean() }
        }
    }
}
```

### 3. UI Tests (Compose Desktop)

```kotlin
// composeApp/src/desktopTest/kotlin/.../TestPlayFlowTest.kt
class TestPlayFlowTest {
    @get:Rule
    val compose = createComposeRule()
    
    @Test
    fun `user can complete test and see results`() {
        // Arrange
        val testQuestions = listOf(
            Question(
                id = "q1",
                text = "What is 'red'?",
                answers = listOf(
                    Answer(id = "a1", text = "Красный"),
                    Answer(id = "a2", text = "Синий")
                )
            )
        )
        
        compose.setContent {
            TestPlayScreen(
                questions = testQuestions,
                onSubmit = { /* verify */ }
            )
        }
        
        // Act
        compose.onNodeWithText("Красный").performClick()
        compose.onNodeWithText("Далее").performClick()
        
        // Assert
        compose.onNodeWithText("Результаты").assertExists()
        compose.onNodeWithText("1/1 правильно").assertExists()
    }
}
```

### 4. Visual Regression Testing (AI-Powered)

```yaml
# .github/workflows/visual-regression.yml
name: Visual Regression Tests

on: [push, pull_request]

jobs:
  visual-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      # 1. Build and run app
      - name: Start Application
        run: |
          ./gradlew :composeApp:run &
          sleep 30
      
      # 2. Capture screenshots
      - name: Capture Screenshots
        run: |
          # Используем playwright или puppeteer для скриншотов
          npx playwright screenshot \
            --viewport-size=1280,720 \
            --wait-for-timeout=5000 \
            http://localhost:3000 \
            screenshots/home.png
      
      # 3. Compare with Figma (using Figma API + pixelmatch)
      - name: Compare with Design
        run: |
          node scripts/compare-screenshots.js \
            --design-url="${{ secrets.FIGMA_FILE_URL }}" \
            --screenshots-dir="./screenshots" \
            --threshold=0.1
      
      # 4. Upload results
      - name: Upload Report
        uses: actions/upload-artifact@v3
        with:
          name: visual-regression-report
          path: ./visual-report/
```

### 5. E2E Flow Testing (Maestro)

```yaml
# .maestro/flows/complete_lesson.yaml
appId: com.sotospeak.app
---
# Flow: Пользователь проходит урок от начала до конца

# 1. Login
- tapOn: "Email"
- inputText: "test@example.com"
- tapOn: "Password"
- inputText: "password123"
- tapOn: "Войти"
- assertVisible: "Категории"

# 2. Select category
- tapOn: "Цвета"
- assertVisible: "Цвета: Базовый"

# 3. Start test
- tapOn: "Цвета: Базовый"
- assertVisible: "Как будет \"красный\" по-английски?"

# 4. Answer questions
- tapOn: "Red"
- tapOn: "Далее"

- tapOn: "Blue"
- tapOn: "Далее"

# 5. Complete test
- tapOn: "Green"
- tapOn: "Завершить"

# 6. Verify results
- assertVisible: "Результаты"
- assertVisible: "3/3 правильно"
- tapOn: "Продолжить"

# 7. Check progress updated
- assertVisible: "Категории"
- assertVisible: "⭐ 3"  # 3 stars earned
```

---

## 🧠 AI Агент: Детальная реализация

### Компонент 1: Visual Diff Engine

```python
# qa_agent/visual_diff.py
import cv2
import numpy as np
from skimage.metrics import structural_similarity as ssim

class VisualDiffEngine:
    """
    Сравнивает скриншоты с использованием SSIM и AI (CLIP/ResNet)
    """
    
    def compare(self, screenshot_path: str, design_path: str) -> DiffReport:
        # Загружаем изображения
        screenshot = cv2.imread(screenshot_path)
        design = cv2.imread(design_path)
        
        # 1. Pixel-perfect comparison (для critical elements)
        pixel_diff = cv2.absdiff(screenshot, design)
        pixel_score = 1 - (np.sum(pixel_diff) / (pixel_diff.size * 255))
        
        # 2. Structural similarity (для layout)
        gray_screenshot = cv2.cvtColor(screenshot, cv2.COLOR_BGR2GRAY)
        gray_design = cv2.cvtColor(design, cv2.COLOR_BGR2GRAY)
        ssim_score = ssim(gray_screenshot, gray_design)
        
        # 3. AI-powered semantic comparison (CLIP)
        semantic_similarity = self._semantic_compare(screenshot, design)
        
        # 4. Component detection (YOLO или Detectron2)
        components = self._detect_components(screenshot)
        design_components = self._detect_components(design)
        
        # 5. Generate heatmap of differences
        diff_heatmap = self._generate_heatmap(pixel_diff)
        
        return DiffReport(
            pixel_score=pixel_score,
            ssim_score=ssim_score,
            semantic_score=semantic_similarity,
            component_matches=self._match_components(components, design_components),
            heatmap_path=diff_heatmap,
            issues=self._identify_issues(components, design_components)
        )
    
    def _semantic_compare(self, img1: np.ndarray, img2: np.ndarray) -> float:
        """Использует CLIP для семантического сравнения"""
        # Implementation using OpenAI CLIP or similar
        pass
```

### Компонент 2: Flow Learning Engine

```kotlin
// QA агент учится на действиях реальных пользователей
class FlowLearningEngine {
    
    private val userSessions: List<UserSession>
    private val mlModel: SequencePredictionModel
    
    // 1. Собираем данные о реальных пользовательских путях
    fun recordSession(session: UserSession) {
        userSessions.add(session)
    }
    
    // 2. ML модель предсказывает вероятные проблемные места
    fun predictProblemAreas(): List<RiskArea> {
        return mlModel.predict(
            input = userSessions.map { it.toFeatures() }
        )
    }
    
    // 3. Генерируем автоматические тесты на основе реальных flow
    fun generateSmartTests(): List<AutomatedTest> {
        val commonFlows = findCommonUserFlows(userSessions)
        
        return commonFlows.map { flow ->
            AutomatedTest(
                name = "Auto-generated: ${flow.name}",
                steps = flow.steps,
                priority = calculatePriority(flow),
                assertions = generateAssertions(flow)
            )
        }
    }
    
    // 4. Anomaly detection - находит странное поведение
    fun detectAnomalies(session: UserSession): List<Anomaly> {
        // Isolation Forest или LSTM Autoencoder
        return anomalyDetector.detect(session)
    }
}
```

### Компонент 3: Self-Healing Tests

```kotlin
// Тесты, которые сами адаптируются к изменениям UI
class SelfHealingTest {
    
    private val elementLibrary = ElementLibrary()
    
    suspend fun tapOn(elementDescription: String) {
        // 1. Пытаемся найти по точному локатору
        val element = findElement(By.id(elementDescription))
            ?: findElement(By.text(elementDescription))
        
        if (element != null) {
            element.tap()
            return
        }
        
        // 2. Если не нашли - используем AI для поиска похожего
        val screenshot = captureScreen()
        val similarElement = aiFinder.findSimilarElement(
            description = elementDescription,
            currentScreen = screenshot,
            elementLibrary = elementLibrary
        )
        
        if (similarElement != null) {
            // Обновляем локатор в библиотеке
            elementLibrary.updateLocator(
                oldDescription = elementDescription,
                newLocator = similarElement.locator,
                confidence = similarElement.confidence
            )
            similarElement.tap()
        } else {
            throw ElementNotFoundException(elementDescription)
        }
    }
}
```

---

## 🔄 CI/CD Pipeline для QA

```yaml
# .github/workflows/qa-pipeline.yml
name: QA Automation Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # 1. Fast Feedback (2-3 минуты)
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Unit Tests
        run: ./gradlew :shared:test :backend:test --parallel
      - name: Upload Coverage
        uses: codecov/codecov-action@v3

  # 2. Integration Tests (5-7 минут)
  integration-tests:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_PASSWORD: test
      minio:
        image: minio/minio
    steps:
      - name: Run Integration Tests
        run: ./gradlew :backend:integrationTest

  # 3. UI Tests (Desktop) (10 минут)
  ui-tests-desktop:
    runs-on: ubuntu-latest
    steps:
      - name: Run Compose Desktop Tests
        run: ./gradlew :composeApp:testDesktop

  # 4. Visual Regression (параллельно)
  visual-regression:
    runs-on: ubuntu-latest
    needs: [unit-tests]
    steps:
      - name: Run Visual Tests
        run: |
          docker run --rm \
            -v $(pwd)/screenshots:/screenshots \
            sotospeak-qa-agent \
            compare --baseline=/designs --current=/screenshots
      
      - name: Comment PR with Results
        uses: actions/github-script@v6
        with:
          script: |
            const report = require('./visual-report.json');
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: `## 🎨 Visual Regression Report\n\n${report.summary}`
            })

  # 5. E2E Flow Tests (15 минут)
  e2e-tests:
    runs-on: macos-latest  # Для iOS simulator
    steps:
      - name: Install Maestro
        run: curl -Ls "https://get.maestro.mobile.dev" | bash
      
      - name: Run E2E Tests
        run: maestro test .maestro/flows/
      
      - name: Upload Test Report
        uses: actions/upload-artifact@v3
        with:
          name: maestro-report
          path: ~/.maestro/tests/

  # 6. Performance Tests
  performance-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Run Performance Benchmarks
        run: ./gradlew :composeApp:benchmark
      
      - name: Compare with Baseline
        run: |
          node scripts/compare-performance.js \
            --baseline=./perf-baseline.json \
            --current=./perf-results.json

  # 7. Security Scan
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
      
      - name: SonarQube Analysis
        uses: sonarqube-quality-gate-action@master

  # 8. AI QA Agent Review
  ai-qa-review:
    runs-on: ubuntu-latest
    needs: [unit-tests, ui-tests-desktop]
    steps:
      - name: Run QA Agent
        env:
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
        run: |
          python qa_agent/main.py \
            --mode=review \
            --pr-number=${{ github.event.pull_request.number }} \
            --repo=${{ github.repository }}
```

---

## 📱 Практический пример: QA агент для So to Speak

### Сценарий 1: Сравнение с дизайном

```kotlin
// Пример использования QA агента
suspend fun main() {
    val qaAgent = QAVisualAgent(
        figmaToken = System.getenv("FIGMA_TOKEN"),
        openaiKey = System.getenv("OPENAI_API_KEY")
    )
    
    // 1. Запускаем приложение
    val app = TestApp.start()
    
    // 2. Навигируем к экрану
    app.navigateTo(Screen.TestPlay)
    
    // 3. Делаем скриншот
    val screenshot = app.captureScreen()
    
    // 4. Загружаем дизайн из Figma
    val design = qaAgent.loadFigmaFrame(
        fileId = "ABC123",
        frameId = "Test-Play-Screen"
    )
    
    // 5. Сравниваем
    val report = qaAgent.compareWithDesign(
        screenName = "TestPlay",
        implementation = screenshot,
        design = design
    )
    
    // 6. Выводим результаты
    println("Pixel Match: ${report.pixelMatch}%")
    println("Issues found: ${report.issues.size}")
    
    report.issues.forEach { issue ->
        println("❌ ${issue.severity}: ${issue.description}")
        println("   Location: ${issue.boundingBox}")
        println("   Suggestion: ${issue.suggestion}")
    }
    
    // 7. Генерируем HTML отчет
    report.generateHtmlReport("./qa-report.html")
}
```

### Сценарий 2: Автоматический тест flow

```kotlin
@Test
fun `QA agent verifies complete user journey`() = runTest {
    val agent = QAFlowAgent()
    
    // Определяем критический пользовательский путь
    val criticalFlow = UserFlow(
        name = "First Time User Journey",
        steps = listOf(
            FlowStep.Screenshot(name = "onboarding-start"),
            FlowStep.Tap(element = "Get Started"),
            FlowStep.Input(element = "Email", value = "test@test.com"),
            FlowStep.Input(element = "Password", value = "password123"),
            FlowStep.Tap(element = "Sign Up"),
            FlowStep.AssertVisible("Categories"),
            FlowStep.Tap(element = "Colors Category"),
            FlowStep.Screenshot(name = "test-list"),
            FlowStep.Tap(element = "First Test"),
            FlowStep.AnswerQuestion(answer = "Red"),
            FlowStep.Tap(element = "Submit"),
            FlowStep.AssertVisible("Results"),
            FlowStep.Screenshot(name = "results-screen")
        )
    )
    
    // Выполняем flow
    val result = agent.executeFlow(criticalFlow)
    
    // Проверяем метрики
    assertTrue(result.successRate >= 0.95f, "Flow success rate too low")
    assertTrue(result.criticalPathTime < 30_000, "Flow too slow")
    
    // Проверяем скриншоты на соответствие дизайну
    result.screenshots.forEach { screenshot ->
        val designMatch = agent.compareWithDesign(screenshot)
        assertTrue(designMatch.similarity > 0.90f, 
            "Screen ${screenshot.name} doesn't match design")
    }
}
```

---

## 🎓 Roadmap внедрения QA автоматизации

### Phase 1: Базовое покрытие (Неделя 1-2)
- [ ] Unit tests для core logic (70%+ coverage)
- [ ] API integration tests
- [ ] Простые Compose UI tests

### Phase 2: Визуальное тестирование (Неделя 3-4)
- [ ] Screenshot testing (Paparazzi)
- [ ] Сравнение с Figma (ручной скрипт)
- [ ] Baseline screenshots в git

### Phase 3: AI QA Агент (Неделя 5-8)
- [ ] Обучение модели на дизайнах
- [ ] Автоматическое сравнение скриншотов
- [ ] Self-healing тесты

### Phase 4: Полная автоматизация (Неделя 9-12)
- [ ] CI/CD pipeline со всеми тестами
- [ ] Performance benchmarks
- [ ] Automated regression suite

---

## 💡 Быстрый старт (MVP QA)

Для начала достаточно:

1. **Unit tests** — 70%+ coverage (Kotest или JUnit5)
2. **API tests** — Postman коллекция + Newman в CI
3. **Screenshot tests** — Paparazzi для Compose
4. **One E2E flow** — Maestro для критического пути

Затем добавлять сложность по мере роста проекта.

---

Готов помочь с реализацией любого компонента! 🚀
