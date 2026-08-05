# Speaking Trainer — Design System Output (Playful Coach, v1.1)

Дата: 2026-07-31 · Источники: `docs/DESIGN_BRIEF_SPEAKING_TRAINER.md`, `docs/prd/SPEAKING-TRAINER-001.prd.md`

> **2026-07-31: владельцем выбран вариант B (Playful Coach)** из `presentation.html`. Токены (`tokens.json`/`tokens.css`) обновлены до палитры B; поведенческие требования мокапов (3 попытки Training, автоотправка Practice, субтитры под плеером) не зависят от варианта и остаются в силе. Мокапы/стайлгайд подхватывают новую палитру через `tokens.css` (перегенерация не требуется, визуальные мелочи вроде радиусов допиливаются при реализации).

## Состав

| Файл | Что это |
|---|---|
| `tokens.json` | Дизайн-токены W3C DTCG: цвета (light+dark), типографика, spacing 4dp, радиусы, elevation, motion. Источник истины |
| `tokens.css` | Те же токены как CSS-переменные + `[data-theme="dark"]` + Reduce motion + `.tnum` (tabular-nums) |
| `styleguide.html` | Интерактивный стайлгайд: свотчи с copy-HEX, тёмная тема, шкалы, компоненты, A11y-таблица контрастов |
| `mockups.html` | 5 эталонных мокапов: Library (360×800), Video — плеер с субтитрами под плеером (play/pause, seek, CC, WebVTT-подобные реплики), Training-запись (флоу 3 попыток: запись → прослушать, ✅ проставляется автоматически с анимацией; после 3-й — «Перейти к практике» / «в библиотеку» / «заново с попытки 1»), Practice (30 сек, одна запись на все вопросы, без review: автостоп → автоматическая отправка upload → sent), Grading detail (1440, рабочие слайдеры рубрики) |
| `presentation.html` | Демо-презентация 3 вариантов (A Calm Studio / B Playful Coach / C Dark Mic), 8 слайдов, навигация ←/→ |
| `icons.svg` | SVG-спрайт: mic, mic-off, play, pause, stop, delete, refresh, cc, lock, upload, check-circle, waveform, clock, chevron-right, shield |

Остальные 9 экранов из брифа §2 в мокапах не генерируются — строятся из тех же токенов при реализации.

## Handoff в код (Фаза 4, So to Speak-8tg.4)

1. Скопировать папку в `.docs/design-system/`.
2. `tokens.json` → `design/src/commonMain/kotlin/com/funnyenglish/design/theme/`:
   - `color.brand` + `color.neutral` → `Color.kt` (Material 3 `ColorScheme`, light+dark из `color.dark`)
   - `color.semantic.record`, `color.timer.*`, `color.status.*` — новые семантические токены, ввести в обоих таргетах
   - `font.scale` → `Type.kt` (`questionText`, `timerDisplay` mono+tnum, `subtitleText`)
   - `radius`, `elevation` → `Shape.kt`, `Elevation.kt`
3. Та же палитра → `admin-web/src/theme/Theme.ts` (`brandColors`/`semanticColors`). HEX обязаны совпадать.
4. `icons.svg` → `design/.../icons/CustomIcons.kt` (ImageVector) и/или `composeApp/src/commonMain/composeResources/drawable/`.
5. Верификация по брифу §5.5: сборка `:design` + `npm run build`, контраст record vs error (WCAG AA), detekt/ktlint.

## Ключевые решения стиля (Variant B — Playful Coach)

- Пастельная игровая палитра — эволюция DS 2.0: фон `#EEF3FF`, primary `#5B8DEF`, фирменный фиолетовый `#9B7EDE`, текст-индиго `#2D3561`. Без стриков и гемов: мотивация — прогресс-кольца и уровни таймера.
- Record — дружелюбный персиковый `#FF9F6B` (не «тревожный» красный); красный `#E53935` закреплён за error — путаницы нет.
- Кнопка записи — **squircle** (radius 22px, НЕ круг) с «оттопыренной» жёсткой тенью `0 4px 0 rgba(217,114,56,.55)`, при нажатии схлопывается.
- Карточки без рамок: радиус 22px, тени 1–2dp с индиго-подтоном.
- Таймер: моноширинные tabular-цифры, кольцо от доли оставшегося времени, уровни 80/50/30 = синий → фиолетовый → персиковый + текстовая подпись уровня.
- Таймер: easing `cubic-bezier(.16,1,.3,1)` + игровой overshoot `cubic-bezier(.34,1.56,.64,1)` для ✅ попыток; фокус — двойное кольцо; Reduce motion обязателен.

### История
- v1.0 (2026-07-30) — Calm Studio (Variant A): тёплая «бумажная» база, record `#E53935`. Заменена на B.
