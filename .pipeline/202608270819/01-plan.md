# План: FunnyEnglish-4d1 (завершение follow-up) — верификация Ktor 3.0.3 + явного движка, закрытие крит.2, блокер живого гейта

> Статус: **DRAFT — на согласование человека** (планировщик конвейера)
> Пайплайн: 202608270819 · Тикет: bd FunnyEnglish-4d1 (P3, task, in_progress)
> Проект: So to Speak (FunnyEnglish) · Ветка: develop (изменения прогонов 202608270715/202608270758 НЕ закоммичены, лежат в working tree)
> Автор: планировщик конвейера · Дата: 2026-08-27
> Предыдущие прогоны: 202608270715 (реализация, вердикт ЧАСТИЧНО) · 202608270758 (план follow-up; execute выполнен частично — см. §2)

---

## 0. Предположение о скоупе (требует подтверждения)

Промпт планировщика не описывает задачу («продолжи работ»). По состоянию конвейера «продолжение» =
**завершение follow-up FunnyEnglish-4d1**, начатого прогоном 202608270758: изменения уже лежат в
working tree (ktor=3.0.3 в каталоге, expect/actual-фабрика движка, тест движка), гейт №1 прогона 758
(desktopTest 120/120 + compileDebugKotlinAndroid) прошёл, но гейт №2 (wasm) прерван, а
memory.md / .beads / verify / report для follow-up НЕ обновлены. Код менять не планируется —
только догнать гейты, зафиксировать факты, обновить память/статусы и оформить артефакты.
Если подразумевалась иная задача (открытые bd: j8r, c47/xic/0zl — вне скоупа, см. §9) — план переоформим.

## 1. Цель и критерии приёмки

**Цель.** Завершить follow-up 4d1: (1) подтвердить единую версию Ktor **3.0.3** на всех classpath
(deps-отчёт) — закрыть формально открытый крит.2 прогона 202608270715; (2) подтвердить детерминированный
выбор движка медиа-клиента (OkHttp/Js/Darwin) тестом и кодом; (3) перезапустить прерванный wasm-гейт и
догнать остальные гейты; (4) обновить memory.md и .beads/issues.jsonl; (5) зафиксировать блокер живого
Android-гейта (крит.3/5) с командами; (6) оформить артефакты пайплайна 202608270819.
Поведение плеера не меняется — спеки (Part 1–3, DESIGN_SYSTEM_SPEC, PRD) не затрагиваются (ADR-007 не задействуется).

**Критерии приёмки (Gate REVIEW_OK + QA_PASS):**
1. deps-отчёт: на `androidDebugCompileClasspath` все прямые/транзитивные `io.ktor:*` — **ровно 3.0.3**
   (никаких 3.0.2); то же по факту резолва для desktop/wasmJs classpath'ов. Любые остаточные версии
   ≠ 3.0.3 — задокументированы (критично: coil-network-ktor3, ktor-client-mock).
2. Движок медиа-клиента детерминирован: android/desktop — OkHttp явно, wasmJs — Js явно, ios — Darwin
   (единственный на runtime, неявно) — подтверждено `MediaHttpClientEngineTest` (1/1) и кодом фабрики.
3. Гейты зелёные: `:composeApp:desktopTest` (ожидается **120/120**), `:composeApp:compileDebugKotlinAndroid`,
   `:composeApp:compileKotlinWasmJs --no-configuration-cache` (**перезапуск** — прерван в 758),
   `:app:assembleDebug`, компиляции ktor-бампа shared/core (R5).
4. Статика: в androidMain по-прежнему нет `DefaultHttpDataSource`/`DefaultDataSource`;
   `MediaHttpClient.create()` идёт через `createPlatformMediaHttpClient` (явный движок, не ServiceLoader).
5. Живой Android-гейт (крит.3/5): **блокирован до окружения** (adb пуст — эмулятора нет; dev-стек docker
   не поднят, работает только staging). Фиксируется блокером с точными командами (§6.6/§7.1);
   тикет 4d1 **не закрывается** до живого прогона (политика прогонов 715/758).
6. memory.md дополнен (решение follow-up + уточнение грабли ktor-версий); `.beads/issues.jsonl` — статус
   4d1 обновлён (крит.2 закрыт; крит.3/5 ждут живого гейта; тикет in_progress).
7. Артефакты пайплайна 202608270819: `02-execute.md`, `03-verify.md`, `00-report.md` (итог: ОК с блокером).

## 2. Факты из исследования (read-only, состояние на 2026-08-27)

- **Каталог `gradle/libs.versions.toml`**: `ktor = "3.0.3"` (бамп уже применён в 758) + alias
  `androidx-media3-datasource-ktor` (media3 1.11.0, добавлен в 715). По grep по `*.kts` ссылок на
  `3.0.2` НЕТ — каталог единственный источник версий.
- **`composeApp/build.gradle.kts`**: androidMain += `libs.androidx.media3.datasource.ktor`
  (изменение 715). `desktopTest`-source set НЕ объявлен явно — подхватывается дефолтной иерархией
  KMP (подтверждено: тест из `src/desktopTest` скомпилировался и выполнился).
- **`MediaHttpClient` (commonMain)**: `create() = createPlatformMediaHttpClient { mediaConfig() }`;
  `mediaConfig()`: expectSuccess=false, HttpRedirect, HttpTimeout connect 10s + request/socket
  INFINITE (Long.MAX_VALUE); БЕЗ auth/JSON. **Файлы actual**: android/desktop — `HttpClient(OkHttp)`,
  wasmJs — `HttpClient(Js)`, ios — `HttpClient { config(this) }` (Darwin, без compile-зависимости).
  Koin `single<HttpClient>(named("media"))`; VideoPlayerController/VideoScreen — не меняются этим прогоном.
- **Новый тест** `MediaHttpClientEngineTest.kt` (desktopTest): asserts `client.engine is OkHttpEngine`,
  без сети. **XML от прогона 758**: 20 файлов, **120 тестов, 0 failures, 0 errors** (newest
  `2026-08-27T08:16:34Z`), вкл. engine-тест 1/1 (timestamp 08:16:19Z).
- **Гейт-логи 758**: `gate1-desktopTest-androidCompile.log` — **BUILD SUCCESSFUL in 2m 15s**
  (desktopTest + android compile, 11:16:38); `gate2-wasm.log` (11:16:58, 4.4 КБ) **обрывается на середине
  сборки** (последняя строка `> Task :design:compileKotlinWasmJs UP-TO-DATE`, BUILD-результата нет) →
  результат wasm-гейта **НЕ подтверждён**, требуется перезапуск.
- **Окружение**: `adb devices` — пусто (эмулятора нет); `docker ps` — daemon РАБОТАЕТ, поднят только
  staging-стек (admin:3100, backend:8180, postgres:5433, minio:9100, mailpit:8125; проект sotospeak-staging)
  + несвязанный hometasks-pb:8090; dev-стек (backend:8080) НЕ запущен → живой Android-гейт невыполним
  без эмулятора.
- **bd CLI**: `bd --version` — пусто (недоступен) → статус тикета обновлять вручную в
  `.beads/issues.jsonl` (как в прогонах 715/758).
- **memory.md**: запись решения 2026-08-27 (прогон 715, строка решения + грабля №99) ЕСТЬ;
  follow-up (ktor 3.0.3, явный движок) **НЕ зафиксирован**.
- **`.beads/issues.jsonl`**: `FunnyEnglish-4d1` — status `in_progress`, updated_at 2026-08-27T07:26:19Z
  (время прогона 715); follow-up не отражён. Другие открытые: c47/xic/0zl (дизайн-конформити, решения
  владельца), j8r (cleanup media3-session, P4) — вне скоупа.
- **git**: ветка develop; изменения 715+758 не закоммичены (12 M-файлов, +62/−16; untracked: файлы 4d1 +
  `.pipeline/`, `scripts/plan-execute-verify.*`, `README.plan-execute-verify.md`, `videos/` (промо-видео,
  несвязано), `.media/preferences.json`). **Несвязанные untracked-файлы в коммит не включать (R7).**

## 3. Изменения по подсистемам

Кодовые изменения уже в working tree (прогоны 715+758) — данный прогон их НЕ меняет:

| Подсистема | Состояние (уже в дереве) | Действие этого прогона |
|---|---|---|
| gradle/libs.versions.toml | ktor 3.0.2→**3.0.3**; +alias media3-datasource-ktor | только проверка резолва (крит.1) |
| composeApp/build.gradle.kts | +androidMain dep media3-datasource-ktor | без изменений (desktopTest подхвачен автоматически) |
| composeApp commonMain: MediaHttpClient.kt | create() через expect-фабрику; mediaConfig() | без изменений |
| composeApp {android,desktop,wasmJs,ios}Main: MediaHttpClient.*.kt | actual: OkHttp/OkHttp/Js/Darwin(неявно) | без изменений |
| composeApp desktopTest: MediaHttpClientEngineTest.kt | новый, 1/1 зелёный (XML 758) | без изменений |
| VideoPlayerController.*, VideoScreen.kt, AppModule.kt | KtorDataSource-связка (715) | без изменений |
| memory.md | запись 715 есть | **добавить** решение follow-up + уточнение грабли ktor-версий |
| .beads/issues.jsonl | 4d1 in_progress | **обновить** статус (крит.2 закрыт, крит.3/5 блокированы) |
| .pipeline/202608270819/* | — | **создать** 02-execute/03-verify/00-report |

REST API / БД / Flyway / схемы / спеки — **не затрагиваются** (поведение плеера то же).

## 4. Затронутые API / схемы / конфиги

| Объект | Характер изменения |
|---|---|
| gradle/libs.versions.toml | уже: ktor 3.0.3, alias media3-datasource-ktor (конфиг сборки) |
| composeApp/build.gradle.kts | уже: androidMain dependency (конфиг сборки) |
| MediaHttpClient.kt + платформенные actual | уже: expect/actual-фабрика движка (код, internal) |
| MediaHttpClientEngineTest.kt | уже: новый unit-тест (desktopTest) |
| Koin-регистрация `named("media")` | не меняется |
| REST API / БД / Flyway / DTO / схемы | **не затрагиваются** |
| Спеки Part 1–3 / PRD / DESIGN_SYSTEM_SPEC | **не затрагиваются** (ADR-007 не задействуется) |
| memory.md, .beads/issues.jsonl | обновление документации/статуса |

## 5. Крайние случаи и риски

| # | Риск / кейс | Митигация |
|---|---|---|
| R1 | wasm-компиляция + configuration cache (грабля №50) | все wasm-задачи — с `--no-configuration-cache` |
| R2 | coil-network-ktor3 (coil 3.0.4) — транзитивный ktor может отличаться от 3.0.3 | deps-отчёт: nearest-wins 3.0.3; расхождение задокументировать; при критичном — constraint (не в скоупе без подтверждения) |
| R3 | ktor-client-android 3.0.3 остаётся транзитивом media3-datasource-ktor | безвреден при явном OkHttp (D4); исключение — отдельный follow-up |
| R4 | kotlin-stdlib 2.2.10 (транзитив media3) vs KGP 2.1.0 | уже проверено в 715: binary-compatible, компиляция проходит |
| R5 | :shared:allTests = NO-SOURCE — регрессия ktor-бампа только компиляционная | компиляции shared по таргетам (android/desktop/wasmJs), :core:compileKotlinDesktop, :feature-tests:* — best-effort |
| R6 | testDebugUnitTest/testReleaseUnitTest гоняют commonTest UI-тесты без desktop-окружения и падают | НЕ использовать как гейт; kover уже исключает их (build.gradle.kts) |
| R7 | В working tree несвязанные untracked (videos/, .media/, scripts/) | git add строго по списку файлов 4d1 (§7.10); чужие файлы не трогать |
| R8 | Живой гейт невыполним (нет эмулятора; dev-стек не поднят) | блокер в отчёт + точные команды (§6.6); тикет 4d1 остаётся in_progress |
| R9 | bd CLI недоступен | правка .beads/issues.jsonl вручную (только строка 4d1, JSON-валидность) |
| R10 | deps-отчёт покажет 3.0.2 от какого-то транзитива (кроме coil) | зафиксировать источник; align/constraint — отдельным решением владельца |
| R11 | Стухший gradle-демон / кривой кэш после прерванного wasm-прогона 758 | при подозрении: `--rerun-tasks` для compileKotlinWasmJs или перезапуск демона |
| R12 | Новые гейты что-то сломают (код 715/758 не менялся с последнего зелёного прогона) | риск низкий; чинить только найденное, отклонения фиксировать в 02-execute |

## 6. Тесты

1. **Новый (уже в дереве)**: MediaHttpClientEngineTest — `create()` возвращает OkHttpEngine на desktop
   (детерминизм, D2). Перезапускается в составе desktopTest.
2. **Существующие**: MediaHttpClientTest 4/4 (нет Authorization, редирект, не-2xx, таймаут-контракт),
   VideoScreenTest 6/6 — ассерты не менялись.
3. **Регрессия (полный гейт)**: `:composeApp:desktopTest` (ожидается 120/120, сверить с XML),
   `:composeApp:compileDebugKotlinAndroid`, `:composeApp:compileKotlinWasmJs --no-configuration-cache`
   (перезапуск прерванного гейта), `:app:assembleDebug`.
4. **Компиляции ktor-бампа**: `:shared:compileKotlinDesktop`, `:shared:compileDebugKotlinAndroid`,
   `:shared:compileKotlinWasmJs --no-configuration-cache`, `:core:compileKotlinDesktop`
   (+ :core/data, :feature-tests:* — best-effort по настройке модулей).
5. **Deps-отчёт (крит.1)**: `./gradlew :composeApp:dependencies --configuration androidDebugCompileClasspath`
   → все `io.ktor:*` = 3.0.3; дополнительно desktop/wasmJs compileClasspath по факту резолва.
6. **Живой гейт (крит.3/5) — окружение-зависим**: команды в §7.1; при отсутствии эмулятора — блокер.
7. **Статика**: grep androidMain — `DefaultHttpDataSource|DefaultDataSource` только в комментариях;
   grep — `createPlatformMediaHttpClient` вызывается из `create()` (явный движок).

## 7. Порядок шагов

1. **Снимок окружения**: `adb devices`, `docker ps` → зафиксировать блокер живого гейта (R8):
   команды для будущего живого прогона — `docker compose up -d` (dev-стек на 8080),
   `.\.\gradlew.bat :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/`
   (грабли №13/85), установка APK на эмулятор, флоу тема→топик→видео
   (старт/пауза/seek/replay, субтитры синхронны, retry после обрыва), проверка отсутствия
   Authorization к видео-URL (крит.4) и редиректов CDN (крит.5) в логах/прокси.
2. **Перезапуск прерванного wasm-гейта**: `:composeApp:compileKotlinWasmJs --no-configuration-cache`
   (при подозрении на кэш — R11).
3. **Основные гейты**: `:composeApp:desktopTest` (сверить 120/120 по test-results XML) →
   `:composeApp:compileDebugKotlinAndroid` → `:app:assembleDebug`.
4. **Компиляции ktor-бампа**: shared/core по таргетам (§6.4, R5).
5. **Deps-отчёт** → проверка крит.1 (единая 3.0.3); задокументировать остаточные версии (R2/R10).
6. **Статические проверки** (§6.7).
7. **memory.md**: добавить решение 2026-08-27 (follow-up 4d1): единая версия ktor 3.0.3 закрыта
   бампом каталога; явный движок через expect/actual (OkHttp/Js/Darwin) вместо ServiceLoader;
   desktopTest-сет подхватился дефолтной иерархией; уточнить граблю ktor-версий (смеси 3.0.2/3.0.3
   больше нет).
8. **`.beads/issues.jsonl`**: строка FunnyEnglish-4d1 — `updated_at` на текущее время, комментарий в
   `description`/close-логике: «крит.2 закрыт (ktor 3.0.3 единая версия + явный движок), крит.3/5 ждут
   живого гейта; тикет НЕ закрывать» (R9; JSON валидный).
9. **Артефакты пайплайна**: `02-execute.md` (dev-агент), `03-verify.md` (verify-агент, вердикт
   **ОК с блокером живого гейта**), `00-report.md` (отчёт-агент) в `.pipeline/202608270819/`.
10. **Коммит — НЕ выполнять без явного разрешения** (консервативный профиль). Предложение команд:
    `git add` строго по списку файлов 4d1 (12 M + 7 untracked 4d1-файлов; БЕЗ videos/, .media/,
    scripts/plan-execute-verify.*) и conventional message вида
    `refactor(video): единая версия Ktor 3.0.3 + явный движок медиа-клиента (bd 4d1)`.
    После согласования — `git status` на подтверждение чистоты.

## 8. Решения, требующие подтверждения

- **D1 (скоуп)**: «продолжение» = завершение follow-up 4d1 (верификация + доки + статусы + артефакты).
  Если планировалась иная задача — уточнить, план переоформим.
- **D2 (подтверждение)**: явный движок через expect/actual (OkHttp android+desktop, Js wasmJs, Darwin ios
  неявно) — уже реализован и зелёный; оставить как есть. Альтернатива (явный Darwin + compile-зависимость
  в composeApp iosMain) — не требуется.
- **D3 (подтверждение)**: бамп ktor 3.0.2→3.0.3 в каталоге — уже в дереве; оставить (единственный способ
  единой версии при транзитиве 3.0.3 от media3-datasource-ktor).
- **D4 (подтверждение)**: ktor-client-android (транзитив, 3.0.3) остаётся на classpath — безвредно при
  явном OkHttp; исключение — отдельный follow-up (не в скоупе).
- **D5 (коммит)**: не коммитить без разрешения; по согласованию — команды §7.10 (скоуп только 4d1).
- **D6 (живой гейт)**: блокирован (нет эмулятора; dev-стек не поднят, staging работает). По запросу
  владельца — поднять dev-стек и эмулятор по командам §7.1 и провести живой прогон отдельным шагом.
- Спеки не меняются; тикет 4d1 не закрывается до живого гейта (политика прогонов 715/758).

## 9. Смежные задачи (вне скоупа)

- FunnyEnglish-j8r (P4): media3-session unused — cleanup, отдельная задача (не связана с движком).
- FunnyEnglish-c47 / xic / 0zl: дизайн-конформити (решения владельца) — вне скоупа.
- `videos/sotospeak-promo/`, `.media/`, `scripts/plan-execute-verify.*` — несвязанная работа в
  working tree; не включается в коммит 4d1.
- Унификация движков за пределами медиа-клиента (SoToSpeakApi использует OkHttp неявно) — отдельная задача.
