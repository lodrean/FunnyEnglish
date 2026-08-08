# Диффы спек на утверждение владельца (ADR-007) — транскрипт + аппбары

Дата: 2026-08-08. План: `~/.kimi/plans/red-star-riri-williams-starfire.md`.
OpenSpec change: `openspec/changes/add-video-transcript-highlight/` (proposal + delta-specs + tasks).
Реализация уже выполнена и проверена (desktopTest, wasm live); спеки отстают от кода —
по SDD требуется bump. **Спеки НЕ правились — ждём утверждения.**

## 1. `docs/SPEAKING_TRAINER_SPEC_PART2.md` → v1.6 (minor)

**§3.2 (Видеоплеер)** — дополнить:
- WASM: стаб заменён на реальный HTML5-плеер. UI wasm — canvas-only, поэтому `<video>`
  живёт в DOM поверх canvas и позиционируется по координатам Compose-области плеера.
  Следствие: overlay-контролы поверх видео на wasm невозможны → control-bar под плеером,
  big-play/replay показываются только когда DOM-video скрыт. Клик по видео — play/pause.
  `VideoPlayerController.supportsOverlayControls` (android=true, остальные=false).
  Desktop/iOS — стабы (без изменений).

**§3.3 (Субтитры/WebVTT)** — заменить описание панели:
- Вместо построчной SubtitlePanel — **TranscriptPanel**: полный текст видео сразу на экране
  (LazyColumn по cue), произнесённые слова — `speaking.text`, непроизнесённые — `textMuted`,
  текущее слово — плавный lerp по прогрессу внутри слова + полужирный; автоскролл к активному
  cue; reduce-motion → мгновенное переключение.
- Пословные тайминги: karaoke-таймкоды `<mm:ss.mmm>` при наличии; иначе интерполяция
  внутри cue пропорционально длине слова. Отдельного поля/файла транскрипта НЕ вводится.

**§2.2–2.6 (Экраны)** — аппбары по мокапу:
- Без стрелки «назад»; подзаголовок-цепочка: Topics — «N топиков · выбери и начни говорить»;
  Video — «{тема} · видео m:ss»; Questions — «{тема} · {топик} · N вопросов»;
  Training/Practice — «{тема} · {топик}».
- «Назад» — системная кнопка/жест (Android: BackHandler; wasm: Escape/BrowserBack);
  подтверждение выхода в Practice (§6.1) сохраняется.

## 2. `docs/DESIGN_SYSTEM_SPEC.md` → v3.1 (minor)

- **Новый компонент SpeakingAppBar**: title (titleMedium, ExtraBold) + sub (labelSmall,
  textMuted, SemiBold), без navigationIcon.
- **Новый компонент FadingEdgeText**: перенос по словам, max 3 строки, при переполнении —
  затухание градиентом в цвет фона (БЕЗ многоточия). Применён в списках тем/топиков.
- **Статус-чипы**: текст чипов — токены `statusNew`/`statusReviewed` (dark-варианты
  `#FFB74D`/`#81C784` на контейнерах `#3D2A0A`/`#1B4D1F`); hardcoded `#256629`/`#8A5200`
  запрещены (нечитаемы в dark theme — зафиксированный баг).
- **Список топиков**: supporting-текст «N вопросов · видео m:ss», trailing — чип
  «пройден/новый» + chevron (по mockups.html frame-topics).

## 3. `docs/SPEAKING_TRAINER_SPEC_PART3.md` → v1.3 (patch)

- §4.1 MediaUploader: превью .vtt показывает полный текст транскрипта (склейка cue без
  таймингов/тегов) + подпись «отдельный транскрипт не нужен».

## Ченджлоги (добавить в соответствующие документы после утверждения)

- Part 2 v1.6 (2026-08-08): TranscriptPanel вместо SubtitlePanel; пословные тайминги
  (karaoke/интерполяция); wasm HTML5-плеер; аппбары без стрелки с breadcrumb.
- DESIGN_SYSTEM_SPEC v3.1 (2026-08-08): SpeakingAppBar, FadingEdgeText, токены статус-чипов.
- Part 3 v1.3 (2026-08-08): превью полного текста транскрипта в MediaUploader.
