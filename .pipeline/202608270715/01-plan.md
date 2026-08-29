# План: FunnyEnglish-4d1 — Video: KtorDataSource — единый HTTP-стек ExoPlayer с API-слоем

> Статус: **DRAFT — на согласование человека** (планировщик конвейера)
> Тикет: bd FunnyEnglish-4d1 (P3, task, OPEN) · follow-up к FunnyEnglish-did (closed)
> Проект: So to Speak (FunnyEnglish) · Модуль: composeApp (Android), gradle/libs.versions.toml
> Автор: планировщик конвейера · Пайплайн: 202608270715 · Дата: 2026-08-27

---

## 1. Цель и критерии приёмки

**Цель.** Убрать дублирующий HTTP-стек видеоплеера: сейчас ExoPlayer на Android стримит
видео через встроенный DefaultHttpDataSource (свой HTTP-стек поверх HttpURLConnection),
тогда как весь остальной сетевой слой проекта (API, субтитры WebVTT) уже на Ktor 3.0.2.
Перевести VideoPlayerController.android на KtorDataSource.Factory (media3-datasource-ktor)
с общим Ktor-клиентом/конфигом. Спеки (Part 2 §3.2) описывают поведение плеера, а не стек
HTTP — **правка спек не требуется** (ADR-007 не задействуется; при несогласии владельца —
отдельный шаг с диффом).

**Критерии приёмки (Gate REVIEW_OK + QA_PASS):**
1. Android-плеер стримит видео через Ktor: в androidMain нет импортов/использования
   DefaultHttpDataSource / DefaultDataSource (проверка grep + ревью).
2. Единая версия Ktor во всём проекте (dependencies report: io.ktor:* — одна версия).
3. Поведение не хуже текущего на живом прогоне (эмулятор + docker-стек):
   старт/пауза/seek/replay, субтитры синхронны, retry после сетевой ошибки.
4. JWT **не** утекает на медиа-хост: в исходящем запросе к видео-URL нет Authorization
   (медиа-клиент без auth; паттерн getTextResource уже снимает заголовок — сохраняем принцип).
5. Редиректы работают (dev MinIO — прямые URL; prod CDN может отдавать 3xx).
6. Гейты: :composeApp:compileDebugKotlinAndroid, :composeApp:desktopTest,
   :composeApp:compileKotlinWasmJs, :app:assembleDebug — зелёные; новые unit-тесты — зелёные.
7. memory.md дополнен (решение + грабли).

## 2. Факты из исследования (read-only)

- VideoPlayerController.android.kt:108-113: ExoPlayer.Builder(context).build() — дефолтный
  DefaultHttpDataSource внутри ExoPlayer; MediaItem.fromUri(url) + prepare().
- VideoPlayerController — expect class БЕЗ параметров; создаётся в VideoScreen.kt:660
  (remember { VideoPlayerController() }), НЕ в Koin; actual: android (реальный),
  desktop/ios (стабы), wasmJs (HTML5 <video>).
- VideoScreenTest.kt:153 (commonTest → desktop) конструирует VideoPlayerController().
- HTTP-клиент API: SoToSpeakApi создаёт ПРИВАТНЫЙ HttpClient с defaultRequest
  { url(baseUrl); contentType(JSON); header(Authorization) }, expectSuccess=true,
  HttpTimeout 30/10/30s. getTextResource(url) (WebVTT) явно удаляет Authorization —
  медиа-хосту токен не отправляется.
- core содержит HttpClientFactory (два дубля: core/network и core/data/network — грабля из
  memory.md) с ContentNegotiation/JSON + HttpTimeout 30s, НО composeApp НЕ зависит от :core
  (commonMain: только projects.shared + projects.design), а coreModule (Koin) в приложении
  не загружается (App() → modules(appModule)).
- Каталог версий: ktor=3.0.2, coroutines=1.9.0, kotlin=2.1.0, media3=1.11.0;
  media3-datasource-ktor в каталоге ОТСУТСТВУЕТ и НЕТ в gradle cache → нужен сетевой fetch.
- POM media3-datasource-ktor 1.11.0 (проверен с Google Maven): ktor-client-core:3.0.3,
  ktor-client-android:3.0.3, media3-common/datasource:1.11.0, kotlin-stdlib:2.2.10 (compile),
  kotlinx-coroutines-core:1.9.0 (runtime), androidx.annotation:1.6.0.
- API (javap по AAR 1.11.0): androidx.media3.datasource.ktor.KtorDataSource implements
  HttpDataSource (open/read/close, getResponseCode/getResponseHeaders, setRequestProperty);
  KtorDataSource.Factory implements HttpDataSource.Factory:
  Factory(httpClient, userAgent=…, contentTypePredicate=…, transferListener=…) — синтетический
  конструктор с mask подтверждает дефолты; точный состав дефолтов проверить на спайке.

## 3. Изменения по подсистемам

### 3.1 gradle/libs.versions.toml + composeApp/build.gradle.kts
- Каталог: androidx-media3-datasource-ktor = { module = "androidx.media3:media3-datasource-ktor", version.ref = "media3" }.
- androidMain.dependencies: implementation(libs.androidx.media3.datasource.ktor).
- Побочный эффект резолва: ktor 3.0.2 → **3.0.3** глобально (patch-бамп, same-minor;
  перегнать гейты shared/composeApp). kotlinx-coroutines 1.9.0 совпадает с каталогом ✅.
  kotlin-stdlib: прямой dependency KGP 2.1.0 побеждает транзитивную 2.2.10 (nearest-wins) —
  проверить фактический резолв на спайке (риск R2).

### 3.2 composeApp commonMain — медиа-клиент (новый)
- com.sotospeak.app.player.MediaHttpClient (фабрика): create(): HttpClient для стриминга —
  движок платформенный (Android — OkHttp из androidMain), HttpRedirect (редиректы CDN),
  HttpTimeout (connect 10s; socket/request — без жёсткого лимита или крупный, чтобы длинные
  сегменты не рвались), БЕЗ ContentNegotiation/JSON, БЕЗ defaultRequest/auth,
  expectSuccess=false (KtorDataSource сам обрабатывает статусы).
- Koin: в appModule — single<HttpClient>(named("media")) { MediaHttpClient.create() }
  (appModule — единственный загружаемый модуль; coreModule не трогаем).

### 3.3 VideoPlayerController — конструктор + androidMain
- expect class VideoPlayerController(httpClient: HttpClient) (commonMain).
- Все 4 actual получают параметр; desktop/ios/wasm — игнорируют (поведение стабов не меняется).
- VideoRoute (VideoScreen.kt:660): val mediaClient: HttpClient = koinInject(named("media"));
  remember { VideoPlayerController(mediaClient) }.
- createPlayer() (androidMain):
  ExoPlayer.Builder(context).setMediaSourceFactory(
    DefaultMediaSourceFactory(context).setDataSourceFactory(KtorDataSource.Factory(mediaClient)))
  .build() + @OptIn(UnstableApi::class) на createPlayer(). MediaItem.fromUri остаётся.
- release() НЕ закрывает HttpClient (Koin-single, общий жизненный цикл).

### 3.4 Тесты
- Новый MediaHttpClientTest (commonTest, ktor-client-mock уже в deps): запрос без
  Authorization; редирект; таймаут-конфиг.
- Обновить VideoScreenTest.kt:153 (передать клиент; в тесте — HttpClient(MockEngine {...})).

## 4. Затронутые API / схемы / конфиги

| Объект | Характер изменения |
|---|---|
| gradle/libs.versions.toml | + alias media3-datasource-ktor |
| composeApp/build.gradle.kts | + androidMain dependency |
| VideoPlayerController.kt (expect) | + конструктор-параметр HttpClient |
| VideoPlayerController.{android,desktop,ios,wasmJs}.kt | сигнатуры actual |
| VideoScreen.kt (VideoRoute) | koinInject + передача клиента |
| di/AppModule.kt | + single<HttpClient>(named("media")) |
| VideoScreenTest.kt | обновление конструктора |
| REST API / БД / миграции / схемы | **не затрагиваются** |
| Спеки (Part 1-3, DESIGN_SYSTEM_SPEC, PRD) | **не затрагиваются** (поведение плеера то же) |

## 5. Крайние случаи и риски

| # | Риск / кейс | Митигация |
|---|---|---|
| R1 | ktor 3.0.2 → 3.0.3 глобально (транзитив) | patch-бамп; desktopTest + shared-тесты; dependencies report |
| R2 | kotlin-stdlib 2.2.10 (требование media3) vs компилятор 2.1.0 — возможен metadata-конфликт | спайк: проверить фактический резолв и компиляцию; fallback — constraint stdlib 2.1.0 или бамп Kotlin (отдельное решение) |
| R3 | Артефакт не в gradle cache — сборка требует сети | зафиксировано; офлайн-сборка невозможна до первого fetch |
| R4 | Утечка JWT на медиа-хост | медиа-клиент без auth (D1); НЕ переиспользовать приватный клиент SoToSpeakApi |
| R5 | Редиректы (CDN 3xx) | HttpRedirect в медиа-клиенте; OkHttp-engine редиректы не обрабатывает — проверить на живом прогоне |
| R6 | Socket-timeout рвёт длинные/медленные сегменты | socket timeout не задавать (или >60s); live-проверка |
| R7 | guava Predicate<String> в сигнатуре Factory | вызывать Factory(client) без contentTypePredicate (дефолт); guava не нужна; подтвердить на спайке |
| R8 | @UnstableApi на setMediaSourceFactory / KtorDataSource | @OptIn на createPlayer() |
| R9 | Ошибки сети / не-2xx → поведение UI | KtorDataSource.open бросает HttpDataSourceException → PlaybackException → существующий error-overlay/retry (UI не меняется) |
| R10 | release() раньше времени убьёт общий клиент | клиент живёт в Koin (single); контроллер его не закрывает |
| R11 | URL с query (presigned S3) | Ktor/OkHttp не трогают query; регрессия на живом прогоне |
| R12 | Грабли №50 (wasm — --no-configuration-cache), №26 (admin E2E не параллелить с wasm-сборкой) | учитывать при прогоне гейтов |
| R13 | Быстрый вход/выход с экрана видео (пересоздание контроллера) | Factory создаёт DataSource на каждый open — переиспользование клиента безопасно |

## 6. Тесты

1. **Спайк (шаг 0)**: :composeApp:compileDebugKotlinAndroid с новой зависимостью —
   подтверждает R1/R2/R7 и дефолты Factory.
2. **Unit (новые)**: MediaHttpClientTest — отсутствие Authorization, редирект, таймауты
   (commonTest, MockEngine; гоняется в :composeApp:desktopTest).
3. **Обновлённые**: VideoScreenTest (новый конструктор) — 5 UI-кейсов, ассерты не меняются.
4. **Регрессия**: :composeApp:desktopTest, :composeApp:compileKotlinWasmJs,
   :composeApp:compileDebugKotlinAndroid, :app:assembleDebug; при желании :shared:allTests (ktor-бамп).
5. **Живой гейт** (эмулятор + docker-стек): старт/пауза/seek/replay, субтитры синхронны,
   retry после обрыва; отсутствие Authorization в запросе к видео-URL (логи/прокси);
   e2e-cmp wasm smoke (навигация не сломана).
6. **Статическая проверка**: grep по androidMain — нет DefaultHttpDataSource/DefaultDataSource.

## 7. Порядок шагов

1. Спайк: каталог + dependency → compileDebugKotlinAndroid; зафиксировать фактические версии
   ktor/stdlib; подтвердить KtorDataSource.Factory(client) (R1/R2/R7).
2. MediaHttpClient (commonMain) + single<HttpClient>(named("media")) в appModule.
3. Конструктор VideoPlayerController (expect + 4 actual) + VideoRoute + VideoScreenTest.
4. createPlayer(): setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(KtorDataSource.Factory(client))).
5. MediaHttpClientTest (MockEngine).
6. Гейты компиляции/тестов (см. §6.4) — чинить найденное.
7. Живой прогон Android (видео + субтитры + retry) и e2e-cmp smoke; проверка отсутствия Authorization.
8. memory.md: решение (единый Ktor-стек для видео; медиа-клиент без auth) + грабли (ktor-бамп,
   stdlib-нюанс, fetch-зависимость артефакта); bd — закрыть 4d1 после приёмки.

## 8. Решения, требующие подтверждения

- **D1 (рекомендация)**: медиа-клиент — отдельный HttpClient (named "media") без auth/JSON.
  Альтернатива «переиспользовать приватный клиент SoToSpeakApi» отклонена (утечка JWT на
  медиа-хост); «переиспользовать core.HttpClientFactory» требует новой зависимости :core и
  доработки фабрики (JSON + socket 30s не подходят для стриминга) — не рекомендуется.
- **D2**: конструкторная инъекция HttpClient в VideoPlayerController (все таргеты) — принято;
  альтернатива (глобальный holder в androidMain) хуже (mutable global).
- **D3**: ktor-бамп 3.0.2 → 3.0.3 в рамках задачи (patch) — по умолчанию принято; при
  возражениях — constraint ktor=3.0.2 и проверка совместимости media3-модуля с 3.0.2.
- Спеки не меняются (поведение плеера то же); при ином мнении владельца — дифф спек через
  OpenSpec/ADR-007 отдельным шагом.

## 9. Смежные задачи (вне скоупа)

- FunnyEnglish-j8r (P4): media3-session не используется, media3-ui (PlayerView) остался —
  не трогаем; наш рефакторинг не зависит и не мешает.
- FunnyEnglish-c47 / xic / 0zl — дизайн-конформити (решения владельца), вне скоупа.
