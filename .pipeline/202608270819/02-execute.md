# Отчёт об исполнении: FunnyEnglish-4d1 (follow-up) — верификация Ktor 3.0.3 + явный движок

> Пайплайн: 202608270819 · Исполнитель: dev-агент · Дата: 2026-08-27
> План: .pipeline/202608270819/01-plan.md · Тикет: bd FunnyEnglish-4d1 (P3, task, in_progress)
> Предыдущие прогоны: 202608270715 (реализация) · 202608270758 (follow-up: бамп+движок, wasm-гейт прерван)

## 1. Что сделано

Кодовых изменений в этом прогоне НЕТ — изменения follow-up (ktor=3.0.3 в каталоге,
expect/actual-фабрика движка, тест движка) уже лежали в working tree (прогоны 715/758).
Прогон 202608270819 завершил follow-up 4d1: догнал прерванный wasm-гейт, закрыл крит.2
(единая версия Ktor) формально, зафиксировал блокер живого гейта, обновил память/статусы.

### Изменённые файлы (этот прогон)
| Файл | Изменение |
|---|---|
| memory.md | + грабля №100 (ktor: транзитив обгоняет каталог — смесь версий; фикс: бамп каталога + deps-отчёт) + запись решения «Follow-up 4d1: единая версия Ktor 3.0.3 + явный движок» |
| .beads/issues.jsonl | FunnyEnglish-4d1: updated_at → 2026-08-27T08:34:33Z; в description: крит.2 ЗАКРЫТ (ktor 3.0.3 единая версия + явный движок), крит.3/5 ждут живого гейта, тикет НЕ закрывать; status остаётся in_progress |
| .pipeline/202608270819/* | + gate1-main.log, gate2-bump-compile.log, gate3-desktopTest-rerun.log, deps-{android,desktop,wasm}.log, 02-execute.md |

## 2. Ключевые находки / отклонения от плана

1. **Крит.2 ЗАКРЫТ формально.** deps-отчёты (:composeApp:dependencies, androidDebugCompileClasspath /
   desktopCompileClasspath / wasmJsCompileClasspath): ВСЕ io.ktor:* = **3.0.3**, остатков 3.0.2 — 0
   (после бампа каталога в 758 nearest-wins убрал смесь, найденную verify-прогоном 715).
   coil-network-ktor3 (R2) транзитивно тоже 3.0.3 — расхождений нет.
2. **Движок медиа-клиента детерминирован** (D2): create() → createPlatformMediaHttpClient (expect +
   4 actual: OkHttp android/desktop, Js wasmJs, Darwin ios неявно). MediaHttpClientEngineTest
   (desktopTest): client.engine is OkHttpEngine — 1/1 зелёный (XML 758 + свежий rerun).
3. **Wasm-гейт перезапущен** (прерван в 758 на :design:compileKotlinWasmJs):
   :composeApp:compileKotlinWasmJs --no-configuration-cache — BUILD SUCCESSFUL (грабля №50 соблюдена).
4. **desktopTest**: прогон 758 — 120/120, 0 failures/errors (20 XML-файлов, newest 08:16:34Z);
   в этом прогоне UP-TO-DATE (исходники не менялись) + выполнен свежий rerun (gate3 log).
5. **Компиляции ktor-бампа** (R5/R7): :core / :core:data / :core:domain / :core:presentation /
   :feature-tests — desktop+android compile — BUILD SUCCESSFUL; :feature-tests = NO-SOURCE (пустой,
   best-effort как в плане); :shared по трём таргетам — UP-TO-DATE в составе основного гейта.
6. **Живой Android-гейт (крит.3/5) — БЛОКИРОВАН окружением** (R8): adb devices пуст (эмулятора нет);
   docker — daemon работает, поднят только staging-стек (admin:3100, backend:8180, postgres:5433,
   minio:9100, mailpit:8125) + несвязанный hometasks-pb:8090; dev-стек (8080) НЕ запущен.
   Тикет 4d1 НЕ закрывается до живого прогона (команды §4).
7. **kimi CLI**: доступен (v1.47.0), НО агентный запуск падает на старте — MCP rag-memory:
   Connection closed (та же ошибка, что в прогоне 758). Кодогенерация в этом прогоне не требовалась
   (код не менялся) — fallback на прямую проверку инструментами (гейты + grep + deps-отчёты).
8. **Статика** (крит.4): grep androidMain — DefaultHttpDataSource/DefaultDataSource — только в комментарии;
   create() идёт через createPlatformMediaHttpClient (явный движок, не ServiceLoader);
   Koin single<HttpClient>(named("media")), VideoScreen инжектит named("media").

## 3. Как проверено (гейты)

| Гейт | Результат |
|---|---|
| :composeApp:compileKotlinWasmJs --no-configuration-cache (перезапуск) | ✅ BUILD SUCCESSFUL (общий запуск 1m14s) |
| :composeApp:desktopTest | ✅ 120/120, 0 failures/errors (XML: 20 файлов, вкл. MediaHttpClientEngineTest 1/1, MediaHttpClientTest 4/4) + свежий rerun ✅ |
| :composeApp:compileDebugKotlinAndroid | ✅ BUILD SUCCESSFUL |
| :app:assembleDebug | ✅ BUILD SUCCESSFUL (APK собран) |
| Компиляции ktor-бампа (core/core:data/core:domain/core:presentation/feature-tests, desktop+android) | ✅ BUILD SUCCESSFUL (feature-tests NO-SOURCE) |
| shared: compileKotlinDesktop / compileDebugKotlinAndroid / compileKotlinWasmJs | ✅ (UP-TO-DATE в составе основного гейта) |
| deps-отчёт (крит.1) | ✅ все io.ktor:* = 3.0.3 (android/desktop/wasm compile classpath; 3.0.2: 0) |
| Статическая проверка (крит.4/2) | ✅ grep + чтение фабрики движка |
| kimi review (CLI) | ⚠️ не выполнен: kimi падает на старте (MCP rag-memory: Connection closed) — код не менялся, проверен гейтами/grep/deps |

## 4. Как проверить вручную (живой гейт — крит.3/5, остаётся открытым)

```powershell
# 1) поднять dev-стек (backend на 8080) + эмулятор
docker compose up -d
# 2) APK с LAN-IP хоста (грабли №13/85)
.\gradlew.bat :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.x.x:8080/
adb install -r app/build/outputs/apk/debug/app-debug.apk
# 3) флоу: тема → топик → видео: старт/пауза/seek/replay, субтитры синхронны,
#    retry после обрыва; в логах/прокси убедиться, что к видео-URL
#    Authorization НЕ отправляется (медиа-клиент без auth) и CDN-редиректы работают
```

## 5. Статус bd

- FunnyEnglish-4d1: **in_progress** (обновлён вручную в .beads/issues.jsonl — CLI bd недоступен).
  Крит.2 закрыт (ktor 3.0.3 единая версия + явный движок, подтверждено deps-отчётами и тестом движка).
  Крит.3/5 (живой Android-гейт) ждут эмулятора + dev-стека. **Тикет НЕ закрывать** до живого прогона.
- Коммит НЕ выполнялся (консервативный профиль; план §7.10 — только по разрешению).
  Предложение: git add по списку файлов 4d1 (12 M + 7 untracked 4d1 + .pipeline/202608270819),
  БЕЗ videos/, .media/, scripts/plan-execute-verify.*; message вида
  refactor(video): единая версия Ktor 3.0.3 + явный движок медиа-клиента (bd 4d1).
