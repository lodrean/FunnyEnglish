# QA Automation Documentation

Complete guide to the QA Automation setup for FunnyEnglish application.

## Overview

The QA Automation framework consists of three layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    QA Automation Pyramid                     │
├─────────────────────────────────────────────────────────────┤
│  🎨 Visual Layer  │ AI QA Agent (Phase 2)                    │
│                   │ Screenshot comparison, Visual regression  │
├───────────────────┼───────────────────────────────────────────┤
│  📱 E2E Layer     │ Maestro                                   │
│                   │ User flows, Cross-platform testing        │
├───────────────────┼───────────────────────────────────────────┤
│  🔌 API Layer     │ Newman (Postman)                          │
│                   │ REST API testing, Contract validation     │
├───────────────────┼───────────────────────────────────────────┤
│  ⚙️  Unit Layer   │ Kotest (Kotlin)                           │
│                   │ Business logic, Validation rules          │
└───────────────────┴───────────────────────────────────────────┘
```

## 1. Unit Tests (Kotest)

### Location
- `shared/src/commonTest/kotlin/com/funnyenglish/shared/`

### Running Tests

```bash
# All tests
./gradlew :shared:allTests

# Specific module
./gradlew :backend:test
```

### Writing Tests

```kotlin
class AuthValidatorTest : FunSpec({
    test("validateEmail should return true for valid email") {
        AuthValidator.validateEmail("user@example.com") shouldBe true
    }
    
    test("validatePassword should require min 8 chars") {
        AuthValidator.validatePassword("weak") shouldBe false
        AuthValidator.validatePassword("Strong1!") shouldBe true
    }
})
```

## 2. API Tests (Newman/Postman)

### Location
- `qa/postman/`

### Running Tests

```bash
cd qa

# Basic run
newman run postman/funnyenglish-api.json

# With environment
newman run postman/funnyenglish-api.json \
  -e postman/test-environment.json

# With HTML report
newman run postman/funnyenglish-api.json \
  -e postman/test-environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export reports/api-test-report.html
```

### Prerequisites

1. Backend running on `http://localhost:8080`
2. PostgreSQL accessible
3. Newman installed: `npm install -g newman`

## 3. E2E Tests (Maestro)

### Location
- `.maestro/flows/`

### Running Tests

```bash
# All flows
maestro test .maestro/

# Specific flow
maestro test .maestro/flows/login.yaml

# With tags
maestro test .maestro/ --include-tags=smoke
```

### Available Flows

| Flow | Description | Tags |
|------|-------------|------|
| `login.yaml` | User login flow | smoke, auth |
| `complete_test.yaml` | Full test completion | e2e, critical |

### Writing Flows

```yaml
appId: com.funnyenglish.app
---
- launchApp
- assertVisible: "Вход"
- tapOn: "Email"
- inputText: "test@example.com"
- tapOn: "Пароль"
- inputText: "password123"
- tapOn: "Войти"
- assertVisible: "Категории"
```

## 4. AI QA Agent (Visual Regression)

### Location
- `qa-agent/`

### Installation

```bash
cd qa-agent
./setup.sh
source venv/bin/activate
```

### Commands

```bash
# Compare screenshots
qa-agent compare baseline.png current.png -n "login-screen"

# Custom thresholds
qa-agent compare base.png curr.png \
  --pixel-threshold 0.05 \
  --ssim-threshold 0.98

# Show configuration
qa-agent config
```

### Python API

```python
from qa_agent.visual_diff import VisualDiffEngine
from qa_agent.report_generator import ReportGenerator

engine = VisualDiffEngine(
    pixel_threshold=0.1,
    ssim_threshold=0.95
)

report = engine.compare(
    baseline_path="baseline.png",
    current_path="current.png",
    output_dir="./reports"
)

generator = ReportGenerator(output_dir="./reports")
generator.generate_html(report, "screen-name")
```

## CI/CD Pipeline

### GitHub Actions

Workflow: `.github/workflows/qa-automation.yml`

```yaml
name: QA Automation Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Kotest
        run: ./gradlew :shared:allTests

  api-tests:
    needs: unit-tests
    runs-on: ubuntu-latest
    steps:
      - name: Run Newman
        run: newman run qa/postman/funnyenglish-api.json

  e2e-tests:
    needs: api-tests
    runs-on: macos-latest
    steps:
      - name: Run Maestro
        run: maestro test .maestro/flows/
```

### Pipeline Flow

```
Push/PR
    │
    ▼
┌───────────────┐
│  Unit Tests   │ ← Kotest (fastest)
│   (~2 min)    │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│   API Tests   │ ← Newman
│   (~5 min)    │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│   E2E Tests   │ ← Maestro
│  (~15 min)    │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Visual Tests  │ ← AI QA Agent (optional)
│  (~5 min)     │
└───────────────┘
```

## Test Environments

### Docker Compose Test Environment

```bash
# Start test infrastructure
docker-compose -f docker-compose.test.yml up -d

# Run tests
./gradlew :backend:test
newman run qa/postman/funnyenglish-api.json

# Stop
docker-compose -f docker-compose.test.yml down
```

### Test Data

The test environment includes:
- PostgreSQL on port `5433`
- MinIO on port `9010`
- Pre-populated test data

## Best Practices

### 1. Test Organization

- Keep tests close to the code they test
- Use descriptive test names
- Follow AAA pattern: Arrange, Act, Assert

### 2. Test Independence

- Each test should be independent
- Clean up after tests
- Don't rely on test execution order

### 3. Test Data

- Use factory methods for test data
- Avoid hardcoding IDs
- Use environment variables for configuration

### 4. Selective Testing

- Run unit tests on every commit
- Run API tests on PR
- Run E2E tests before release
- Run visual tests on UI changes

## Troubleshooting

### Unit Tests

```bash
# Run with info
./gradlew :shared:allTests --info

# Run specific test
./gradlew :shared:allTests --tests "*AuthValidatorTest*"
```

### API Tests

```bash
# Run with delay
newman run collection.json --delay-request 500

# Verbose output
newman run collection.json --verbose
```

### E2E Tests

```bash
# Debug mode
maestro test .maestro/flows/ --debug-output

# Record video
maestro test .maestro/flows/ --format xml
```

## Resources

- [Kotest Documentation](https://kotest.io/)
- [Newman Documentation](https://learning.postman.com/docs/collections/using-newman-cli/command-line-integration-with-newman/)
- [Maestro Documentation](https://maestro.mobile.dev/)
- [OpenCV Documentation](https://docs.opencv.org/)

## Contributing

When adding new features:

1. Write unit tests for business logic
2. Add API tests for new endpoints
3. Add E2E tests for critical user flows
4. Update visual baselines if UI changes

See `CONTRIBUTING.md` for more details.
