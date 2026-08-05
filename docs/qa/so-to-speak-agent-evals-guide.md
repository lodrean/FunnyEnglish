# Гайд: Evaluation Framework для AI-агентов в So to Speak

**Версия:** 1.0  
**Дата:** 2026-08-02  
**Продукт:** So to Speak (KMP Compose Multiplatform, speaking-тренажёр)  
**Статус:** Рабочий черновик — применять сразу, дорабатывать по мере поступления данных

---

## 1. Зачем этот гайд

AI-агенты (Kimi Code CLI, Claude Code, Cursor Agent и др.) уже выполняют задачи в нашем KMP-проекте. Без системы оценки (evals) мы:
- Не видим, стал ли агент лучше или хуже после обновления модели
- Не можем отличить реальную регрессию от шума
- Рискуем накопить техдолг, который агент генерирует быстрее, чем мы его контролируем
- Теряем понимание кода — исследование Anthropic (2026) показало: разработчики с AI набирают на **17% меньше** в тестах на понимание кода

Этот гайд — практический набор правил, метрик и процессов, чтобы evals работали **сегодня**, а не "когда-нибудь".

---

## 2. Принципы (незыблемые)

### 2.1. Два типа evals — всегда вместе

| Тип | Вопрос | Pass rate | Когда запускать |
|-----|--------|-----------|-----------------|
| **Capability** | «Что агент умеет?» | 20–80% (зависит от сложности) | При внедрении новой модели, нового инструмента, новой категории задач |
| **Regression** | «Всё ещё работает?» | ~95–100% | Каждый PR, каждое обновление агента, ежедневно ночью |

> **Правило:** Capability eval с pass rate >90% "выпускается" в Regression suite и защищается навсегда.

### 2.2. Три слоя проверки

```
┌─────────────────────────────────────────────────────────────┐
│  LAYER 1: Code-based graders (быстро, дёшево, объективно)   │
│  → Detekt, KMP compile, desktopTest, e2e-cmp screenshot diffs │
├─────────────────────────────────────────────────────────────┤
│  LAYER 2: Model-based graders (гибко, для нюансов)          │
│  → LLM-rubric на архитектуру, code style, alignment с Figma  │
├─────────────────────────────────────────────────────────────┤
│  LAYER 3: Human graders (золотой стандарт)                  │
│  → Code review senior'ом, pixel-perfect check дизайнером    │
└─────────────────────────────────────────────────────────────┘
```

> **Правило:** Layer 1 запускается автоматически в CI. Layer 2 — на выборке. Layer 3 — обязательно для каждого агентского PR до мержа.

### 2.3. Метрики автономии — не менее важны, чем correctness

| Метрика | Почему важна | Как измерить |
|---------|-------------|--------------|
| **Turn duration** | Показывает, насколько сложные задачи агент берёт | Время от prompt до завершения/остановки |
| **Human interventions / task** | Частые прерывания = агент не справляется | Считаем уточнения и прерывания в треде |
| **Token efficiency** | Дорогостоящая метрика; агент может "крутиться" вокруг задачи | Токены / строка кода или токены / задача |
| **Build success rate** | Агент генерирует код, который не собирается с первой попытки | % green build после агентского коммита |
| **Clarification rate** | Агент задаёт уточнения чаще, чем нужно? | Количество "уточните..." / задача |

> **Правило:** Если агент прерывается для уточнений >3 раз на простую задачу — задача слишком расплывчата или агент недостаточно автономен.

### 2.4. Проверка понимания — обязательна

На основе [Anthropic, 2026]: AI-ассистирование снижает понимание кода. После каждой 3-й задачи, выполненной агентом, разработчик проходит мини-опрос:

1. Объясни, как работает сгенерированный код, своими словами (2–3 предложения)
2. Где находится entry point в эту фичу?
3. Что произойдёт, если пользователь нажмёт кнопку "Назад" в середине flow?
4. Найди потенциальный баг в сгенерированном коде (намёк: он может быть)

> **Правило:** Не прошёл 2+ вопроса из 4 — задача не считается завершённой. Нужен pair-review с объяснением.

---

## 3. Категории задач So to Speak и их evals

### 3.1. Каталог категорий

| Код | Категория | Примеры задач | Primary Graders |
|-----|-----------|--------------|-----------------|
| **UI** | UI / UX Layout | Экран Library, карточка темы, bottom nav, SpeakingGate, анимация прогресса | e2e-cmp pixel-diff, Compose UI tests, accessibility scan, Figma pixel-match |
| **NAV** | Navigation | Добавление экрана в AppScreen, back-обработка, bottom navigation | Compose tests, Maestro flow |
| **API** | API Integration | Загрузка библиотек, отправка submission, авторизация | MockWebServer/Ktor tests, contract tests, error handling tests |
| **ARCH** | Architecture | Новый ViewModel, Repository, UseCase, Koin-модуль | ArchUnit/dependency checks, Koin graph compilation, dependency rule checks |
| **DATA** | Data Layer | SharedPreferences/multiplatform-settings, Ktor client config | Integration tests, migration tests |
| **TEST** | Testing | Unit tests, Compose UI tests, test data builders | Coverage report (JaCoCo/kover), mutation testing |
| **SA** | Static Analysis | Исправление detekt/lint, cleanup warnings | Detekt == 0, lint delta ≤ 0 |
| **PERF** | Performance | LazyColumn, Coil caching, анимации | Compose UI tests, startup benchmark, memory leak detection |

### 3.2. Eval spec по категориям

#### UI — Экран Library (ThemeCover карточка)

```yaml
task: "Добавить карточку темы в Library по мокапу"
category: UI
input:
  figma: ".docs/design-system/mockups.html#library"
  requirements:
    - "ThemeCover с градиентом по хешу id и инициалами"
    - "ThemeStatusChip (ПРОЙДЕНО/НОВАЯ)"
    - "ThemeProgressBar 4dp"
    - "Плюрализация 'N топиков'"
    - "Поддержка TalkBack / contentDescription"

graders:
  layer_1_code:
    - type: build
      command: ./gradlew :composeApp:compileKotlinDesktop :composeApp:compileKotlinAndroid
      must_succeed: true
    - type: compose_ui_test
      command: ./gradlew :composeApp:desktopTest
      required_tests:
        - "theme cover displays initials"
        - "status chip visible"
    - type: screenshot_regression
      tool: e2e-cmp
      threshold: 0.1%  # pixel diff vs baseline
    - type: detekt
      detekt_errors: 0

  layer_2_model:
    - type: llm_rubric
      rubric: |
        Оцени код по критериям (1-5):
        1. Использует ли код SpeakingTokens / design system?
        2. Соответствует ли именование conventions проекта (MVI, State/Action/Event)?
        3. Есть ли reusable компоненты или весь UI в одном файле?
        4. Обработаны ли edge cases (пустой список, ошибка загрузки)?
        5. Можно ли покрыть это UI-тестами без рефакторинга?
      min_score: 3.5

  layer_3_human:
    - type: code_review
      reviewer: senior_kmp_dev
    - type: design_review
      reviewer: ux_designer
      criteria: "Pixel-perfect с Figma ±1dp"

tracked_metrics:
  - n_turns
  - n_toolcalls
  - total_tokens
  - build_attempts
  - human_interventions
  - time_to_completion
```

#### API — Загрузка списка библиотек

```yaml
task: "Реализовать загрузку списка библиотек с бэкенда"
category: API
input:
  endpoint: "GET /api/public/speaking/libraries"
  requirements:
    - "Repository с Result<T, E>"
    - "Обработка 401 → редирект на авторизацию"
    - "Кэш в памяти / settings при необходимости"
    - "Error state: retry button, заглушка 'Нет интернета'"
    - "Unit tests: success, empty, error, network timeout"

graders:
  layer_1_code:
    - type: unit_tests
      min_coverage: 80
      required_tests:
        - test_loadLibraries_success_returnsList
        - test_loadLibraries_empty_returnsEmptyState
        - test_loadLibraries_401_navigatesToAuth
        - test_loadLibraries_timeout_showsRetry
    - type: build
      command: ./gradlew :shared:compileKotlin :composeApp:compileKotlinDesktop
    - type: detekt
      errors: 0

  layer_2_model:
    - type: llm_rubric
      rubric: |
        1. Корректно ли разделены слои (Data → Domain → Presentation)?
        2. Используется ли Flow/Coroutines правильно?
        3. Есть ли утечка абстракций (Ktor client в ViewModel)?
        4. Как обрабатывается cancellation (job cancelling)?
      min_score: 3.5

  layer_3_human:
    - type: code_review
      reviewer: senior_kmp_dev

tracked_metrics:
  - n_turns
  - total_tokens
  - test_count
  - coverage_percent
```

#### SA — Cleanup Detekt Warnings

```yaml
task: "Исправить 50 detekt warnings в модуле composeApp"
category: SA
input:
  detekt_report: "composeApp/build/reports/detekt/detekt.xml"
  requirements:
    - "Устранить все warnings категории complexity"
    - "Устранить все unused imports / parameters"
    - "Не добавлять новых warnings"
    - "Не менять бизнес-логику без согласования"

graders:
  layer_1_code:
    - type: detekt
      command: ./gradlew detekt
      max_warnings_delta: 0
      target_reduction: 50
    - type: build
      command: ./gradlew :composeApp:compileKotlinDesktop

  layer_2_model:
    - type: llm_rubric
      rubric: |
        1. Все исправления — mechanical (без изменения семантики)?
        2. Нет ложных срабатываний detekt, которые стоит suppress'ить правильно?
        3. Сохраняется ли обратная совместимость?
      min_score: 4.0

  layer_3_human:
    - type: spot_check_review
      reviewer: any_kmp_dev
      sample_size: 20%  # проверить 10 из 50 исправлений

tracked_metrics:
  - warnings_before
  - warnings_after
  - fixes_count
  - build_attempts
  - time_to_completion
```

---

## 4. Regression Suite — "Золотые" задачи

Набор из **20 задач**, которые агент должен решать консистентно. Обновляется ежемесячно.

| ID | Категория | Задача | Минимальный pass^k |
|----|-----------|--------|-------------------|
| REG-UI-01 | UI | Создать Compose-экран с LazyColumn и stable keys | 95% |
| REG-UI-02 | UI | Добавить bottom sheet / dialog по мокапу | 95% |
| REG-NAV-01 | NAV | Добавить экран в AppScreen с back-обработкой | 95% |
| REG-API-01 | API | Создать Ktor endpoint + Repository + Koin module | 90% |
| REG-API-02 | API | Обработать network error с retry | 90% |
| REG-ARCH-01 | ARCH | Создать ViewModel с StateFlow и sealed class состояний | 95% |
| REG-DATA-01 | DATA | Добавить multiplatform-settings key с typed wrapper | 90% |
| REG-TEST-01 | TEST | Написать unit tests для ViewModel (success, error, loading) | 90% |
| REG-SA-01 | SA | Исправить 1 detekt error (unused import) | 99% |
| REG-SA-02 | SA | Исправить 10 detekt warnings | 95% |
| REG-PERF-01 | PERF | Оптимизировать recomposition в списке | 90% |
| ... | ... | ... | ... |

> **Правило:** Regression suite прогоняется автоматически каждую ночь и при каждом обновлении агента/модели. Падение pass^k ниже порога — блокер для релиза агента.

---

## 5. Инфраструктура — что настроить

### 5.1. CI Pipeline (GitHub Actions)

```yaml
# .github/workflows/agent-eval.yml
name: Agent Eval
on:
  pull_request:
    branches: [main, develop]
  schedule:
    - cron: "0 3 * * *"  # nightly regression
  workflow_dispatch:

jobs:
  code-graders:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      
      - name: KMP Compile
        run: ./gradlew :composeApp:compileKotlinDesktop :composeApp:compileKotlinAndroid
      - name: Desktop Unit Tests
        run: ./gradlew :composeApp:desktopTest
      - name: Backend Tests
        run: ./gradlew :backend:test
      - name: Admin Vitest
        run: cd admin-web && npx vitest run
      - name: Build Android
        run: ./gradlew :app:assembleDebug
      
      - name: Report Metrics
        run: python scripts/report_eval_metrics.py

  model-graders:
    if: github.event_name == 'workflow_dispatch'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Architecture Alignment Check
        env: { ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }} }
        run: python scripts/eval_architecture.py --pr ${{ github.event.pull_request.number }}
      - name: Code Quality Rubric
        env: { ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }} }
        run: python scripts/eval_quality.py --pr ${{ github.event.pull_request.number }}
```

### 5.2. Eval Harness

Для So to Speak используем комбинацию:
- **qa-agent** — визуальная регрессия (pixel diff, SSIM) для e2e-cmp скриншотов
- **e2e-cmp** — координатные клики + pixel-diff assertion'ы для WASM-таргета
- **Maestro** — мобильные end-to-end флоу
- **Gradle tasks** — компиляция и unit/UI-тесты KMP/backend

Ключевые метрики для сбора:

```kotlin
data class AgentMetrics(
    val turnsCount: Int,
    val toolCallsCount: Int,
    val totalTokens: Int,
    val humanInterventions: Int,
    val clarificationsAsked: Int,
    val timeToCompletionMs: Long,
    val buildAttempts: Int,
    val testsPassed: Int,
    val testsTotal: Int,
    val detektErrors: Int,
    val e2eCmpPassed: Int,
    val e2eCmpTotal: Int,
    val maestroPassed: Int,
    val maestroTotal: Int
)
```

### 5.3. Дашборд метрик

Рекомендуется завести простой дашборд (Notion / Grafana / Google Sheets) с еженедельным апдейтом:

| Неделя | Capability pass@1 | Regression pass^k | Avg turns/task | Avg tokens/task | Detekt Δ | Comprehension score |
|--------|-------------------|-------------------|----------------|-----------------|----------|---------------------|
| W33 | — | — | — | — | — | — |
| W34 | 62% | 94% | 8.3 | 12.4K | -15 | 78% |

---

## 6. Чек-лист для каждой агентской задачи

### Перед стартом задачи
- [ ] Задача описана конкретно (есть входные данные, критерии приёмки, скоуп)
- [ ] Выбрана категория (UI / NAV / API / ARCH / DATA / TEST / SA / PERF)
- [ ] Понятно, какой grader будет primary
- [ ] У senior'а есть 10 минут на ревью после завершения

### Во время работы агента
- [ ] Отслеживается количество уточнений и прерываний
- [ ] Если агент "застрял" >10 минут — human takeover или переписать prompt

### После завершения задачи
- [ ] Layer 1 (CI) — зелёный
- [ ] Layer 2 (LLM rubric) — пройден
- [ ] Layer 3 (Human review) — аппрувнут
- [ ] **Comprehension check** — разработчик ответил на 3+ вопроса из 4
- [ ] Метрики записаны в дашборд
- [ ] Задача добавлена в Capability или Regression suite

---

## 7. Риски и что с ними делать

| Риск | Признак | Действие |
|------|---------|----------|
| **Агент проходит evals, но код не production-ready** | Human review находит критические проблемы после "зелёного" CI | Ужесточить LLM-rubric, добавить integration tests, увеличить sample size для human review |
| **Атрофия навыков команды** | Comprehension score падает ниже 60% | Ввести "AI-free дни", требовать объяснения кода перед мержем, rotate задачи без AI |
| **Overfitting evals** | Агент проходит regression, но ломается на новых задачах | Регулярно обновлять capability suite, held-out test set, randomize параметры |
| **Высокая стоимость LLM-graders** | Бюджет на API растёт быстрее полезности | Использовать code-based graders как primary, LLM — только для calibration и сложных кейсов |
| **Нестабильные UI-тесты** | e2e-cmp/Maestro флакает | Использовать deterministic рендеринг, фиксировать viewport, отключать анимации в тестах |
| **Агент генерирует много техдолга** | Detekt warnings растут, coverage падает | Добавить SA-graders как gate, запретить мерж при detekt > 0 или lint Δ > 0 |

---

## 8. Быстрый старт (сделать сегодня)

1. **Добавить в CI** (`./gradlew :composeApp:compileKotlinDesktop :composeApp:desktopTest :backend:test`) как обязательный gate для всех PR
2. **Создать 5 «золотых» задач** из текущего бэклога — самых типовых (Compose-экран, API call, ViewModel, cleanup detekt, unit test)
3. **Начать отслеживать** в Google Sheets: turns/task, tokens/task, build attempts, human interventions
4. **Ввести comprehension check** — 4 вопроса после каждой 3-й агентской задачи
5. **Назначить владельца evals** — один человек отвечает за обновление suite, калибровку graders, анализ метрик

---

## Приложение A. Comprehension Check — шаблон

```
Задача: ___________________
Агент: ___________________
Разработчик: _____________
Дата: ____________________

1. Объясни, как работает сгенерированный код, своими словами:
   [поле для ответа]

2. Где находится entry point в эту фичу (класс/метод)?
   [поле для ответа]

3. Что произойдёт, если пользователь нажмёт "Назад" в середине flow?
   [поле для ответа]

4. Найди потенциальный баг или edge case в сгенерированном коде:
   [поле для ответа]

Оценка: ___/4 (проход ≥ 3/4)
Подпись ревьюера: _________
```

---

## Приложение B. Eval Task Template

```yaml
task:
  id: "sts-{category}-{n:03d}"
  category: UI|NAV|API|ARCH|DATA|TEST|SA|PERF
  description: ""
  
  input:
    requirements: []
    figma_url: ""
    api_spec: ""
    
  graders:
    layer_1_code:
      - type: build
      - type: detekt
      - type: unit_tests
      - type: compose_ui_tests
      - type: e2e_cmp_screenshot
    layer_2_model:
      - type: llm_rubric
        rubric: ""
    layer_3_human:
      - type: code_review
        reviewer: ""
      
  tracked_metrics:
    - n_turns
    - n_toolcalls
    - total_tokens
    - time_to_completion
    - human_interventions
    - build_attempts
```

---

**Владелец документа:** [TBD]  
**Следующее обновление:** После первых 2 недель сбора метрик — корректировка порогов, расширение regression suite.
