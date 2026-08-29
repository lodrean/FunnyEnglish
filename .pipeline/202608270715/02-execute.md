# Отчёт об исполнении: FunnyEnglish-4d1 — Video: KtorDataSource

> Пайплайн: 202608270715 · Исполнитель: dev-агент · Дата: 2026-08-27
> План: .pipeline/202608270715/01-plan.md · Тикет: bd FunnyEnglish-4d1 (P3, task) — CLAIMED, гейты зелёные, живой прогон не выполнен

## 1. Что сделано

Переведён HTTP-стек Android-видеоплеера с встроенного DefaultHttpDataSource (HttpURLConnection)
на единый Ktor-стек через media3-datasource-ktor (media3 1.11.0) + общий медиа-HTTP-клиент.
Поведение плеера не менялось — правки спек не требовались (ADR-007 не задействован).

### Изменённые файлы
| Файл | Изменение |
|---|---|
| gradle/libs.versions.toml | + alias `androidx-media3-datasource-ktor` (version.ref media3) |
| composeApp/build.gradle.kts | + androidMain dep `libs.androidx.media3.datasource.ktor` |
| composeApp/src/commonMain/.../player/MediaHttpClient.kt | **новый**: фабрика медиа-клиента (expectSuccess=false, HttpRedirect, HttpTimeout connect 10s + request/socket INFINITE; БЕЗ auth/JSON) + внутренняя `mediaConfig()` для тестов |
| composeApp/src/commonMain/.../player/VideoPlayerController.kt | expect: + конструктор-параметр `httpClient: HttpClient` |
| composeApp/src/androidMain/.../player/VideoPlayerController.android.kt | actual `actual constructor(httpClient)`; `createPlayer()` → `setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(KtorDataSource.Factory(mediaClient)))` + `@OptIn(UnstableApi::class)`; клиент не закрывается |
| composeApp/src/{desktop,ios,wasmJs}Main/.../VideoPlayerController.*.kt | actual-сигнатуры (стабы игнорируют клиент) |
| composeApp/src/commonMain/.../screens/VideoScreen.kt | VideoRoute: `koinInject(named("media"))` → `remember { VideoPlayerController(mediaClient) }` |
| composeApp/src/commonMain/.../di/AppModule.kt | + `single<HttpClient>(named("media")) { MediaHttpClient.create() }` |
| composeApp/src/commonTest/.../tests/MediaHttpClientTest.kt | **новый**: 4 теста (нет Authorization; редирект; не-2xx не бросается; таймаут-контракт) |
| composeApp/src/commonTest/.../tests/VideoScreenTest.kt | конструктор + MockEngine-клиент (ассерты не менялись) |
| memory.md | + решение (2026-08-27, bd 4d1) + грабля №99 (K2 `actual constructor`) |

## 2. Ключевые находки / отклонения от плана

1. **Грабля №99 (новая, в memory.md)**: Kotlin 2.1.0 K2 — actual-класс, чей expect имеет
   конструктор с параметрами, обязан помечать конструктор `actual constructor(...)`, иначе
   `Declaration must be marked with actual`. `val`-параметры в expect-конструкторе запрещены.
   Прецедент найден в `shared` (`Settings(name: String)`). План не предусматривал этот шаг.
2. **R1 подтверждён**: ktor 3.0.2 → **3.0.3** глобально (deps-отчёт: все io.ktor:* → 3.0.3, единая версия ✅).
3. **R2 не материализовался**: kotlin-stdlib резолвится в 2.2.10 (требование media3-datasource-ktor),
   K2 2.1.0 компилирует без metadata-ошибок (binary-compatible). План-фолбэк (constraint 2.1.0) НЕ понадобился.
4. **R7 подтверждён**: `KtorDataSource.Factory(client)` компилируется без contentTypePredicate (guava не нужна).
5. **kimi CLI недоступен**: попытка запуска (review-режим) падает на старте — MCP-сервер `rag-memory` не отвечает (`Connection closed`), повторный запуск тот же результат. Код сгенерирован напрямую (разрешённый fallback) и проверен инструментами (гейты + grep + ручное ревью диффа).
6. **Таймауты**: `requestTimeoutMillis=0`/`socketTimeoutMillis=0` в Ktor 3.0.x ЗАПРЕЩЕНЫ
   (`require(value == null || value > 0)` — проверено по исходникам 3.0.2) → использован
   `HttpTimeoutConfig.INFINITE_TIMEOUT_MS` (Long.MAX_VALUE).
7. **Живой Android-гейт НЕ выполнен** (нет эмулятора и docker в сессии): старт/пауза/seek/replay,
   синхронность субтитров, retry, отсутствие Authorization в запросе к медиа-хосту — отдельным шагом
   перед закрытием 4d1 (команды в §4).

## 3. Как проверено (гейты)

| Гейт | Результат |
|---|---|
| `:composeApp:compileDebugKotlinAndroid` | ✅ BUILD SUCCESSFUL |
| `:composeApp:desktopTest` | ✅ (вкл. MediaHttpClientTest 4/4, VideoScreenTest 6/6) |
| `:composeApp:compileKotlinWasmJs --no-configuration-cache` | ✅ (грабля №50) |
| `:app:assembleDebug` | ✅ BUILD SUCCESSFUL |
| `:shared:allTests` (ktor-бамп регрессия) | ✅ BUILD SUCCESSFUL (exit 0) |
| Статическая проверка (крит. 1) | ✅ grep androidMain: DefaultHttpDataSource/DefaultDataSource — только в комментариях; единственный factory — `KtorDataSource.Factory` |
| kimi review (CLI) | ⚠️ не выполнен: `kimi` падает на старте (MCP `rag-memory`: Connection closed) — код написан напрямую (fallback по инструкции), проверен компиляцией/тестами/grep |

## 4. Как проверить вручную (живой гейт)

```powershell
# 1) docker-стек (postgres/minio/backend) + эмулятор
docker compose up -d
# 2) APK с LAN-IP хоста (грабли №13/85)
.\gradlew.bat :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/
adb install -r app/build/outputs/apk/debug/app-debug.apk
# 3) флоу: тема → топик → видео: старт/пауза/seek/replay, субтитры синхронны,
#    retry после обрыва сети; в логах/прокси убедиться, что к видео-URL
#    Authorization не отправляется (медиа-клиент без auth, R4)
```

## 5. Статус bd

- FunnyEnglish-4d1: CLAIMED (исполнение завершено, гейты зелёные; живой прогон — следующий шаг; закрыть после приёмки).
