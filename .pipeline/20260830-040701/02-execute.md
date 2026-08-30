# 02-execute — bd FunnyEnglish-qbq.5: INF: CI — слияние workflow + detekt + Kover

## Что сделано

1. **Слияние 3 workflow в один pipeline** (`.github/workflows/ci.yml`):
   - `tests.yml` — удалён (был deprecated 2026-07-20, дублировал джобы ci.yml).
   - `quality-check.yml` — удалён; уникальные джобы перенесены в `ci.yml`: compose-app quality (compileKotlinDesktop + desktopTest + Kover), E2E (docker-стек + smoke CORS + Playwright админки), security-scan (Trivy+SARIF, с permissions), coverage-артефакты.
   - В `ci.yml` добавлены джобы: `compose-app-test`, `detekt`, `ci-summary` (агрегирует статусы + fail-gate по результатам needs).
   - Триггеры: push/PR main,develop + cron `0 9 * * *` (из quality-check) + workflow_dispatch.
   - admin-web в едином джобе: lint + typecheck + `npx vitest run --coverage` (грабли №69/88) + build + upload coverage.

2. **detekt подключён к :backend и :composeApp** (грабля №8 закрыта):
   - Плагин-алиас `detekt` добавлен в `gradle/libs.versions.toml` ([plugins], version.ref = detekt 1.23.7); корневой `build.gradle.kts` переведён на `alias(libs.plugins.detekt) apply false`.
   - В обоих модулях: `detekt { config.setFrom(config/detekt/detekt.yml); baseline = config/detekt/baseline.xml }` — общий существующий конфиг (maxIssues: 0) и общий baseline.

3. **Пороги Kover** (`koverVerify`, DSL kover 0.9.1 `kover { reports { verify { rule { bound {} } } } }`):
   - backend: line coverage ≥ 40%.
   - composeApp: line coverage ≥ 20% (существующий блок kover с `disabledForTestTasks` сохранён).
   - Значения консервативные стартовые с комментариями «поднимать постепенно» (прецедент грабли №88).

4. **Документация**: обновлены таблица workflow в `docs/RELEASE_FLOW.md`, грабля №8 и раздел команд в `memory.md`, добавлена запись в «Решения и договорённости».

## Изменённые/созданные файлы

- `.github/workflows/ci.yml` — переписан (единый pipeline)
- `.github/workflows/tests.yml` — удалён
- `.github/workflows/quality-check.yml` — удалён
- `gradle/libs.versions.toml` — + alias `detekt` в [plugins]
- `build.gradle.kts` — detekt через alias (apply false)
- `backend/build.gradle.kts` — + detekt (config+baseline), + kover verify rule (line 40)
- `composeApp/build.gradle.kts` — + detekt (config+baseline), + kover verify rule (line 20)
- `docs/RELEASE_FLOW.md` — таблица workflow актуализирована
- `memory.md` — грабля №8, команда detekt, запись в решениях

## Как проверить (гейты драйвера)

1. `./gradlew :backend:detektBaseline :composeApp:detektBaseline` — **сгенерировать общий baseline** `config/detekt/baseline.xml` (сейчас пустой → detekt упадёт на существующих замечаниях) и закоммитить. После этого `./gradlew :backend:detekt :composeApp:detekt` должен быть зелёным.
2. `./gradlew :backend:test :backend:koverVerify :backend:koverHtmlReport` — тесты + порог 40%.
3. `./gradlew :composeApp:desktopTest :composeApp:koverVerify :composeApp:koverHtmlReport` — тесты + порог 20%.
4. Если koverVerify падает — выровнять порог под фактическое покрытие (как в №88), не удалять правило.
5. YAML ci.yml проверен `yaml.safe_load` (OK). Полная проверка — `gh run list` после пуша.

## Замечания

- Если фактическое покрытие ниже стартовых порогов — скорректировать значения в `backend/build.gradle.kts` / `composeApp/build.gradle.kts` (одно число `minValue`).
- detekt для KMP-модуля создаёт per-compilation задачи; агрегирующая `:composeApp:detekt` должна покрывать все source set'ы — подтвердить выводом первого прогона.
