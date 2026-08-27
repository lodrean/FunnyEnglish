# Итоговый отчёт: FunnyEnglish-4d1 (follow-up) — единая версия Ktor 3.0.3 + явный движок медиа-клиента

> Пайплайн: 202608270819 · Автор: отчёт-агент · Дата: 2026-08-27
> Материалы: [01-plan.md](01-plan.md) · [02-execute.md](02-execute.md) · [03-verify.md](03-verify.md)
> Файлы решений человека (05-gate-*.md): **отсутствуют** — в этом прогоне гейты человека не оформлены
> Тикет: bd FunnyEnglish-4d1 (P3, task) — статус: in_progress (CLI bd недоступен; статус обновлён вручную)
> Предыдущие прогоны: 202608270715 (реализация, вердикт ЧАСТИЧНО) · 202608270758 (follow-up, wasm-гейт прерван)

## 1. Цель

Завершить follow-up тикета 4d1, начатый прогоном 202608270758: изменения follow-up (бамп ktor
3.0.2→3.0.3 в каталоге, expect/actual-фабрика движка медиа-клиента, тест движка) уже лежали в
working tree, но follow-up не был доведён — wasm-гейт прерван, memory.md / .beads / verify / report
не обновлены. Цель этого прогона: (1) формально закрыть крит.2 прогона 715 (единая версия Ktor 3.0.3
на всех classpath — deps-отчёт); (2) подтвердить детерминированный выбор движка медиа-клиента
(OkHttp/Js/Darwin) кодом и тестом; (3) перезапустить прерванный wasm-гейт и догнать остальные гейты;
(4) обновить memory.md и .beads/issues.jsonl; (5) зафиксировать блокер живого Android-гейта
(крит.3/5) с командами; (6) оформить артефакты пайплайна 202608270819.
Поведение плеера не меняется; спеки (Part 1–3, PRD, DESIGN_SYSTEM_SPEC) не затрагиваются (ADR-007 не задействуется).

## 2. Что сделано

Кодовых изменений в этом прогоне **нет** — код follow-up (715+758) не менялся, догонялись только
гейты, документы и статусы:

| Файл | Изменение |
|---|---|
| gradle/libs.versions.toml (уже в дереве) | ktor 3.0.3 + alias media3-datasource-ktor — проверен deps-отчётом |
| composeApp: MediaHttpClient.kt + actual (android/desktop/wasmJs/iosMain) | expect/actual-фабрика движка (OkHttp/OkHttp/Js/Darwin неявно) — проверена статикой и тестом |
| composeApp desktopTest: MediaHttpClientEngineTest.kt | тест детерминизма движка — 1/1 зелёный |
| memory.md | + грабля №100 (ktor: транзитив обгоняет каталог — смесь версий; фикс: бамп каталога + deps-отчёт) + запись решения «Follow-up 4d1: единая версия Ktor 3.0.3 + явный движок» |
| .beads/issues.jsonl | FunnyEnglish-4d1: updated_at → 2026-08-27T08:34:33Z; крит.2 ЗАКРЫТ, крит.3/5 ждут живого гейта; тикет НЕ закрывать; status остаётся in_progress |
| .pipeline/202608270819/* | + gate1-main.log, gate2-bump-compile.log, gate3-desktopTest-rerun.log, deps-{android,desktop,wasm}.log, 02-execute.md, 03-verify.md, 00-report.md |

Решения плана применены: D2 (явный движок через expect/actual — оставлен как есть), D3 (бамп
ktor 3.0.3 в каталоге — оставлен), D4 (транзитив ktor-client-android остаётся — безвреден при явном
OkHttp), D5 (коммит НЕ выполнялся), D6 (живой гейт блокирован окружением).

## 3. Результаты проверок

| Гейт / проверка | Результат |
|---|---|
| :composeApp:compileKotlinWasmJs --no-configuration-cache (перезапуск прерванного гейта 758) | ✅ BUILD SUCCESSFUL (общий запуск 1m14s; грабля №50 учтена) |
| :composeApp:desktopTest | ✅ 120/120, 0 failures/errors (20 XML, вкл. MediaHttpClientEngineTest 1/1, MediaHttpClientTest 4/4) + свежий rerun ✅ |
| :composeApp:compileDebugKotlinAndroid | ✅ BUILD SUCCESSFUL |
| :app:assembleDebug | ✅ BUILD SUCCESSFUL (APK собран) |
| Компиляции ktor-бампа: :core / :core:data / :core:domain / :core:presentation / :feature-tests (desktop+android) | ✅ BUILD SUCCESSFUL (:feature-tests = NO-SOURCE, best-effort по плану R5) |
| :shared по трём таргетам | ✅ UP-TO-DATE в составе основного гейта |
| Крит.1 (deps-отчёт, единая 3.0.3) | ✅ на androidDebugCompileClasspath / desktopCompileClasspath / wasmJsCompileClasspath все io.ktor:* = **3.0.3**; остатков 3.0.2 — 0; coil-network-ktor3 (транзитив) тоже резолвится в 3.0.3 (nearest-wins) |
| Крит.2 (детерминизм движка) | ✅ create() → createPlatformMediaHttpClient (expect + 4 actual: OkHttp android/desktop, Js wasmJs, Darwin ios неявно); тест engine is OkHttpEngine — 1/1; ServiceLoader — только в комментарии |
| Крит.4 (статика) | ✅ DefaultHttpDataSource/DefaultDataSource в androidMain — только комментарий (VideoPlayerController.android.kt:118); Koin single<HttpClient>(named("media")); KtorDataSource.Factory(mediaClient) |
| Независимый повтор верификатора | ✅ gradlew :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid :app:assembleDebug :composeApp:compileKotlinWasmJs --no-configuration-cache — BUILD SUCCESSFUL (133 tasks, исходники не менялись) |
| Крит.5 (живой Android-гейт) | ❌ **БЛОКИРОВАН окружением**: adb пуст (эмулятора нет); docker — только staging-стек (admin:3100, backend:8180, postgres:5433, minio:9100, mailpit:8125) + несвязанный hometasks-pb:8090; dev-стек (8080) не запущен |
| Код-ревью (kimi CLI) | ⚠️ не выполнен: kimi падает на старте (MCP rag-memory: Connection closed — та же ошибка, что в 758); код не менялся, покрыто гейтами + grep + deps-отчётами |

Вердикт верификатора: **ОК (с блокером живого Android-гейта)** — все критерии приёмки, кроме
окружение-зависимого крит.5, подтверждены независимой перепроверкой; критичных/блокирующих
дефектов реализации нет.

## 4. Отклонения

1. **Живой Android-гейт (крит.3/5) — блокер окружения, не реализации.** Эмулятор отсутствует
   (adb devices пуст), dev-стек не поднят (работает только staging). По политике прогонов 715/758
   тикет 4d1 **не закрывается** до живого прогона. Команды — в 02-execute.md §4.
2. **kimi CLI (агентное ревью) недоступен** — MCP rag-memory: Connection closed на старте (то же в 758).
   Код в этом прогоне не менялся, деградации проверки нет (гейты + grep + deps-отчёты покрывают).
3. **Предупреждения сборки (предсуществующие, не гейт-критичны)**: deprecated Gradle features
   (несовместимость с Gradle 9.0), EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA, deprecation
   LocalLifecycleOwner в TrainingScreen.kt — не связаны с 4d1.
4. **Рабочее дерево содержит несвязанные untracked** (videos/, .media/, scripts/plan-execute-verify.*)
   — при коммите 4d1 строго исключить (R7).
5. **Файлы решений человека (05-gate-*.md) отсутствуют** — план был DRAFT «на согласование человека»,
   но исполнение прошло без документированного гейта (как и в прогоне 715); скоуп D1 («продолжение» =
   завершение follow-up 4d1) принят без возражений.
6. Отклонений от плана, меняющих поведение, **нет**; спеки не затрагивались.

## 5. Как проверить результат

```powershell
# Автоматические гейты (повтор верификатора)
.\gradlew.bat :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid :app:assembleDebug :composeApp:compileKotlinWasmJs --no-configuration-cache

# Крит.1 — единая версия Ktor 3.0.3 (все io.ktor:* = 3.0.3, остатков 3.0.2 нет)
.\gradlew.bat :composeApp:dependencies --configuration androidDebugCompileClasspath

# Крит.4 — статика
grep -rn "DefaultHttpDataSource\|DefaultDataSource" composeApp/src/androidMain

# Живой гейт (крит.3/5) — команды из 02-execute.md §4
docker compose up -d
.\gradlew.bat :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/
adb install -r app/build/outputs/apk/debug/app-debug.apk
# флоу: тема → топик → видео: старт/пауза/seek/replay, субтитры синхронны, retry после обрыва;
# в логах/прокси убедиться, что к видео-URL Authorization НЕ отправляется и CDN-редиректы работают
```

## 6. Что осталось

1. **Живой Android-гейт (крит.3/5)** — обязателен перед закрытием 4d1: поднять dev-стек + эмулятор,
   собрать APK с LAN-IP, прогнать флоу тема→топик→видео (старт/пауза/seek/replay, субтитры, retry),
   проверить отсутствие Authorization к видео-URL и работу CDN-редиректов.
2. **Коммит** — НЕ выполнялся (консервативный профиль, D5): по разрешению владельца git add
   строго по списку файлов 4d1 (12 M + 7 untracked 4d1 + .pipeline/202608270819), БЕЗ videos/,
   .media/, scripts/plan-execute-verify.*; message вида
   refactor(video): единая версия Ktor 3.0.3 + явный движок медиа-клиента (bd 4d1).
3. **bd**: тикет FunnyEnglish-4d1 остаётся in_progress (CLI bd недоступен; статус ведётся вручную
   в .beads/issues.jsonl); закрыть после живого гейта.
4. **Опционально (отдельным follow-up, вне скоупа 4d1)**: исключение транзитива ktor-client-android
   с classpath (D4); унификация движков SoToSpeakApi с медиа-клиентом.
5. Предупреждения Gradle 9.0 / expect-actual beta — кандидаты в tech-debt.

## 7. Итог

Follow-up 4d1 завершён: крит.2 закрыт формально (единая версия Ktor 3.0.3 на всех classpath,
подтверждено deps-отчётами; остатков 3.0.2 нет), движок медиа-клиента детерминирован
(expect/actual: OkHttp/Js/Darwin + тест 1/1), прерванный wasm-гейт перезапущен и зелёный,
все автоматические гейты зелёные (desktopTest 120/120, assembleDebug OK), память и статус тикета
обновлены. Отклонений, меняющих поведение, нет; спеки не затрагивались. Статус — **ОК с блокером**:
единственный открытый пункт — живой Android-гейт (крит.3/5), блокированный окружением
(нет эмулятора, dev-стек не поднят); тикет 4d1 не закрывается до живого прогона.
