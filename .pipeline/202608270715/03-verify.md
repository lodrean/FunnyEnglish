# Вердикт верификации: FunnyEnglish-4d1 — Video: KtorDataSource

> Пайплайн: 202608270715 · Верификатор: verify-агент · Дата: 2026-08-27
> План: .pipeline/202608270715/01-plan.md · Отчёт разработчика: .pipeline/202608270715/02-execute.md
> Тикет: bd FunnyEnglish-4d1 (P3, task, in_progress в .beads/issues.jsonl; CLI bd в сессии недоступен)

## Статус: **ЧАСТИЧНО**

Реализация соответствует плану, все автоматические гейты зелёные, код-ревью и
статическая проверка пройдены. НЕ выполнен живой Android-гейт (критерий приёмки 3 и 5)
и формально не закрыт критерий 2 (единая версия Ktor). До живого прогона тикет
**не закрывать**.

## 1. Что подтверждено (повторный прогон верификатором)

| Проверка | Результат |
|---|---|
| Код-ревью: 12 изменённых файлов (MediaHttpClient, expect+4 actual, VideoRoute, AppModule, каталог, билд, 2 теста, memory.md) | соответствует §3 плана; D1/D2/D3 применены |
| Крит.1: grep androidMain — DefaultHttpDataSource/DefaultDataSource | только в комментариях, импортов/использования нет |
| :composeApp:compileDebugKotlinAndroid | BUILD SUCCESSFUL (UP-TO-DATE, кэш валиден) |
| :composeApp:desktopTest | 119/119, 0 failures (MediaHttpClientTest 4/4, VideoScreenTest 6/6 — по test-results XML) |
| :composeApp:compileKotlinWasmJs --no-configuration-cache | BUILD SUCCESSFUL |
| :app:assembleDebug | BUILD SUCCESSFUL |
| :shared:allTests (ktor-бамп) | exit 0, но NO-SOURCE — в shared тесты не настроены; регрессия = только компиляция (см. §2.4) |
| Крит.4: JWT на медиа-хост | по коду: media-клиент без auth/JSON, Koin single(named "media") отделён от SoToSpeakApi; живой запрос не проверен |
| Крит.7: memory.md | грабля №99 + запись решения 2026-08-27 |

## 2. Найденные проблемы / отклонения

1. **Крит.2 «единая версия Ktor» НЕ закрыт формально.** deps-отчёт
   (:composeApp:dependencies, androidDebugCompileClasspath): core-граф io.ktor:*
   резолвится в 3.0.3 (media3-datasource-ktor тянет 3.0.3), НО прямые артефакты каталога
   остались на 3.0.2: ktor-client-okhttp, ktor-client-content-negotiation,
   ktor-client-logging, ktor-serialization-kotlinx-json. На classpath смесь 3.0.2+3.0.3
   (patch-совместимы, риск низкий), но утверждение отчёта «все io.ktor:* → 3.0.3» неточно.
2. **Крит.3/5 (живой гейт) не выполнен**: старт/пауза/seek/replay, синхронность субтитров,
   retry после обрыва, редиректы CDN, отсутствие Authorization в реальном исходящем
   запросе. Причина — нет эмулятора/docker в сессиях execute и verify. Это обязательные
   критерии приёмки — остаются открытыми.
3. **Два Ktor-движка на Android-classpath**: ktor-client-okhttp 3.0.2 (декларирован) +
   ktor-client-android 3.0.3 (транзитив media3-datasource-ktor). `HttpClient {}` в
   MediaHttpClient.create() выбирает движок через ServiceLoader — неявно. Работает, но
   лучше указать движок явно.
4. **Отчёт dev завышает :shared:allTests**: задача «регрессия ktor-бампа» фактически
   компиляционная (NO-SOURCE), тестов в shared нет — при бампе это не ловит рантайм.
5. Отклонения, задокументированные dev-ом (принимаются): грабля №99 (K2 actual
   constructor — план не предусматривал), таймауты INFINITE_TIMEOUT_MS вместо 0 (Ktor
   require>0), kimi недоступен (код проверен гейтами + grep + ручным ревью диффа).

## 3. Рекомендации

1. **Выполнить живой Android-гейт** перед закрытием 4d1 (команды в 02-execute §4):
   docker-стек + эмулятор, флоу видео, проверка отсутствия Authorization к видео-URL
   (R4) и редиректов (R5), retry (R6), seek/replay, субтитры.
2. **Унифицировать ktor**: поднять каталог `ktor = "3.0.3"` (или constraint) — закрыть
   крит.2 и убрать смесь версий; перегнать гейты.
3. **Явно указать движок** в MediaHttpClient.create() (например `HttpClient(OkHttp)`
   на android/desktop) — убрать зависимость от ServiceLoader при двух движках на classpath.
4. В отчётах фиксировать, что `:shared:allTests` = NO-SOURCE (регрессия ktor — только
   компиляция, без тестового покрытия).
5. bd: тикет 4d1 остаётся in_progress; закрыть после живого гейта и (рекомендуемо)
   унификации ktor. CLI bd в сессии верификатора недоступен — закрытие вручную.

## 4. Итог

Код и автоматические гейты — готовы (дельта плана минимальна и задокументирована).
Статус «частично» из-за невыполненного живого Android-гейта (крит. 3/5) и формально
незакрытого крит.2. После живого прогона + унификации ktor статус → ОК.
