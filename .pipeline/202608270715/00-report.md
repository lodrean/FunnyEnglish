# Итоговый отчёт: FunnyEnglish-4d1 — Video: KtorDataSource

> Пайплайн: 202608270715 · Автор: отчёт-агент · Дата: 2026-08-27
> Материалы: [01-plan.md](01-plan.md) · [02-execute.md](02-execute.md) · [03-verify.md](03-verify.md)
> Файлы решений человека (05-gate-*.md): **отсутствуют** — в этом прогоне гейты человека не оформлены
> Тикет: bd FunnyEnglish-4d1 (P3, task) — статус: in_progress (CLI bd в сессиях execute/verify недоступен)

## 1. Цель

Убрать дублирующий HTTP-стек Android-видеоплеера: ExoPlayer стримил видео через встроенный
`DefaultHttpDataSource` (HttpURLConnection), тогда как весь остальной сетевой слой проекта
(API, субтитры WebVTT) уже на Ktor. Задача — перевести `VideoPlayerController.android` на
`KtorDataSource.Factory` (media3-datasource-ktor 1.11.0) с общим медиа-HTTP-клиентом, не меняя
поведение плеера (спеки не затрагиваются, ADR-007 не задействуется).

Критерии приёмки (из плана): (1) в androidMain нет использования DefaultHttpDataSource/DefaultDataSource;
(2) единая версия Ktor; (3) поведение не хуже текущего на живом прогоне; (4) JWT не утекает на
медиа-хост; (5) редиректы работают; (6) гейты компиляции/тестов зелёные; (7) memory.md дополнен.

## 2. Что сделано

Реализация соответствует плану §3; применены решения D1 (отдельный медиа-клиент без auth/JSON),
D2 (конструкторная инъекция HttpClient), D3 (patch-бамп ktor). Изменено 12 файлов:

| Файл | Изменение |
|---|---|
| gradle/libs.versions.toml | + alias `androidx-media3-datasource-ktor` (media3 1.11.0) |
| composeApp/build.gradle.kts | + androidMain dep `libs.androidx.media3.datasource.ktor` |
| MediaHttpClient.kt (commonMain, новый) | фабрика медиа-клиента: expectSuccess=false, HttpRedirect, таймауты connect 10s + request/socket INFINITE; БЕЗ auth/JSON; общая `mediaConfig()` для тестов |
| VideoPlayerController.kt (expect) | + конструктор-параметр `httpClient: HttpClient` |
| VideoPlayerController.{android,desktop,ios,wasmJs}.kt | actual-сигнатуры (стабы игнорируют клиент) |
| VideoScreen.kt (VideoRoute) | `koinInject(named("media"))` → передача клиента в контроллер |
| di/AppModule.kt | + `single<HttpClient>(named("media"))` |
| MediaHttpClientTest.kt (commonTest, новый) | 4 теста: нет Authorization, редирект, не-2xx не бросается, таймаут-контракт |
| VideoScreenTest.kt | конструктор + MockEngine-клиент (ассерты не менялись) |
| memory.md | решение 2026-08-27 (bd 4d1) + грабля №99 |

Android `createPlayer()`: `ExoPlayer.Builder(context).setMediaSourceFactory(
DefaultMediaSourceFactory(context).setDataSourceFactory(KtorDataSource.Factory(mediaClient)))`
+ `@OptIn(UnstableApi::class)`; контроллер клиент НЕ закрывает (клиент живёт в Koin, R10).

## 3. Результаты проверок

| Гейт / проверка | Результат |
|---|---|
| :composeApp:compileDebugKotlinAndroid | ✅ BUILD SUCCESSFUL |
| :composeApp:desktopTest | ✅ 119/119, 0 failures (MediaHttpClientTest 4/4, VideoScreenTest 6/6) |
| :composeApp:compileKotlinWasmJs --no-configuration-cache | ✅ (грабля №50 учтена) |
| :app:assembleDebug | ✅ BUILD SUCCESSFUL |
| :shared:allTests (ktor-бамп) | ✅ exit 0, но **NO-SOURCE** — регрессия только компиляционная |
| Крит.1 (grep androidMain) | ✅ DefaultHttpDataSource/DefaultDataSource — только в комментариях; единственный factory — KtorDataSource.Factory |
| Крит.4 (JWT на медиа-хост) | ✅ по коду: медиа-клиент без auth, Koin-single отделён от SoToSpeakApi; живой запрос НЕ проверен |
| Крит.7 (memory.md) | ✅ решение + грабля №99 внесены |
| Код-ревью (kimi CLI) | ⚠️ не выполнен: CLI падает на старте (MCP rag-memory: Connection closed) — fallback: гейты + grep + ручное ревью диффа |
| Крит.2 (единая версия Ktor) | ⚠️ формально НЕ закрыт: на classpath смесь 3.0.2 (прямые артефакты каталога) + 3.0.3 (транзитив media3) |
| Крит.3/5 (живой Android-гейт) | ❌ НЕ выполнен (нет эмулятора/docker в сессиях execute и verify) |

Вердикт верификатора: **ЧАСТИЧНО** — код соответствует плану, все автоматические гейты зелёные,
ревью и статическая проверка пройдены; до живого прогона тикет не закрывать.

## 4. Отклонения

1. **Грабля №99** (не в плане): K2 2.1.0 требует `actual constructor(...)` для actual-класса с
   параметризованным expect-конструктором; `val`-параметры в expect-конструкторе запрещены.
   Задокументировано в memory.md.
2. **ktor 3.0.2 → 3.0.3** (R1, patch-бамп): единой версии на classpath фактически НЕТ —
   прямые артефакты каталога остались на 3.0.2, транзитив media3-datasource-ktor тянет 3.0.3
   (patch-совместимо, риск низкий; утверждение dev-отчёта «все io.ktor:* → 3.0.3» неточно).
3. **kotlin-stdlib 2.2.10** (транзитив media3) против компилятора 2.1.0: R2 НЕ материализовался —
   компиляция проходит (binary-compatible), план-фолбэк не понадобился.
4. **Таймауты**: `0` запрещён в Ktor 3.0.x (`require(value > 0)`) → `INFINITE_TIMEOUT_MS` (Long.MAX_VALUE).
5. **Два Ktor-движка на Android-classpath**: ktor-client-okhttp 3.0.2 (декларирован) +
   ktor-client-android 3.0.3 (транзитив) — движок выбирается ServiceLoader неявно; рекомендуется
   указывать явно.
6. **Живой Android-гейт не выполнен** (крит. 3/5 — обязательные критерии приёмки).
7. **:shared:allTests = NO-SOURCE**: dev-отчёт завысил его как регрессию; фактически ktor-бамп
   покрыт только компиляцией, без тестового покрытия.
8. **Решения человека не зафиксированы**: файлы 05-gate-*.md в пайплайне отсутствуют (план был
   помечен DRAFT «на согласование человека», но исполнение прошло без документированного гейта);
   отклонения dev-агента (№1, №4, kimi) приняты в верификации.

## 5. Как проверить результат

```powershell
# Статическая проверка крит.1
grep -rn "DefaultHttpDataSource\|DefaultDataSource" composeApp/src/androidMain

# Автоматические гейты
./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:desktopTest
./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache
./gradlew :app:assembleDebug

# Живой гейт (крит. 3/5) — команды из 02-execute §4
docker compose up -d
./gradlew :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/
adb install -r app/build/outputs/apk/debug/app-debug.apk
# флоу: тема → топик → видео: старт/пауза/seek/replay, субтитры синхронны,
# retry после обрыва сети; в логах/прокси убедиться, что к видео-URL
# Authorization НЕ отправляется (крит. 4); редиректы CDN (крит. 5)
```

## 6. Что осталось

1. **Живой Android-гейт** (крит. 3/5) — обязателен перед закрытием 4d1.
2. **Унификация Ktor до 3.0.3** (бамп каталога или constraint) + перегон гейтов — закрывает крит.2,
   убирает смесь версий и второй движок (рекомендация верификатора).
3. **Явный движок** в `MediaHttpClient.create()` (например `HttpClient(OkHttp)`) — убрать
   зависимость от ServiceLoader.
4. **bd**: тикет FunnyEnglish-4d1 остаётся in_progress (CLI bd в сессиях execute/verify недоступен);
   закрыть вручную после живого гейта (и, рекомендовано, унификации ktor).
5. При ином мнении владельца по «спеки не меняются» — дифф спек через OpenSpec/ADR-007 отдельным шагом.

## 7. Итог

Код и автоматические гейты готовы, дельта от плана минимальна и задокументирована (грабля №99,
таймауты INFINITE, ktor-бамп). Статус — **ЧАСТИЧНО**: живой Android-гейт (крит. 3/5) не выполнен,
крит. 2 формально открыт (смесь ktor 3.0.2/3.0.3). После живого прогона и унификации ktor статус → ОК.
