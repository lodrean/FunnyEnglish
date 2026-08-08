# ЗАДАЧА: M3-редизайн дизайн-проекта So to Speak (Material 3 + палитра Playful Coach)

> **Версия постановки:** 1.0 · Дата: 2026-08-07 · Тикет: bd `FunnyEnglish-dmb` (P2)
> **Исполнитель:** Kimi-агент с дизайн-плагинами (Figma MCP / HTML-мокапы — как итерации DS 1.x–2.x)
> **Заказчик/ревьюер:** владелец продукта. Любые решения вне рамок §3 — только через него.

---

## 1. Роль и контекст

Ты — дизайн-агент. Продукт **So to Speak** — кроссплатформенный speaking-тренажёр английского:
ученик смотрит видео по теме (с/без субтитров), отвечает голосом на вопросы в режимах
Training/Practice, учитель оценивает записи в admin-web по рубрике.

Платформы, на которые ляжет дизайн:
- **Compose Multiplatform** (android / desktop / wasm) — реализация на Compose Material3;
- **admin-web** — React + MUI 6.

У продукта УЖЕ есть действующая дизайн-система «Playful Coach» v1.2.0 (вариант B) —
она реализована в коде и подтверждена E2E-конформити. Задача — НЕ придумать новый
дизайн, а **пересобрать существующий дизайн-проект на компонентной базе Material 3**,
сохранив фирменную палитру и поведение экранов.

Текущая дизайн-система лежит в репо: `.docs/design-system/`
(`tokens.json` v1.2.0, `tokens.css`, `mockups.html`, `styleguide.html`, `icons.svg`).

## 2. Цель

Заменить текущий дизайн-проект (`mockups.html`, `styleguide.html`, `tokens.*`) новой
версией на базе **Material 3**: компоненты, паттерны, state layers, motion, адаптивность,
доступность — по рекомендациям M3; цвета, шрифты, UX-флоу и поведение — без изменений.

**Это НЕ:** смена бренда, смена палитры, смена UX-флоу, реализация в коде.

## 3. Жёсткие ограничения (нарушать нельзя)

### 3.1. Цветовая гамма — HEX 1:1 из tokens.json v1.2.0

**Brand (light):**

| Роль | HEX | Назначение |
|---|---|---|
| primary | `#5B8DEF` | Навигация, ссылки, play-контролы |
| primaryStrong | `#3B6FD4` | Primary для БЕЛОГО текста (4.76:1 AA): кнопки, активные чипы, bottomnav, CTA. Белый на primary = 3.23:1 FAIL — запрещён |
| onPrimary | `#FFFFFF` | |
| primaryContainer / onPrimaryContainer | `#DDE8FD` / `#1A2F5E` | Заливка выбранных элементов |
| secondary | `#9B7EDE` | Фирменный фиолетовый: акценты, таймер 50, чипы |
| onSecondary | `#FFFFFF` | |
| secondaryContainer / onSecondaryContainer | `#E5DCFF` / `#5B3FA8` | Чипы, подложки заметок |
| tertiary / onTertiary | `#006C4C` / `#FFFFFF` | Успешные действия, статус sent |

**Semantic (light):**

| Роль | HEX | Назначение |
|---|---|---|
| record | `#FF9F6B` | Кнопка записи, REC-индикатор. Дружелюбный персиковый, НЕ тревожный красный |
| recordActive | `#D97238` | Waveform при записи (затемнён ≥3:1) |
| recordShadow | `#D97238` | «Оттопыренная» тень rec-кнопки: `0 4px 0 rgba(217,114,56,.55)` |
| recordContainer / onRecordContainer | `#FFE3D1` / `#8A3B0E` | Подложка record-элементов / текст на ней (AA) |
| onRecord | `#2D3561` | Текст/иконка на record (5.81:1 AA; белый = 2.01 FAIL) |
| waveformPlayback | `#5B8DEF` | Waveform при воспроизведении |
| success | `#43A047` | |
| warning | `#FB8C00` | Статус NEW (только large text / графика — 2.37:1 для белого FAIL) |
| error / onError | `#E53935` / `#FFFFFF` | Ошибки. Красный свободен: record ≠ error |

**Timer levels:** level80 `#4A7FE8` · level50 `#8A68D6` · level30 `#D97238` (≥3:1 для графики)

**Submission status:** new `#FB8C00` / newContainer `#FFE0B2` · reviewed `#43A047` / reviewedContainer `#C8E6C9` (текст на container — тёмный, AA проверен)

**Neutral (light):** background `#EEF3FF` · surface `#FFFFFF` · surfaceWarm `#E5DCFF` · surfaceCard `#FFFFFF` · surfaceVariant `#D8E2FA` · outline `#B9C7EE` · text `#2D3561` · textMuted `#58609A` (5.32:1 — допустим для мелкого текста) · scrimSubtitle `#000000B3` · scrimVideoControls `#00000080`

**Dark:** background `#161A2E` · surface `#1F2440` · surfaceVariant `#2B3152` · text `#E8EAF6` · textMuted `#9AA0C4` · primary `#8FB3F5` · secondary `#B79EED` · record `#FFB27D` · outline `#3D4568`

**Прочие токены (сохранить):** шрифты Nunito (brand) + JetBrains Mono tnum (таймер/длительности);
type scale: labelSmall 12 / bodySmall 14 / bodyMedium 16 / titleMedium 20 / questionText 25 / headlineSmall 31 / timerDisplay 64 / subtitleText 17; веса 400/600/700/800;
spacing 4/8/12/16/24/32; radius: button 16, card 22, cardLarge 26, sheet 28, chip 12, recorder 22 (squircle), full 999;
touchTarget 48, recorderButton 72, recorderButtonSmall 56;
тени: card `0 1px 2px rgba(45,53,97,.06), 0 2px 8px rgba(45,53,97,.05)`, fab `0 4px 0 rgba(217,114,56,.55)`.

**WCAG AA-пары, которые нельзя деградировать (аудит 2026-08-01):**
- белый текст — ТОЛЬКО на primaryStrong (4.76:1);
- onRecord `#2D3561` на record `#FF9F6B` — 5.81:1;
- textMuted `#58609A` на background — 5.32:1 (мелкий текст ок);
- recordActive / timer levels — ≥3:1 (графика);
- чипы статусов — тёмный текст на container (9.2/8.7:1), белый на warning — FAIL.

### 3.2. Поведенческие требования мокапов (авторитетны, дословно)

- **Training** = ровно 3 попытки на топик (лимиты 80→50→30 сек), без удаления записей —
  только прослушивание, авто-✅ после записи, финальные CTA.
- **Practice** = без Review-экрана; автостоп на 30с ИЛИ ручной стоп → автоотправка;
  повторная отправка по топику запрещена (backend 409).
- **Guest-first:** Splash → (первый запуск → Onboarding 3 слайда, «Начать») → Library;
  регистрация/логин — гейт при входе в Practice и в гостевом профиле.
- Запись голоса — центральная механика; **rec-кнопка — фирменный brand-компонент**
  (squircle radius 22, «оттопыренная» тень `0 4px 0 rgba(217,114,56,.55)`) — остаётся,
  но состояния hover/pressed/disabled оформить по M3 state layers.
- **Таймер-кольцо** (SpeakingTimerRing) с уровнями цвета 80/50/30 — остаётся.

### 3.3. Платформенный маппинг

Мокапы должны маппиться на **Compose Material3** (`androidx.compose.material3.*`) и
**MUI 6** (компонент + theme overrides) БЕЗ кастомной магии. Кастом допустим только
для brand-элементов: rec-кнопка, таймер-кольцо, waveform, ThemeCover-градиенты
(градиент по хешу id + инициалы темы).

## 4. Что берём из Material 3

1. **Компонентная база** — заменить кастомные компоненты M3-аналогами (маппинг, не удаление поведения):
   - Кнопки: Filled / FilledTonal / Outlined / Text (вместо SpeakingPrimaryButton/Ghost/DangerGhost).
   - TextFields: Outlined (M3-стиль: label в бордере, состояния error/focused).
   - Карточки: Elevated / Filled / Outlined — по семантике (тема библиотеки, отправка, статистика).
   - Navigation: NavigationBar (bottom nav «Темы/Отправки/Профиль») с M3-индикатором активного пункта.
   - Chips (assist/filter), Dialogs, Snackbar, BottomSheet, List items, LinearProgress/CircularProgress — где уместно.
2. **M3 type scale** — маппинг текущих стилей на роли display/headline/title/body/label
   (размеры Nunito из §3.1 сохранить, роли — по M3).
3. **State layers и elevation** — hover/focus/pressed/dragged (8%/12%…), tonal elevation
   вместо кастомных теней (КРОМЕ фирменной rec-кнопки).
4. **M3 motion** — маппинг на Emphasized/Standard easing и длительности M3;
   recPulse (1600ms) и CheckPopAppear (overshoot scale 0.3→1.18→1.0, 500ms) остаются brand-моушеном.
5. **Адаптивность** — window size classes; канонические раскладки для desktop/wide.
6. **Accessibility** — touch-таргеты ≥48dp, видимый фокус, контрасты AA (см. §3.1).

## 5. Этапы работы (выполнять последовательно, отчёт после каждого)

| Этап | Что сделать | Выход (файл) | Done-критерий |
|---|---|---|---|
| **DSM-1. Реестр замен** | Инвентаризация ВСЕХ экранов (§6) и кастомных компонентов (composeApp `designsystem/`, `app/components/Speaking*.kt`; admin-web `src/components/`) → таблица «текущий компонент/экран → M3-аналог → что меняется». Образец формата: `docs/qa/design-conformance/REPORT.md` | `docs/design/M3_REPLACEMENT_REGISTRY.md` | Покрытие 100% экранов приложения и админки; каждая строка — конкретный M3-компонент |
| **DSM-2. Tokens v1.3.0** | `tokens.json`/`tokens.css`: цвета НЕ трогать; добавить недостающие M3-роли (state-layer alpha, `surfaceContainer*`, `outlineVariant` и пр. по необходимости реестра) | `.docs/design-system/tokens.json` + `tokens.css`, bump `version: 1.3.0` + ченджлог в `$metadata.notes` | Цвета из §3.1 — HEX 1:1 (diff только добавления); WCAG-пары не деградировали |
| **DSM-3. Styleguide v2.0** | Страница M3-компонентов в фирменной теме: кнопки 4 вида, Outlined TextFields, карточки 3 видов, chips, dialogs, snackbar, NavigationBar, progress + brand-блок (rec-кнопка со state layers, таймер-кольцо, waveform) | `.docs/design-system/styleguide.html` (v2.0 в шапке + ченджлог) | Все компоненты §4.1 представлены в light и dark; brand-блок визуально = текущему |
| **DSM-4. Mockups v2.0** | Все экраны приложения на M3-компонентах, light+dark. Поведенческие аннотации §3.2 — ДОСЛОВНО | `.docs/design-system/mockups.html` (v2.0 в шапке + ченджлог) | Каждый экран §6 (приложение) в 2 темах; аннотации сохранены; соответствие реестру DSM-1 |
| **DSM-5. Маппинг реализации** | Для каждого компонента реестра: Compose Material3-компонент + MUI 6 компонент/overrides (`Theme.ts`) | `docs/design/M3_IMPLEMENTATION_MAPPING.md` | Реализация по документу — механическая, без дизайн-решений в коде |
| **DSM-6. Дифф спеки DS** | Подготовить дифф `docs/DESIGN_SYSTEM_SPEC.md` (v2.1 → v3.0) под M3-базу. **СТОП: спеку НЕ править.** Дифф — отдельным документом на утверждение владельца (ADR-007) | `docs/plan/M3_DESIGN_SYSTEM_SPEC_DIFF.md` | Дифф содержит: что/почему/альтернативы/влияние на смежные спеки |
| **DSM-7. Ревью владельца** | Презентация: mockups v2 + styleguide v2 + реестр + маппинг. Собрать решения, оформить правки | Отчёт ревью (в ответ заказчику) | Все замечания владельца обработаны или зафиксированы follow-up'ами |

## 6. Полный список экранов

**Приложение (Compose):** Onboarding (3 слайда), Login, Register, Library («Библиотека тем»),
Topics, Video (плеер + субтитры, чипы режима), Questions/Training/Practice (список вопросов +
rec-зона + таймер-кольцо), MySubmissions («Отправки», чипы статусов NEW/REVIEWED),
Profile (+ гостевой вариант), Debug Menu.

**Admin-web (MUI 6):** Login, Dashboard, Speaking (Libraries/Topics/Questions CRUD +
MediaUploader), Grading (Inbox + Detail: плеер + рубрика grammar/vocabulary/pronunciation/fluency
1–10 + комментарий), Users, Analytics, ClientLogs, Settings.

## 7. Критерии приёмки (финальные)

- [ ] Все экраны/компоненты — на M3-базе; кастом только там, где это осознанный brand-элемент
      (rec-кнопка, таймер-кольцо, waveform, ThemeCover-градиенты).
- [ ] Цвета — HEX 1:1 из §3.1; WCAG AA-пары не деградировали (проверить контрасты новых сочетаний).
- [ ] Тёмная тема проработана для всех экранов.
- [ ] Поведенческие аннотации мокапов сохранены дословно (§3.2).
- [ ] Реестр замен покрывает 100% экранов (приложение + админка).
- [ ] Все артефакты версионированы (v1.3.0 / v2.0) с ченджлогом.

## 8. Стоп-правила и границы

1. **ADR-007:** спеки и PRD (`docs/DESIGN_SYSTEM_SPEC.md`, `docs/SPEAKING_TRAINER_SPEC_PART*.md`,
   `docs/prd/*`) НЕ править. Обнаружив расхождение дизайна со спекой — остановиться,
   оформить вопрос/дифф владельцу (что/почему/альтернативы/влияние) и ждать решения.
2. Цвета, шрифты, название бренда, UX-флоу и поведение экранов — НЕ менять.
3. Код приложения и админки — НЕ трогать (реализация — отдельный план после утверждения дизайна).
4. Если данных не хватает (неясное поведение, непокрытый экран) — зафиксировать вопрос
   владельцу списком, не додумывать.

## 9. Референсы в репо

| Что | Путь |
|---|---|
| Текущая DS (источник истины по цветам) | `.docs/design-system/tokens.json` v1.2.0, `tokens.css` |
| Текущие мокапы/стайлгайд (поведение и компоновка) | `.docs/design-system/mockups.html`, `styleguide.html` |
| Иконки | `.docs/design-system/icons.svg` |
| Спека DS (v2.1, читать, НЕ править) | `docs/DESIGN_SYSTEM_SPEC.md` |
| Конформити-отчёт + скриншоты реального приложения | `docs/qa/design-conformance/REPORT.md` |
| Реализация темы (для понимания маппинга) | `composeApp/.../designsystem/theme/SpeakingTokens.kt`, `design/`, `admin-web/src/theme/Theme.ts` |
| Бриф этой задачи | `docs/DESIGN_BRIEF_M3_REDESIGN.md` |

## 10. Формат сдачи

- Работа по этапам DSM-1 → DSM-7; после каждого этапа — короткий отчёт: что сделано,
  путь к артефакту, отклонения/вопросы.
- Версионирование: bump версии в шапке файла + строка ченджлога (дата → что изменилось).
- Финал: сводный отчёт по чек-листу §7 + список открытых вопросов владельцу.
