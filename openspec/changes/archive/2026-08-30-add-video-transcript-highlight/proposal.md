# Proposal: add-video-transcript-highlight

## Why

Экран видео показывал только ОДНУ текущую строку субтитров (SubtitlePanel) — ученик не видел
весь текст видео целиком и не мог оценить объём/содержание до просмотра. Решение владельца
(2026-08-08): весь текст видео отображается сразу, а субтитры используются для плавной
пословной подсветки уже произнесённого текста (из приглушённого цвета в основной, токены ДС
для light/dark тем). Отдельная загрузка транскрипта в админке НЕ нужна — текст извлекается
из существующего `.vtt`.

Попутно (тот же план `~/.kimi/plans/red-star-riri-williams-starfire.md`): дизайн-конформити
аппбаров (мокап: без стрелки «назад», с подзаголовком-цепочкой «Тема · Топик · мета»),
фикс статус-чипов в тёмной теме, fade-обрезка длинных текстов в списках (перенос по словам,
≤3 строк, затухание вместо многоточия), реальный HTML5-видеоплеер для WASM-веб-версии
(иначе подсветке неоткуда взять позицию воспроизведения — плеер был стабом).

bd-задачи: эпик `FunnyEnglish-et4` (этапы 1–2).

## What Changes

- **WebVttParser** (composeApp, commonMain): `SubtitleCue` получает `words: List<SubtitleWord>`
  (text, startMs, endMs). Тайминги слов: karaoke-таймкоды `<mm:ss.mmm>` из VTT при наличии,
  иначе интерполяция внутри cue пропорционально длине слова. Karaoke-теги вычищаются из
  текста cue (раньше протекали в `text`, т.к. не матчились tagRegex).
- **TranscriptPanel** (новый компонент, заменяет SubtitlePanel): полный текст видео (LazyColumn
  по cue), произнесённые слова — `speaking.text`, непроизнесённые — `speaking.textMuted`,
  текущее слово — плавный `lerp` по доле прогресса + полужирный; автоскролл к активному cue;
  reduce-motion → мгновенное переключение без заливки.
- **WASM HTML5-плеер**: реальный `VideoPlayerController.wasmJs` (DOM `<video>` поверх canvas,
  позиционирование по координатам Compose-области; timeupdate → positionMs). Новый флаг
  `VideoPlayerController.supportsOverlayControls` (android=true): на wasm overlay-контролы
  невозможны (DOM поверх canvas) — control-bar рисуется ПОД плеером, Compose-оверлеи
  (big-play/replay) — только когда DOM-video скрыт. Desktop/iOS остаются стабами.
- **Аппбары по мокапу** (Topics/Video/Questions/Training/Practice): компонент `SpeakingAppBar`
  (заголовок + breadcrumb-подзаголовок, БЕЗ стрелки «назад»). Системная кнопка/жест «назад» —
  через новый expect/actual `PlatformBackHandler` (Android: activity BackHandler; поведение
  диалога-подтверждения в Practice сохранено). `libraryTitle` пробрасывается через AppScreen-роуты.
- **Чипы статусов — фикс dark theme**: ThemeStatusChip/SubmissionStatusChip и новый чип топика
  используют токены `statusNew/statusReviewed` (dark-варианты) вместо hardcoded `#256629/#8A5200`.
- **FadingEdgeText**: перенос по словам, ≤3 строк, затухание градиентом в цвет фона вместо
  многоточия — применён к названиям тем (Library) и топиков (Topics).
- **Список топиков по мокапу**: supporting — «N вопросов · видео m:ss», trailing — чип
  «пройден/новый» + chevron (ранее: длительность + иконки check/mic).
- **Админка**: превью субтитров в MediaUploader показывает полный текст транскрипта
  (`extractVttTranscript`, зеркало клиентского парсера) + подпись, что отдельный транскрипт
  не нужен. Backend не меняется.
- **CI (этап 0 того же плана)**: ci.yml (bootJar перед docker build), quality-check.yml
  (Kover-плагин в backend/composeApp + `disabledForTestTasks` для android unit-тестов;
  vitest через `npx vitest run --coverage`; `@vitest/coverage-v8` → ^2.1.4 под vitest@2;
  пороги покрытия выровнены под текущее; `security-events: write` для Trivy),
  cmp-e2e-tests.yml (chmod gradlew), chromatic.yml (skip без CHROMATIC_TOKEN),
  qa-automation.yml (S3_* env вместо MINIO_*, health-wait вместо sleep 60).

## Capabilities

### New Capabilities
- `video-transcript`: полный транскрипт видео на клиенте с пословной подсветкой по позиции
  воспроизведения; извлечение пословных таймингов из WebVTT (karaoke + интерполяция);
  HTML5-воспроизведение видео в WASM-веб-версии.

### Modified Capabilities
- `speaking-content`: превью субтитров в админке показывает полный текст транскрипта;
  отдельное поле/файл транскрипта НЕ вводится (источник текста — .vtt).

## Non-goals

- Редактор karaoke-таймкодов в админке (файлы с `<mm:ss.mmm>` готовятся внешними инструментами,
  напр. Whisper).
- Видеоплеер для desktop/iOS (остаются стабами «Видео недоступно» — отдельное решение владельца).
- Полный back stack навигации (ADR-006) — системный «назад» маппится на родительский экран точечно.

## Impact

- Код: composeApp (`subtitles/`, `player/`, `screens/{Video,Topics,Questions,Training,Practice}Screen`,
  `components/{SpeakingAppBar,FadingEdgeText,PlatformBackHandler}`, App.kt, LibraryViewModel,
  TopicsViewModel), admin-web (`MediaUploader`, `utils/vtt.ts`, `SpeakingTopicEditor`),
  workflows CI, gradle (kover).
- Тесты: WebVttParserTest (+karaoke/интерполяция), TranscriptHighlightTest (новый),
  VideoScreenTest (+транскрипт), admin vitest (+vtt).
- Спеки (ADR-007, на утверждении владельца): Part 2 (транскрипт, аппбары, wasm-плеер) и
  DESIGN_SYSTEM_SPEC (SpeakingAppBar, FadingEdgeText, status-чипы).
- e2e-cmp pixel-базлайны и Maestro-флоу требуют перекалибровки (новый экран видео, аппбары без стрелок).
