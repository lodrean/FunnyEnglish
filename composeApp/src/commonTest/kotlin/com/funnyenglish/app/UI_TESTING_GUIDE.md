# Compose UI Testing Guide

## Overview

UI тесты FunnyEnglish построены на **Compose UI Testing Framework** с использованием **Page Object Pattern**. Это позволяет писать читаемые тесты, которые моделируют реальные действия пользователя.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     UI Test Architecture                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   User Flow Tests                                           │
│   ├── LoginUserFlowTest.kt          ← Сценарии логина      │
│   ├── TestTakingUserFlowTest.kt     ← Прохождение тестов   │
│   ├── AchievementUserFlowTest.kt    ← Ачивки               │
│   └── HomeUserFlowTest.kt           ← Главный экран        │
│                                                              │
│   Page Objects                                               │
│   ├── BasePage.kt                   ← Базовый класс        │
│   ├── LoginPage.kt                  ← Экран логина         │
│   ├── TestCatalogPage.kt            ← Каталог тестов       │
│   ├── TestPlayPage.kt               ← Прохождение теста    │
│   ├── AchievementsPage.kt           ← Ачивки               │
│   └── HomePage.kt                   ← Главный экран        │
│                                                              │
│   Infrastructure                                             │
│   ├── BaseUiTest.kt                 ← Базовый тест класс   │
│   └── di/TestMocks.kt               ← Моки для тестов      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Page Object Pattern

Page Object инкапсулирует детали UI и предоставляет высокоуровневые методы для взаимодействия.

### Example: LoginPage

```kotlin
class LoginPage(override val compose: ComposeUiTest) : BasePage() {
    
    companion object {
        const val TAG_EMAIL_INPUT = "login_email_input"
        const val TAG_PASSWORD_INPUT = "login_password_input"
        const val TAG_LOGIN_BUTTON = "login_button"
    }
    
    fun enterEmail(email: String) {
        enterText(TAG_EMAIL_INPUT, email)
    }
    
    fun enterPassword(password: String) {
        enterText(TAG_PASSWORD_INPUT, password)
    }
    
    fun clickLogin() {
        clickOnTag(TAG_LOGIN_BUTTON)
    }
    
    // High-level scenario
    fun login(email: String, password: String) {
        enterEmail(email)
        enterPassword(password)
        clickLogin()
    }
}
```

### Usage in Test

```kotlin
@Test
fun userCanLogin() = runTest({ LoginScreen() }) {
    val loginPage = LoginPage(this)
    
    // Given
    loginPage.assertScreenDisplayed()
    
    // When  
    loginPage.login("user@test.com", "password123")
    
    // Then
    HomePage(this).assertScreenDisplayed()
}
```

---

## Writing User Flow Tests

### Test Structure

```kotlin
/**
 * Сценарий: [Что делает пользователь]
 * 
 * Given: [Начальное состояние]
 * When: [Действие пользователя]
 * Then: [Ожидаемый результат]
 */
@Test
fun descriptiveTestName() = runTest(
    content = { YourScreen() }
) {
    val page = YourPage(this)
    
    // Test implementation
}
```

### Example: Complete User Scenario

```kotlin
/**
 * Сценарий: Пользователь проходит тест и получает ачивку
 * 
 * Given: Пользователь авторизован
 * When: Выбирает тест из каталога
 * And: Отвечает на все вопросы
 * And: Завершает тест
 * Then: Видит результаты
 * And: Получает ачивку "First Steps"
 */
@Test
fun userCompletesTestAndEarnsAchievement() = runTest(
    content = { TestPlayScreen() }
) {
    val testPage = TestPlayPage(this)
    
    // Проходим тест
    testPage.selectTextAnswer("Option A")
    testPage.clickNext()
    // ... more questions
    
    testPage.clickSubmit()
    
    // Проверяем результаты
    testPage.assertResultsDisplayed()
    testPage.assertScore("100%")
    testPage.assertAchievementUnlocked("First Steps")
}
```

---

## Test Tags

Для надежного поиска элементов используются **testTag**:

```kotlin
// In your Composable
TextField(
    value = email,
    onValueChange = { email = it },
    modifier = Modifier.testTag("login_email_input")
)

Button(
    onClick = { login() },
    modifier = Modifier.testTag("login_button")
) {
    Text("Login")
}
```

### Naming Convention

```
Format: {screen}_{element}_{type}

Examples:
- login_email_input
- login_password_input  
- login_button
- test_play_question_text
- test_play_answer_option_0
- achievement_list
- home_streak_card
```

---

## Available Matchers

### Text Matchers
```kotlin
// Find by text
compose.onNodeWithText("Submit").performClick()

// Find by text (substring)
compose.onNodeWithText("Submit", substring = true)

// Find by content description
compose.onNodeWithContentDescription("Close button")
```

### Tag Matchers
```kotlin
// Find by testTag
compose.onNodeWithTag("login_button")

// Find all by tag
compose.onAllNodesWithTag("answer_option")
```

### Assertions
```kotlin
// Visibility
.assertIsDisplayed()
.assertIsNotDisplayed()

// State
.assertIsEnabled()
.assertIsNotEnabled()
.assertIsSelected()
.assertIsNotSelected()

// Text
.assertTextContains("Hello")
.assertTextEquals("Exact text")
```

### Actions
```kotlin
// Click
.performClick()

// Text input
.performTextInput("text")
.performTextClearance()

// Gestures
.performTouchInput { swipeUp() }
.performTouchInput { swipeDown() }
.performTouchInput { swipeLeft() }
.performTouchInput { swipeRight() }
```

---

## Running Tests

### Run All UI Tests
```bash
./gradlew :composeApp:allTests
```

### Run Specific Test Class
```bash
./gradlew :composeApp:testDebugUnitTest --tests "LoginUserFlowTest"
```

### Run Single Test
```bash
./gradlew :composeApp:testDebugUnitTest --tests "LoginUserFlowTest.userCanLoginWithValidCredentials"
```

### Run with Gradle Continuous Mode
```bash
./gradlew :composeApp:testDebugUnitTest --continuous
```

---

## Best Practices

### 1. Use High-Level Scenarios

```kotlin
// ❌ Bad - too low level
compose.onNodeWithTag("login_email_input").performTextInput("email")
compose.onNodeWithTag("login_password_input").performTextInput("pass")
compose.onNodeWithTag("login_button").performClick()

// ✅ Good - high level scenario
loginPage.login("email", "pass")
```

### 2. Descriptive Test Names

```kotlin
// ❌ Bad
@Test fun test1()

// ✅ Good  
@Test fun userSeesErrorWhenPasswordIsInvalid()
```

### 3. Given-When-Then Comments

```kotlin
@Test fun userCanLogout() = runTest({ App() }) {
    val homePage = HomePage(this)
    val loginPage = LoginPage(this)
    
    // Given: User is logged in
    homePage.assertScreenDisplayed()
    
    // When: User clicks logout
    homePage.clickLogout()
    
    // Then: Login screen is shown
    loginPage.assertScreenDisplayed()
}
```

### 4. Reuse Page Objects

```kotlin
// Define pages at class level
class TestSuite : BaseUiTest() {
    private lateinit var loginPage: LoginPage
    private lateinit var homePage: HomePage
    
    @Test
    fun test() = runTest({ App() }) {
        loginPage = LoginPage(this)
        homePage = HomePage(this)
        // ...
    }
}
```

### 5. Mock Network Requests

```kotlin
// Use Koin to inject mocks
val testModule = module {
    single<ApiService> { MockApiService() }
}
```

---

## Common Patterns

### Waiting for Async Operations

```kotlin
// Wait for element to appear
compose.waitUntil(5000) {
    compose.onAllNodesWithText("Loaded").fetchSemanticsNodes().isNotEmpty()
}

// Or use Page Object method
fun waitForLoadingComplete() {
    waitForText("Content loaded")
}
```

### Checking Navigation

```kotlin
fun assertNavigationTo(screen: String) {
    compose.onNodeWithTag("${screen}_screen")
        .assertIsDisplayed()
}
```

### Testing Lists

```kotlin
// Scroll to item
compose.onNodeWithTag("list")
    .performScrollToNode(hasText("Item 10"))

// Check item count
val items = compose.onAllNodesWithTag("list_item")
    .fetchSemanticsNodes()
assertEquals(10, items.size)
```

---

## Debugging Tests

### Print Semantics Tree
```kotlin
@Test
fun debugTest() = runTest({ Screen() }) {
    // Print full semantics tree
    onRoot().printToLog("TAG")
    
    // Print specific node
    onNodeWithTag("container").printToLog("TAG")
}
```

### Capture Screenshot
```kotlin
@Test
fun screenshotTest() = runTest({ Screen() }) {
    // Capture and save
    captureToImage().asAndroidBitmap()
}
```

---

## Adding New Tests

### Step 1: Add Test Tags to Composable

```kotlin
@Composable
fun MyScreen() {
    Column(
        modifier = Modifier.testTag("my_screen")
    ) {
        Text(
            text = "Title",
            modifier = Modifier.testTag("my_title")
        )
        Button(
            onClick = { },
            modifier = Modifier.testTag("my_button")
        ) {
            Text("Click me")
        }
    }
}
```

### Step 2: Create Page Object

```kotlin
class MyPage(override val compose: ComposeUiTest) : BasePage() {
    companion object {
        const val TAG_SCREEN = "my_screen"
        const val TAG_TITLE = "my_title"
        const val TAG_BUTTON = "my_button"
    }
    
    fun assertScreenDisplayed() {
        assertTagDisplayed(TAG_SCREEN)
    }
    
    fun clickButton() {
        clickOnTag(TAG_BUTTON)
    }
}
```

### Step 3: Write Test

```kotlin
class MyUserFlowTest : BaseUiTest() {
    @Test
    fun userCanInteractWithMyScreen() = runTest(
        content = { MyScreen() }
    ) {
        val page = MyPage(this)
        
        page.assertScreenDisplayed()
        page.clickButton()
        
        // Assert result
    }
}
```

---

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: UI Tests
on: [push, pull_request]

jobs:
  ui-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'corretto'
      
      - name: Run UI Tests
        run: ./gradlew :composeApp:testDebugUnitTest
      
      - name: Upload Test Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: composeApp/build/reports/tests/
```

---

## Resources

- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing)
- [Testing Cheat Sheet](https://developer.android.com/jetpack/compose/testing-cheatsheet)
- [Semantics in Compose](https://developer.android.com/jetpack/compose/accessibility)

---

## Troubleshooting

### Test Fails to Find Node
- Check that testTag is applied correctly
- Use `onRoot().printToLog()` to see available nodes
- Verify the screen is actually displayed

### Async Operations Timeout
- Increase timeout in `waitUntil()`
- Use `waitForIdle()` after async operations
- Mock API calls to be synchronous in tests

### Flaky Tests
- Add proper waits for async operations
- Use `waitUntil()` instead of `Thread.sleep()`
- Ensure test isolation (clear state between tests)
