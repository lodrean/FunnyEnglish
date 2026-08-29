# План: FunnyEnglish-4d1 (follow-up) — унификация Ktor 3.0.3 + явный движок медиа-клиента + закрытие крит. приёмки

> Статус: **DRAFT — на согласование человека** (планировщик конвейера)
> Пайплайн: 202608270758 · Тикет: bd FunnyEnglish-4d1 (P3, task, in_progress) — follow-up к прогону 202608270715
> Проект: So to Speak (FunnyEnglish) · Ветка: develop (изменения прогона 202608270715 НЕ закоммичены, лежат в working tree)
> Автор: планировщик конвейера · Дата: 2026-08-27

---

## 0. Предположение о скоупе (требует подтверждения)

Промпт планировщика не содержал описания задачи («возьми следующую задачу в работу»). По контексту
конвейера «следующая задача» = **закрытие открытых критериев приёмки FunnyEnglish-4d1**, оставленных
верификатором прогона 202608270715 (статус ЧАСТИЧНО):
крит.2 «единая версия Ktor» + рекомендации верификатора (явный движок медиа-клиента, убрать смесь
движков/версий) + перегон гейтов + подготовка живого Android-гейта (крит.3/5) + memory.md + статус bd.
Если подразумевалась другая задача — план переоформим по уточнению (сводка уходит человеку).

## 1. Цель и критерии приёмки

**Цель.** Закрыть формально открытые пункты прогона 202608270715 без изменения поведения плеера:
(1) привести прямые зависимости Ktor каталога к фактически резолвленной транзитивной версии 3.0.3 —
устранить смесь 3.0.2/3.0.3 на classpath (крит.2); (2) сделать выбор HTTP-движка медиа-клиента явным
вместо неявного ServiceLoader при двух движках на Android-classpath (рекомендация верификатора);
(3) перегнать гейты компиляции/тестов; (4) зафиксировать состояние живого Android-гейта (крит.3/5)
с командами; (5) обновить memory.md и статус bd 4d1. Спеки не затрагиваются (поведение плеера то же,
ADR-007 не задействуется).

**Критерии приёмки (Gate REVIEW_OK + QA_PASS):**
1. dependencies-отчёт: на androidDebugCompileClasspath все прямые io.ktor:* — одна версия **3.0.3**
   (никаких 3.0.2); то же проверить для desktop/wasm classpath'ов по факту резолва.
2. Выбор движка медиа-клиента детерминирован: android/desktop — OkHttp явно (не ServiceLoader);
   wasmJs — Js явно; ios — единственный движок Darwin (остаётся неявным, обоснование в §8 D2).
3. Код клиента: MediaHttpClient.create() по-прежнему собирает конфиг через mediaConfig()
   (expectSuccess=false, HttpRedirect, таймауты connect 10s + request/socket INFINITE) — контракт тестов не меняется.
4. Гейты зелёные: :composeApp:desktopTest (119 существующих + новый engine-тест),
   :composeApp:compileDebugKotlinAndroid, :composeApp:compileKotlinWasmJs --no-configuration-cache,
   :app:assembleDebug, :shared: компиляции (ktor-бамп) — см. §6.4.
5. Поведение не хуже текущего: живые проверки (старт/пауза/seek/replay, субтитры, retry, отсутствие
   Authorization к видео-URL, редиректы) — **выполняются при наличии эмулятора+docker**; в текущей
   сессии окружение отсутствует (adb пуст, docker daemon не запущен) → критерий фиксируется как
   «блокирован до окружения», тикет 4d1 **не закрывается** до живого прогона (как в прогоне 202608270715).
6. memory.md дополнен (решение + уточнение грабли ktor-версий); bd 4d1 — статус обновлён.

## 2. Факты из исследования (read-only, состояние working tree на 2026-08-27)

- Каталог gradle/libs.versions.toml: ktor = "3.0.2"; alias androidx-media3-datasource-ktor
  (version.ref media3=1.11.0) уже добавлен прогоном 202608270715.
- media3-datasource-ktor 1.11.0 тянет транзитивно ktor-client-core **3.0.3** + ktor-client-android **3.0.3**
  (проверено POM в прогоне 202608270715) → на Android-classpath СЕЙЧАС смесь: прямые артефакты каталога
  3.0.2 (ktor-client-okhttp, content-negotiation, logging, serialization-kotlinx-json), транзитив 3.0.3.
  Крит.2 формально НЕ закрыт (верификатор: «утверждение отчёта неточно»).
- **Два движка на Android-classpath**: ktor-client-okhttp 3.0.2 (декларирован в androidMain composeApp/shared)
  + ktor-client-android 3.0.3 (транзитив media3-datasource-ktor). MediaHttpClient.create()
  (composeApp/src/commonMain/.../player/MediaHttpClient.kt) вызывает HttpClient { mediaConfig() } —
  движок выбирается ServiceLoader неявно (недетерминированно какой из двух).
- MediaHttpClient (commonMain, object): create() + mediaConfig() (internal, переиспользуется тестами
  с MockEngine): expectSuccess=false, HttpRedirect, HttpTimeout connect 10_000 + request/socket
  INFINITE_TIMEOUT_MS (Long.MAX_VALUE, т.к. 0 запрещён в Ktor 3.0.x).
- Koin: appModule → single<HttpClient>(named("media")) { MediaHttpClient.create() }; контроллер плеера
  клиент не закрывает (R10 из прошлого плана). VideoPlayerController (expect + 4 actual) и VideoScreen.kt
  (VideoRoute, koinInject named "media") — уже на конструкторе, **не трогаются**.
- Ktor-потребители каталога (бамп 3.0.3 затронет): shared (core/auth/logging/json + okhttp/darwin/js по
  таргетам), core, core/data, feature-tests, composeApp (common/android/desktop/wasm + mock в commonTest),
  feature-home (только coil-network-ktor, внешний). backend — Spring, Ktor не использует.
- Движки в composeApp: androidMain ktor-client-okhttp (compile-виден), desktopMain ktor-client-okhttp
  (compile-виден), wasmJsMain ktor-client-js (compile-виден, объект io.ktor.client.engine.js.Js),
  iosMain — darwin НЕ объявлен в composeApp (только runtime-транзитив из shared, compile НЕ виден).
- Состояние тестов: :composeApp:desktopTest — **119/119, 0 failures** (последний прогон 2026-08-27,
  вкл. MediaHttpClientTest 4/4 и VideoScreenTest 6/6, XML в build/test-results). :shared:allTests —
  NO-SOURCE (тестов в shared нет; регрессия ktor-бампа = только компиляция).
- Окружение для живого гейта: adb devices — пусто; docker ps — daemon не запущен → **живой Android-гейт
  в этой сессии невыполним** (как и в прогоне 202608270715).
- git: ветка develop, изменения прогона 202608270715 не закоммичены (12 файлов, +61/−15 + 2 новых файла);
  .pipeline/202608270715/{00-report,01-plan,02-execute,03-verify}.md — эталон формата артефактов.

## 3. Изменения по подсистемам

### 3.1 gradle/libs.versions.toml — бамп Ktor (крит.2)
- ktor = "3.0.2" → "3.0.3" (patch-бамп; выравнивает прямые артефакты с уже резолвленным транзитивом
  3.0.3 от media3-datasource-ktor). Побочно подтягивает ktor-client-mock (тестовая зависимость) до 3.0.3.
- Никаких других правок каталога. kotlin=2.1.0, coroutines=1.9.0, media3=1.11.0 — не меняются.

### 3.2 composeApp commonMain — явный движок медиа-клиента (D2)
- MediaHttpClient.kt: create() меняется на фабрику движка через expect/actual (документированный
  Ktor-KMP-шаблон с HttpClientConfig<*>.() -> Unit):
  - commonMain (expect, internal): createPlatformMediaHttpClient(config)
  - androidMain / desktopMain (actual): HttpClient(OkHttp) { config(this) } — io.ktor.client.engine.okhttp.OkHttp
  - wasmJsMain (actual): HttpClient(Js) { config(this) } — io.ktor.client.engine.js.Js
  - iosMain (actual): HttpClient { config(this) } — движок не указываем (Darwin единственный на runtime)
- mediaConfig() не меняется; Koin-регистрация, VideoPlayerController, VideoScreen — не меняются.

### 3.3 Новый тест на движок
- composeApp/src/desktopTest/.../player/MediaHttpClientEngineTest.kt (desktopTest source set, иерархия
  наследует commonTest-зависимости): val client = MediaHttpClient.create(); assert
  client.engine is OkHttpEngine (io.ktor.client.engine.okhttp.OkHttpEngine); client.close().
  Сетевых запросов нет (только конструкция + тип движка). Если desktopTest source set не подхватывается
  иерархией по умолчанию — объявить val desktopTest by getting в sourceSets (мелкая правка билда).

## 4. Затронутые API / схемы / конфиги

| Объект | Характер изменения |
|---|---|
| gradle/libs.versions.toml | ktor 3.0.2 → 3.0.3 (версия каталога, все alias'ы сразу) |
| composeApp/.../player/MediaHttpClient.kt | create() → через expect-фабрику движка; + expect fun |
| composeApp/src/{androidMain,desktopMain,wasmJsMain}/.../player/MediaHttpClient.*.kt | actual-фабрики (новые файлы в платформенных source set'ах) |
| composeApp/src/desktopTest/.../MediaHttpClientEngineTest.kt | новый тест |
| composeApp/build.gradle.kts | только при необходимости: явный val desktopTest by getting |
| VideoPlayerController.*, VideoScreen.kt, AppModule.kt | **не меняются** |
| REST API / БД / Flyway / схемы | **не затрагиваются** |
| Спеки (Part 1–3, DESIGN_SYSTEM_SPEC, PRD) | **не затрагиваются** (поведение плеера то же) |
| shared / core / core:data / feature-tests | компиляционные (резолв ktor 3.0.3), кода не меняется |

## 5. Крайние случаи и риски

| # | Риск / кейс | Митигация |
|---|---|---|
| R1 | ktor 3.0.3 на desktop/wasm/iOS — ранее 3.0.3 был только на Android-classpath; новые поверхности компиляции | гейты: desktopTest + compileKotlinWasmJs + компиляции shared (ios/darwin 3.0.3) |
| R2 | coil-network-ktor3 (coil 3.0.4) — транзитивный ktor может отличаться от 3.0.3 | deps-отчёт по composeApp (nearest-wins: прямой 3.0.3 побеждает); расхождение задокументировать |
| R3 | HttpClientConfig<*>.() -> Unit — вариантность в expect/actual | документированный Ktor-шаблон; проверяется компиляцией android/desktop/wasm |
| R4 | Смена движка Android: был неявный (возможно ktor-client-android 3.0.3), станет явный OkHttp | оба движка Ktor-совместимы; редиректы обрабатывает HttpRedirect-плагин (уже установлен); HTTP/1.1+2/TLS одинаковы; живой гейт — финальная проверка |
| R5 | ktor-client-android 3.0.3 остаётся на classpath (транзитив media3-datasource-ktor) | безвреден при явном OkHttp; исключение транзитива — опциональный follow-up (не в скоупе, риск сломать media3-модуль) |
| R6 | WASM-задачи и configuration cache (грабля №50) | все wasm-компиляции — с --no-configuration-cache |
| R7 | :shared:allTests = NO-SOURCE — регрессия ktor-бампа только компиляционная | фиксировать в отчёте; обязательны компиляции shared по таргетам |
| R8 | testDebugUnitTest/testReleaseUnitTest (android) гоняют commonTest UI-тесты без desktop-окружения и падают | НЕ использовать как гейт; kover уже исключает их (build.gradle.kts) |
| R9 | Рабочее дерево содержит незакоммиченные изменения прогона 202608270715 | execute-агент работает поверх текущего working tree; без явного разрешения НЕ коммитить/пушить (консервативный профиль) |
| R10 | Новый desktopTest source set может не подхватиться | явная регистрация val desktopTest by getting при необходимости; гейт desktopTest покажет |
| R11 | Живой гейт невыполним в этой сессии (нет эмулятора/docker) | шаг §7.8 фиксирует блокер и точные команды; тикет 4d1 остаётся in_progress; статус прогона — ЧАСТИЧНО |
| R12 | Старые версии в gradle cache / офлайн | резолв уже происходил (3.0.3 в кэше от прогона 202608270715) — риск низкий |

## 6. Тесты

1. **Новый (desktopTest)**: MediaHttpClientEngineTest — MediaHttpClient.create() возвращает клиент с
   явным движком OkHttpEngine (детерминированность, D2). Без сети.
2. **Существующие (не меняются)**: MediaHttpClientTest 4/4 (нет Authorization, редирект, не-2xx,
   таймаут-контракт — через общий mediaConfig() + MockEngine); VideoScreenTest 6/6.
3. **Регрессия**: :composeApp:desktopTest (полный, ожидается 119 + 1 новый); :composeApp:compileDebugKotlinAndroid;
   :composeApp:compileKotlinWasmJs --no-configuration-cache; :app:assembleDebug.
4. **Компиляции ktor-бампа (shared/core)**: :shared:compileDebugKotlinAndroid, :shared:compileKotlinDesktop,
   :shared:compileKotlinWasmJs --no-configuration-cache (+ :core:compileKotlinDesktop, :core/data:... по факту
   настройки модулей); :feature-tests:* — best-effort (модуль пустой/не подключён).
5. **Deps-отчёт**: ./gradlew :composeApp:dependencies --configuration androidDebugCompileClasspath —
   единственная версия io.ktor:* = 3.0.3 (крит.1); при желании :shared:dependencies.
6. **Живой гейт (крит.3/5) — окружение-зависим**: команды в §7.8; при отсутствии окружения — фиксируется блокер.
7. **Статическая проверка**: grep по androidMain — по-прежнему нет DefaultHttpDataSource/DefaultDataSource
   (регрессия не вносится); git diff ограничен ожидаемыми файлами (§4).

## 7. Порядок шагов

1. Проверка окружения живого гейта: adb devices, docker ps → если нет, зафиксировать блокер (R11).
2. Бамп каталога: gradle/libs.versions.toml ktor → 3.0.3.
3. MediaHttpClient.kt (commonMain): expect-фабрика движка + create() через неё.
4. Файлы actual: androidMain/desktopMain (OkHttp), wasmJsMain (Js); iosMain — actual без движка (D2).
5. Новый тест MediaHttpClientEngineTest (desktopTest; при необходимости зарегистрировать source set).
6. Гейты: deps-отчёт (крит.1) → desktopTest → compileDebugKotlinAndroid → compileKotlinWasmJs
   (--no-configuration-cache) → :app:assembleDebug → компиляции shared/core (R1/R7) → чинить найденное.
7. memory.md: решение (2026-08-27, follow-up 4d1): единая версия ktor 3.0.3 закрыта бампом каталога;
   явный движок OkHttp/Js (детерминизм вместо ServiceLoader при двух движках); уточнение грабли ktor-версий.
8. Живой гейт (при наличии окружения): docker compose up -d; .\gradlew.bat :app:assembleDebug
   -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/ (грабли №13/85); adb install -r; флоу
   тема → топик → видео (старт/пауза/seek/replay, субтитры синхронны, retry после обрыва);
   в логах/прокси — отсутствие Authorization к видео-URL; редиректы CDN. Иначе — блокер в отчёт.
9. bd: обновить FunnyEnglish-4d1 (крит.2 закрыт, крит.3/5 — ждут живого гейта; тикет не закрывать;
   CLI bd может быть недоступен — правка .beads/issues.jsonl только вручную при необходимости).

## 8. Решения, требующие подтверждения

- **D1 (скоуп)**: «следующая задача» = follow-up 4d1 (бамп ktor + явный движок + гейты + подготовка живого
  гейта). Если планировалась иная задача (например, из открытых bd: c47/xic/0zl — дизайн-конформити,
  j8r — cleanup media3-session) — уточнить, план переоформим.
- **D2 (рекомендация)**: явный движок через expect/actual фабрику: OkHttp (android+desktop), Js (wasmJs);
  ios — неявный (единственный движок Darwin на runtime, compile-зависимость в composeApp не добавляем).
  Альтернатива — полная явность: добавить implementation(libs.ktor.client.darwin) в composeApp iosMain
  и actual с Darwin; плюс полная детерминированность, минус лишняя зависимость/скоуп.
- **D3 (по умолчанию принято)**: бамп ktor 3.0.2 → 3.0.3 в каталоге (patch) — единственный способ получить
  единую версию при транзитиве 3.0.3 от media3-datasource-ktor; альтернатива (constraint 3.0.2 + исключение
  транзитива) рискованнее и не даёт ничего, кроме сохранения старых патчей.
- **D4**: ktor-client-android (транзитив, 3.0.3) остаётся на classpath при явном OkHttp — безвредно;
  удаление (exclude транзитива media3-datasource-ktor) — отдельный follow-up, не в скоупе.
- Спеки не меняются; живой гейт — обязателен перед закрытием 4d1 (перенос из прогона 202608270715).

## 9. Смежные задачи (вне скоупа)

- FunnyEnglish-j8r (P4): media3-session unused + media3-ui в каталоге — cleanup, не связан с данным
  изменением (движок медиа-клиента не зависит от media3-session).
- FunnyEnglish-c47 / xic / 0zl: дизайн-конформити — решения владельца, вне скоупа (как в прогоне 202608270715).
- Унификация движков за пределами медиа-клиента (SoToSpeakApi использует OkHttp на android/desktop
  неявно) — отдельная задача при желании.
