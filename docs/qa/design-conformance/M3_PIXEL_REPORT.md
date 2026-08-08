# M3 Pixel-отчёт — сверка mockups v2.0 ↔ реализация (DSM-7)

**Дата:** 2026-08-08 · **Тикет:** bd `FunnyEnglish-dmb` (DSM-7)
**Материалы:** мокапы v2.0 — `e2e-cmp/test-results/pixel-report/mockups-{light,dark}/` (15 фреймов × 2 темы);
приложение (WASM, M3) — `e2e-cmp/test-results/bg-audit/{light,dark}/` (11 экранов × 2 темы),
`e2e-cmp/test-results/mobile-audit/` (390×844), эмулятор — `.playwright-mcp/` (Training/Video/replay вживую).

**Решения владельца 2026-08-08:** Q5 — эталонно приложение (мокап правит дизайн-агент);
dark onPrimary/onSecondary = `#1A2F5E` (утверждено; в DSM-5 таблице был #FFFFFF — WCAG FAIL).

## Сводная таблица по фреймам

| # | Фрейм | Вердикт | Комментарий |
|---|---|---|---|
| 1 | Library | ✅ совпадает | ElevatedCard темы, chip НОВАЯ/ПРОЙДЕНО (container+тёмный текст), LinearProgress 4dp, pill-индикатор nav. Wide: NavigationRail (Q4, утверждено) |
| 2 | Topics | ✅ совпадает | M3 ListItem в карточке (play/название/длительность/статусы), back-arrow |
| 3 | Video | ✅ совпадает | Чипы субтитров FilterChip, контролы play/seek/time/CC #FFD666; **+ replay «Начать заново»** (добавлено 2026-08-08 по требованию владельца — дополнить мокап) |
| 4 | Questions | ⚠️ по решению Q5 | CTA эталонно из приложения: «Тренировка · 3 попытки»/«Практика · 30 сек»; мокап v2.0 («Начать Training · 80 сек»/«Сразу Practice») — править. Структура карточек однородные (приложение), gate-карточка — Filled Card |
| 5 | Training | ✅ совпадает | AssistChip уровня (timer-цвета), точки попыток, таймер-кольцо brand, rec-кнопка squircle + state layers, плашка хранения surfaceContainerLow. Проверено на эмуляторе (Maestro) |
| 6 | Practice | ✅ по e2e | Автостоп 30с → автоотправка → Sent → MySubmissions (Maestro practice_auth). Жёлтая плашка statusNewContainer внизу; 409 → Snackbar |
| 7 | MySubmissions | ✅ совпадает | ListItem + AssistChip NEW/REVIEWED; locked-гость — FilledCard + CTA |
| 8–10 | Onboarding / Login / Register | ✅ совпадает | Filled CTA, M3 page-dots (primary pill), OutlinedTextField label-в-бордере, supportingText ошибок, «Проверьте почту» gate 📬 |
| 11 | Practice locked | ✅ совпадает | SpeakingGate (lock-иконка, «Ты почти у цели!», FilledCard surfaceContainerHigh) |
| 12–13 | Профиль / Профиль (гость) | ✅ совпадает | Аватар secondary, stat-карточки OutlinedCard, theme-селектор (SegmentedButton), «Выйти» OutlinedButton error; гость — gate card |
| 14 | Debug Menu | ✅ по desktopTest | ListItem + OutlinedButton действия; вход — 7 тапов по версии |
| 15 | Grading (admin) | ✅ совпадает | MUI: waveform-плеер brand, RubricForm — M3 Slider + avg-панель secondaryContainer v1.3.0; visual-базлайны пересняты |

## Фоны (bg-аудит, pixel-сэмплы light+dark)

Все экраны имеют фирменный фон `#EEF3FF` / `#161A2E`. Флаги аудита — попадание сэмпла на карточки
(surfaceContainerHigh/Lowest) и текст, т.е. by design, а не «дыры». Скрипт: `e2e-cmp/shoot-bg-audit.js`.

## Overflow (аудит 2026-08-08)

- **Admin**: найдено и исправлено двойное смещение AdminLayout (sidebar во flex + `ml:drawerWidth`) —
  docOverflow 115–293px на всех страницах → после фикса **0** на 1280/768/390 (`admin-web/e2e/overflow-audit.cjs`).
- **App**: переполнений нет (mobile 390×844 + desktop 1280, обе темы).

## Открытые правки для дизайн-агента (по решениям владельца)

1. **Q5**: frame-questions в mockups v2.0 привести к текстам приложения («Тренировка · 3 попытки» / «Практика · 30 сек», однородные карточки вопросов).
2. **DSM-5 §1.1 errata**: dark onPrimary/onSecondary `#FFFFFF` → `#1A2F5E` (WCAG).
3. **frame-video**: добавить replay-кнопку «Начать заново» после окончания воспроизведения.

## Гейты (финал M3)

desktopTest 95/95 · vitest 256/256 · lint/typecheck/build · wasm+desktop+APK компиляции ·
Maestro 4/4 · e2e-cmp прод-бандл 51/0/11 · admin Playwright 136/136 + visual 21/21 ·
bg-аудит чистый · overflow-аудит чистый · replay проверен вживую.
