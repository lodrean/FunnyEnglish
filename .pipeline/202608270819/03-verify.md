# Верификация: FunnyEnglish-4d1 (follow-up) — единая версия Ktor 3.0.3 + явный движок медиа-клиента

> Пайплайн: 202608270819 · Верификатор: verify-агент · Дата: 2026-08-27
> План: `.pipeline/202608270819/01-plan.md` · Исполнение: `.pipeline/202608270819/02-execute.md`
> Тикет: bd FunnyEnglish-4d1 (P3, task, in_progress) · Предыдущие прогоны: 202608270715 (реализация), 202608270758 (follow-up, wasm-гейт прерван)

## Вердикт

**ОК (с блокером живого Android-гейта).** Все критерии приёмки плана, кроме окружение-зависимого
крит.5, подтверждены независимой перепроверкой. Критичных/блокирующих дефектов реализации нет.
Единственный открытый пункт — живой Android-прогон (крит.3/5), блокированный отсутствием
эмулятора и dev-стека; по политике прогонов 715/758 тикет 4d1 **не закрывается** до него.

## Что проверено (независимая перепроверка верификатора)

| # | Критерий плана | Результат | Основание |
|---|---|---|---|
| 1 | Крит.1: единая версия `io.ktor:*` = 3.0.3 | ✅ | deps-отчёты (deps-{android,desktop,wasm}.log): на `androidDebugCompileClasspath` / `desktopCompileClasspath` / `wasmJsCompileClasspath` все `io.ktor:*` = 3.0.3; остатков 3.0.2 — 0 (grep по 3.0.2 пуст). Единственное «старое» упоминание — `io.ktor:ktor-client-core:3.0.1 -> 3.0.3` от coil-network-ktor3 3.0.4 (R2), разрешается nearest-wins в 3.0.3 — не расхождение |
| 2 | Крит.2: движок детерминирован | ✅ | Код: `create()` → `createPlatformMediaHttpClient` (expect + 4 actual: OkHttp android/desktop, Js wasmJs, Darwin ios неявно); ServiceLoader — только в комментарии теста. Тест: `MediaHttpClientEngineTest` 1/1 зелёный (XML fresh 2026-08-27T08:35:55Z), `MediaHttpClientTest` 4/4 |
| 3 | Крит.3: гейты зелёные | ✅ | `desktopTest` — 120/120, 0 failures/errors (20 XML, свежий rerun 11:36 local, вкл. engine 1/1); `compileDebugKotlinAndroid` — BUILD SUCCESSFUL; `compileKotlinWasmJs --no-configuration-cache` — BUILD SUCCESSFUL (перезапуск прерванного гейта 758); `:app:assembleDebug` — BUILD SUCCESSFUL. **Независимый повтор верификатора**: `gradlew.bat :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid :app:assembleDebug :composeApp:compileKotlinWasmJs --no-configuration-cache` — BUILD SUCCESSFUL (133 tasks, 1 executed/1 from-cache/131 up-to-date — исходники не менялись с зелёных прогонов dev-агента) |
| 4 | Компиляции ktor-бампа (R5) | ✅ | gate2-bump-compile.log: `:core`, `:core:data`, `:core:domain`, `:core:presentation`, `:feature-api` — desktop+android BUILD SUCCESSFUL; `:feature-tests` — NO-SOURCE (зафиксировано, best-effort по плану); `:shared` по трём таргетам — UP-TO-DATE в составе основного гейта |
| 5 | Крит.4: статика | ✅ | `DefaultHttpDataSource`/`DefaultDataSource` в androidMain — только комментарий (VideoPlayerController.android.kt:118); `createPlatformMediaHttpClient` вызывается из `create()` (MediaHttpClient.kt:38); Koin `single<HttpClient>(named("media"))` (AppModule.kt:58); `KtorDataSource.Factory(mediaClient)` в android createPlayer |
| 6 | Крит.5: живой Android-гейт | ⚠️ **БЛОКИРОВАН** | Подтверждено: `adb devices` пуст (эмулятора нет); docker — только staging-стек (admin:3100, backend:8180, postgres:5433, minio:9100/9101, mailpit:1125/8125) + hometasks-pb:8090; dev-стек (8080) не запущен. Команды живого прогона — в 02-execute.md §4 |
| 7 | Крит.6: memory.md + .beads | ✅ | memory.md: грабля №100 (ktor: транзитив обгоняет каталог) + запись решения «Follow-up 4d1» (2026-08-27); `.beads/issues.jsonl`: 4d1 — status `in_progress`, updated_at 2026-08-27T08:34:33Z, description: крит.2 закрыт, крит.3/5 ждут живого гейта; JSON валиден |
| 8 | Крит.7: артефакты | ✅ (частично) | `02-execute.md` есть; `03-verify.md` — создан этим прогоном; `00-report.md` — ожидается от отчёт-агента после вердикта |

## Найденные проблемы / отклонения

1. **Живой Android-гейт (крит.3/5) — блокер окружения, не реализации.** adb пуст, dev-стек не поднят.
   Не является дефектом кода; тикет остаётся `in_progress` до живого прогона (политика 715/758).
2. **`00-report.md` ещё не создан** — запланированный артефакт пайплайна; должен появиться после
   этого вердикта (отчёт-агент). Не отклонение исполнения, а незавершённый шаг конвейера.
3. **kimi CLI (агентный ревью) недоступен** — MCP rag-memory: Connection closed на старте (та же ошибка,
   что в 758). Код в этом прогоне не менялся, поэтому деградация проверки отсутствует (гейты + grep +
   deps-отчёты покрывают). Зафиксировано в 02-execute.md §2.7.
4. **Предупреждения сборки (не гейт-критичны, предсуществующие)**: deprecated Gradle features
   (несовместимость с Gradle 9.0), EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA, deprecation
   `LocalLifecycleOwner` в TrainingScreen.kt — не связаны с 4d1.
5. **Рабочее дерево содержит несвязанные untracked** (`videos/`, `.media/`, `scripts/plan-execute-verify.*`)
   — при коммите 4d1 строго исключить (R7).

## Рекомендации

1. **Закрыть 4d1 только после живого Android-прогона**: поднять dev-стек (`docker compose up -d`),
   собрать APK с LAN-IP (`gradlew.bat :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/`),
   установить на эмулятор и прогнать флоу тема→топик→видео (старт/пауза/seek/replay, субтитры, retry),
   проверить отсутствие Authorization к видео-URL и работу CDN-редиректов (команды в 02-execute.md §4).
2. **При коммите** (после разрешения владельца): `git add` строго по списку файлов 4d1
   (12 M + 7 untracked 4d1 + `.pipeline/202608270819/`), БЕЗ `videos/`, `.media/`, `scripts/plan-execute-verify.*`;
   conventional message вида `refactor(video): единая версия Ktor 3.0.3 + явный движок медиа-клиента (bd 4d1)`.
3. **Отчёт-агенту**: сформировать `00-report.md` на основе этого вердикта (ОК с блокером).
4. **Опционально (отдельным follow-up, вне скоупа 4d1)**: исключение транзитива `ktor-client-android`
   с classpath (D4) и унификация движков SoToSpeakApi с медиа-клиентом.
5. Предупреждения Gradle 9.0 / expect-actual beta — кандидаты в tech-debt (не гейт 4d1).

## Вывод

План 202608270819 выполнен: крит.2 закрыт формально (единая 3.0.3 + явный движок, подтверждено
deps-отчётами, тестом движка 1/1, статикой), все гейты зелёные, память/статусы обновлены,
блокер живого гейта зафиксирован с командами. Отклонений от плана, меняющих поведение, нет;
спеки не затрагивались (ADR-007 не задействован). Осталось: живой Android-прогон → закрытие 4d1,
артефакт 00-report.md, коммит по разрешению.
