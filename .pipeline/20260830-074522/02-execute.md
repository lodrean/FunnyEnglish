# 02-execute — bd FunnyEnglish-c47: Video — субтитры в карточке, CTA после карточки (V1/V2)

## Что сделано

Аудит DC-A1, строки V1/V2 (`docs/qa/design-conformance/REPORT_ANDROID_2026-08-10.md`):
мокап `frame-video` — транскрипт в белой карточке под плеером (`.subtitle`: `background: var(--color-surface)`,
`border-radius: var(--radius-button)`=16, `box-shadow: var(--shadow-card)`), CTA «Перейти к вопросам» сразу после карточки.
В приложении транскрипт был plain (без карточки), CTA прижата к низу через `Modifier.weight(1f)`.

Изменения в `VideoScreen.kt` (только обычный режим, `!isFullscreen`):

1. **V1**: `TranscriptPanel` обёрнут в `ElevatedCard` — `shape = MaterialTheme.shapes.medium` (16dp = radius-button),
   `elevation = 1.dp` (shadow-card → M3 ElevatedCard level1), `containerColor = colorScheme.surface` (белый),
   горизонтальные отступы 16dp. Внутри карточки транскрипт ограничен по высоте `heightIn(max = transcriptMaxHeight)`
   и скроллится внутри, как раньше.
2. **V2**: убраны `Modifier.weight(1f)` у транскрипта и `Spacer(weight(1f))`-заглушка — CTA идёт сразу после карточки
   (отступ 12dp = `--space-m` из мокапа), свободное место остаётся под hint'ом. Добавлен `Spacer(12.dp)` между
   плеером и карточкой (gap `.video-body` = space-m).
3. Новый bound `transcriptMaxHeight = (maxHeight - videoMaxHeight - 220.dp).coerceAtLeast(96.dp)` в `BoxWithConstraints`:
   LazyColumn в колонке без weight иначе получил бы infinite constraints; bound также гарантирует, что CTA не уезжает
   за экран на низких viewport'ах (220dp ≈ chips 64 + CTA 56 + hint ~40 + отступы).

Полноэкранный overlay-режим (`video_subtitle_overlay`, memory.md §5 решение 2026-08-12) НЕ затронут —
все правки внутри ветки `if (!isFullscreen)`; fullscreen-рендер (overlay-субтитры, контролы, edge-to-edge) без изменений.

Тест-теги (`transcript_panel`, `go_to_questions_button`, `video_hint`) сохранены — commonTest `VideoScreenTest`
(проверки по тегам) не требует изменений.

Спеки/PRD не тронуты: изменение чисто UI-композиции, контракты экрана (CTA всегда доступен, transcript с пословной
подсветкой, mode-chips) сохранены. ADR-007 не требуется.

## Изменённые файлы

- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/VideoScreen.kt` (единственный)

Созданные файлы: отсутствуют.

## Как проверить

- Гейты драйвера: `:composeApp:desktopTest`, `:composeApp:compileDebugKotlinAndroid`, `:composeApp:compileKotlinWasmJs` (--no-configuration-cache).
- Визуально: `./gradlew :composeApp:run` (desktop) или APK → экран Video топика с субтитрами:
  транскрипт в белой скруглённой карточке под плеером, CTA сразу под карточкой (не внизу экрана);
  без субтитров — CTA сразу под плеером; fullscreen — без изменений (субтитры overlay поверх видео).
- Дизайн-аудит: maestro `audit_guest.yaml` + `python e2e-cmp/compare-android-mockups.py` (строка Video: V1/V2 → ok).
