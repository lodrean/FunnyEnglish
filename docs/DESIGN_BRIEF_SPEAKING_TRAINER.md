# Дизайн-бриф: Speaking-тренажёр So to Speak

- **Дата**: 2026-07-30 (обновлено 2026-07-31)
- **Статус**: ✅ Реализован — дизайн-система сгенерирована, выбран **вариант B «Playful Coach»** (владелец, 2026-07-31). Результат: `.docs/design-system/` (tokens.json v1.1, мокапы, icons.svg). Варианты A/C ниже — история выбора.
- **Источники требований**: `docs/prd/SPEAKING-TRAINER-001.prd.md`
- **Назначение документа**: входные данные для генерации макетов через Figma (AI) и последующего переноса дизайн-токенов в код (`:design` модуль Compose и MUI-тему admin-web) через Figma MCP.

---

## 1. Дизайн-бриф для генерации макетов (Figma AI)

### 1.1. Описание продукта

So to Speak — мобильное приложение **тренажёр устной английской речи** (после пивота продукта, см. PRD SPEAKING-TRAINER-001). Ученик проходит флоу:

**Library (темы) → Topics (топики) → Video (с/без субтитров) → Questions → Training | Practice**

- **Training** — безопасная локальная тренировка: запись голоса на каждый вопрос, таймер эскалирует 80с → 50с → 30с, записи хранятся только на устройстве.
- **Practice** — контрольная точка: одна запись 30 секунд на все вопросы, уходит учителю; доступна только авторизованным.
- Учитель в **desktop admin-web** прослушивает записи и оценивает по рубрике (grammar / vocabulary / pronunciation / fluency, 1–10 + комментарий).

### 1.2. Целевая аудитория

| Сегмент | Описание | Что важно в дизайне |
|---|---|---|
| Ученики (основной) | Подростки и взрослые, изучающие английский speaking (уровни A2–B2). Исторически аудитория приложения — дети 7–14 (`docs/DESIGN_SYSTEM_SPEC.md`, §1), но пивот сдвигает фокус на speaking — стоит заложить стиль, комфортный и детям, и взрослым | Крупные touch targets (48dp), понятные состояния записи, снижение тревожности (тренировка без наказания за ошибку), мотивация прогрессом |
| Учителя (вторичный) | Преподаватели, проверяющие записи в admin-web с десктопа | Эффективный inbox: плотные списки, фильтры, быстрое прослушивание и оценка без лишних кликов |

### 1.3. Настроение / направление стиля (2–3 варианта)

**Вариант A — «Calm Studio» (рекомендуемый).**
Спокойная «студия звукозаписи»: приглушённый синий/индиго как база (доверие, концентрация), тёплый акцент (коралловый/оранжевый) только на CTA и кнопке записи. Мягкие радиусы (16–20dp), минимум декора, внимание на waveform и таймер как главных элементах.
*Rationale*: запись голоса — тревожный сценарий; спокойная палитра снижает стресс. Наследует текущий Primary `#4A90D9` (см. `design/src/commonMain/kotlin/com/sotospeak/design/theme/Color.kt`) → меньше пересборки существующего UI.

**Вариант B — «Playful Coach» (эволюция текущей DS 2.0).**
Сохранить пастельную игровую палитру из `docs/DESIGN_SYSTEM_SPEC.md` (Calm Blue `#5B8DEF`, Playful Purple `#9B7EDE`, Energetic Orange `#FF9F6B`) и шрифт Nunito, но убавить геймификацию: прогресс-кольца и уровни таймера (80→50→30) вместо стриков и гемов.
*Rationale*: максимальное переиспользование готового кита в `.docs/Kimi_Agent_So_to_Speak_Design_System_2.0/` и модуля `:design`; риск — выглядит «детски» для взрослых учеников.

**Вариант C — «Dark Mic» (болдинговый).**
Тёмная тема как основная, неоновый акцент на recorder (красная «REC»-кнопка, светящаяся waveform), высокий контраст.
*Rationale*: выразительно и современно, ассоциация с подкаст-студией; риски — хуже читаемость видео-субтитров, больше работы по accessibility, расходится с существующей светлой базой.

**Рекомендация**: генерировать макеты в варианте A; вариант B — fallback для скорости (переиспользование токенов).

### 1.4. Платформы

- **Android-first mobile app**: KMP-монолит `composeApp` (ADR-006), Compose Multiplatform, Material 3. Макеты 360×800dp (baseline), portrait-first.
- **Desktop admin web**: `admin-web` (React + MUI v6, тема в `admin-web/src/theme/Theme.ts`). Макеты 1440px desktop.
- iOS/Desktop/WASM клиент — стабы «недоступно на этой платформе», макеты не нужны (только шаблон экрана-заглушки при желании).

### 1.5. Что переиспользовать из `.docs/`

| Источник | Что взять |
|---|---|
| `.docs/Kimi_Agent_So_to_Speak_Design_System_2.0/` | Готовый Kotlin-кит DS 2.0: `Color.kt`, `Type.kt`, `Shape.kt`, `Elevation.kt`, `Theme.kt`, компоненты (buttons, cards, feedback, inputs) — структура и нейминг уже совпадают с модулем `:design`. Анимации (`animation/PressEffects.kt`, `Loading.kt`) пригодятся для recorder-кнопки |
| `.docs/Kimi_Agent_So_to_Speak_Design_System_2.0/sotospeak-design/components/gamification/` | `ProgressRing.kt` — основа для кругового таймера обратного отсчёта; `Badge.kt` — статусы NEW/REVIEWED |
| `.docs/Kimi_Agent_Admin Panel Design (1)/` | Референсы админки (списки, формы) для Grading inbox |
| `.docs/ADMIN_COMPONENTS_SPEC.md`, `.docs/ADMIN_WEB_DESIGN_SYSTEM_PLAN.md` | Существующие спеки админки — соблюдать консистентность с ними |
| `docs/DESIGN_SYSTEM_SPEC.md` | Токены v1.0 (палитра, типографика, spacing 4dp-grid, elevation) — база, от которой отталкиваемся; принципы accessibility (§1) переносим как есть |

---

## 2. Список экранов для макетов

### 2.1. Mobile (composeApp, Android)

| # | Экран | Ключевые элементы | Состояния |
|---|---|---|---|
| 1 | **LibraryScreen** | Список тем: обложка, название, кол-во топиков. Pull-to-refresh | loading (скелетон карточек), error сети (userFriendlyError + retry), empty |
| 2 | **TopicsScreen** | Список топиков темы: название, длительность видео, статус прохождения (chip/иконка) | loading, error, empty (на случай гостя-офлайна) |
| 3 | **VideoScreen — выбор режима** | Bottom sheet / диалог при входе: «С субтитрами» / «Без субтитров»; вариант скрыт, если субтитров нет у топика | — |
| 4 | **VideoScreen — плеер** | Встроенный плеер: play/pause, seek bar, переключатель субтитров (CC) во время просмотра, субтитры поверх видео (WebVTT, 1–2 строки), кнопка «Перейти к вопросам» (всегда доступна) | buffering, error видео (+ retry, CTA «К вопросам без видео»), landscape/fullscreen (опционально) |
| 5 | **QuestionsScreen** | Список вопросов (текст EN), выбор режима: карточки/кнопки **Training** и **Practice**; Practice для гостя заблокирован (замок + CTA «Войти») | loading, гость vs авторизован |
| 6 | **TrainingScreen** | Текущий вопрос, большая кнопка записи (mic FAB), **waveform** во время записи, круговой/линейный **таймер обратного отсчёта** с уровнем (80с → 50с → 30с, индикатор текущего уровня), автостоп. Список локальных записей по вопросу: play/pause, длительность, дата, delete, перезаписать. Плашка «Записи хранятся только на устройстве» | idle / recording / playing / permission denied (объяснение + ссылка в настройки) / ошибка микрофона / мало места / empty (нет записей) |
| 7 | **PracticeScreen** | Таймер 30с, одна запись на все вопросы, вопросы видны списком во время записи. Состояния: **idle → recording → review (прослушать/перезаписать/отправить) → uploading (progress) → sent (success)**; retry при ошибке сети, запись не теряется | все 5 состояний + permission denied + offline-баннер |
| 8 | **MySubmissionsScreen** | Список отправок: топик, дата, статус (NEW/REVIEWED — badge), после проверки — оценки по рубрике (4 критерия 1–10 + общий балл) и комментарий учителя | empty, loading, error |
| 9 | **Login/Register gate** | Экран/диалог входа-регистрации при попытке запустить Practice гостем; после успеха — возврат в Practice | ошибки авторизации |
| 10 | **Общие состояния** | Empty states (Library, записи, submissions), error screens, permission-rationale для микрофона | — |

### 2.2. Admin-web (desktop, MUI)

| # | Экран | Ключевые элементы |
|---|---|---|
| A1 | **Grading Inbox** | Таблица/список practice-записей: ученик, топик, дата, статус. Фильтры: статус (NEW/REVIEWED), ученик, топик, дата. Empty state «записей нет» |
| A2 | **Grading detail — плеер + рубрика** | Встроенный аудио-плеер, список вопросов топика (read-only), форма рубрики: 4 слайдера/number-input (grammar, vocabulary, pronunciation, fluency, 1–10), авто-усреднённый общий балл, текстовый комментарий, кнопка «Сохранить». Редактирование оценки (REVIEWED) |

(Раздел «Speaking Content» — CRUD Libraries/Topics/Questions — выходит за рамки этого брифа макетов: он переиспользует существующие паттерны админки и MediaUploader, см. `.docs/ADMIN_COMPONENTS_SPEC.md`.)

---

## 3. Дизайн-токены: из Figma в код

### 3.1. Цвета (стартовая палитра, вариант A «Calm Studio»)

Отталкиваемся от существующего `design/src/commonMain/kotlin/com/sotospeak/design/theme/Color.kt` (Primary `#4A90D9` уже совпадает с брендом):

| Токен | Light | Назначение |
|---|---|---|
| primary | `#4A90D9` | навигация, ссылки, play-контролы |
| onPrimary | `#FFFFFF` | |
| primaryContainer | `#D6E6F5` | заливка выбранных элементов, chip статусов |
| secondary | `#006C4C` (teal, из текущей DS) | успешные действия, «sent» |
| tertiary | `#9C27B0` → предлагается заменить на спокойный индиго `#5C6BC0` | второстепенные акценты |
| **record (новый семантический)** | `#E53935` | кнопка записи, REC-индикатор (не путать с error!) |
| **recordActive / waveform** | `#FF5252` → `#4A90D9` | waveform градиент: красный при записи, синий при воспроизведении |
| success | `#43A047` | оценки, статус REVIEWED |
| warning | `#FB8C00` | таймер < 10с, статус NEW |
| error | `#E53935` | ошибки |
| background / surface | `#F8F9FA` / `#FFFFFF` | |
| timer.level (80/50/30) | `#4A90D9` / `#FB8C00` / `#E53935` | цветовая эскалация уровней таймера |

Dark-тема: существующие `*Dark`-токены в `Color.kt` как база; record-красный осветлить до `#FF6B60` для контраста.

### 3.2. Типографика

Текущая шкала — Major Third 1.25, шрифт Nunito (`docs/DESIGN_SYSTEM_SPEC.md` §2.2, реализация `design/.../theme/Type.kt`). Для тренажёра добавить/зафиксировать:
- **questionText**: headlineSmall/medium — вопросы должны читаться с расстояния вытянутой руки;
- **timerDisplay**: tabular figures, моноширинные цифры (например, `FontFamily.Monospace` или Nunito с `fontFeatureSettings = "tnum"`), 48–64sp — таймер не должен «прыгать» по ширине;
- **subtitleText**: 16–18sp, lineHeight 1.4, полупрозрачная подложка под субтитры видео.

### 3.3. Spacing / Shape / Elevation

- **Spacing**: 4dp-сетка (4, 8, 12, 16, 24, 32) — как в текущей DS; расстояние между recorder-кнопкой и списком записей ≥ 24dp.
- **Radii**: карточки 16dp, кнопки 12dp (full-rounded для recorder FAB 56–72dp), bottom sheet 28dp top — сверить с `design/.../theme/Shape.kt`.
- **Elevation**: карточки 1–2dp, плеер-controls поверх видео — scrim-градиент вместо тени; уровни из `design/.../theme/Elevation.kt`.

### 3.4. Маппинг на код

| Figma-токены | Куда ложатся |
|---|---|
| Color styles → | `design/src/commonMain/kotlin/com/sotospeak/design/theme/Color.kt` (Material 3 `ColorScheme`, light+dark) |
| Text styles → | `design/src/commonMain/kotlin/com/sotospeak/design/theme/Type.kt` (`Typography`) |
| Effects/radii → | `Shape.kt`, `Elevation.kt` |
| Новые компоненты (RecorderButton, Waveform, TimerRing, SubmissionCard, RubricBadge) → | `design/src/commonMain/kotlin/com/sotospeak/design/components/recorder/` (новый пакет), ProgressRing переиспользовать из `components/gamification/ProgressRing.kt` |
| Admin (та же палитра!) → | `admin-web/src/theme/Theme.ts` — обновить `brandColors`/`semanticColors`; структура светлой/тёмной темы уже есть |

**Принцип**: одна палитра — два таргета (Compose `ColorScheme` и MUI `palette`). HEX-значения обязаны совпадать; имена семантических токенов (record, timerLevel, statusNew/Reviewed) вводим в обоих местах.

---

## 4. Ассеты для экспорта

| Ассет | Формат | Куда класть |
|---|---|---|
| Иконки: mic, mic-off, play, pause, stop, delete, refresh (перезапись), CC (субтитры), lock (гейт Practice), upload/cloud, check-circle | SVG → вектор (Compose `ImageVector` через CustomIcons, либо XML vector drawable) | Kotlin-иконки: `design/src/commonMain/kotlin/com/sotospeak/design/icons/CustomIcons.kt`; растровые/векторные файлы: `composeApp/src/commonMain/composeResources/drawable/` (создать; зависимость `compose.components.resources` уже подключена в `composeApp/build.gradle.kts:74`) |
| Иллюстрации empty states: пустая библиотека, «нет записей», «нет отправок», ошибка сети, permission denied (микрофон) | SVG (или PNG @1x/2x/3x для сложных) | `composeApp/src/commonMain/composeResources/drawable/` |
| Обложки тем (placeholder) | JPG/WebP | контентные — через admin-web/MinIO; для макетов — `.docs/test_images/` |
| Waveform-заглушка / паттерн | SVG | `composeResources/drawable/` |
| Admin-иконки | MUI icons по возможности; кастомные — SVG | `admin-web/src/assets/` (по существующей структуре) |

Иконки Android-launcher и mipmap (`composeApp/src/androidMain/res/mipmap-*`) в рамках брифа не меняются.

---

## 5. Воркфлоу: Figma → код через Figma MCP

Предусловие: в `.kimi-code/mcp.json` добавлен Figma MCP-сервер (сейчас там только `serena`). Макеты генерируются Figma AI по разделу 1 этого брифа.

1. **Генерация макетов**: промпт из §1 (описание продукта, аудитория, стиль A, платформы, список экранов §2) → Figma AI/плагин. Один файл, две страницы: `Mobile` и `Admin`.
2. **Чтение макетов агентом**: Figma MCP `get_figma_data` по node-id каждого экрана — агент извлекает layout, стили, автолейауты → маппит на Compose-компоненты и MUI-компоненты.
3. **Перенос токенов**: из Figma variables/styles → правка `design/.../theme/Color.kt`, `Type.kt`, `Shape.kt`, `Elevation.kt` и `admin-web/src/theme/Theme.ts` (одна палитра, два таргета — §3.4).
4. **Экспорт ассетов**: `download_figma_images` для иконок/иллюстраций → пути из §4; векторные иконки предпочтительно переносить в `CustomIcons.kt` как `ImageVector`.
5. **Верификация**:
   - Сборка: `./gradlew :design:compileKotlinAndroid` (или `assemble` для затронутых модулей) + `npm run build` в `admin-web`.
   - Visual review: скриншоты экранов из эмулятора/превью Compose (skiko) vs Figma-макеты — сравнение агентом через чтение изображений (папка `docs/screenshots/`).
   - Проверка контраста новых токенов (record vs error!) — WCAG AA, см. §6.
   - detekt/ktlint для изменённых файлов `:design`.

---

## 6. Accessibility требования

1. **Контраст**: текст — WCAG AA (≥4.5:1); субтитры поверх видео — обязательная полупрозрачная подложка (scrim 60–70%) или outline, т.к. фон непредсказуем. Таймер и REC-индикатор — ≥3:1 к фону.
2. **Touch targets**: минимум 48×48dp (унаследовано от DS для детей 7–14, `docs/DESIGN_SYSTEM_SPEC.md` §1); кнопка записи — основная, 64–72dp; кнопки play/delete в списке записей — ≥48dp.
3. **contentDescription** (Compose `semantics`): все recorder-контролы — mic («Начать запись»/«Остановить запись»), play/pause каждой записи («Прослушать запись от <дата>»), delete («Удалить запись»), переключатель субтитров («Субтитры вкл/выкл»), таймер — `liveRegion`/`stateDescription` с озвучкой оставшегося времени (обновление не чаще 1/5с, чтобы не спамить TalkBack).
4. **Статусы не только цветом**: NEW/REVIEWED и уровни таймера 80/50/30 — цвет + текст/иконка (важно: record-красный и error-красный различать формой и подписью).
5. **Reduce motion**: уважать системную настройку — waveform-анимация и пульсация REC упрощаются (см. `design/.../animation/` и принцип «Reduce motion option» из DS spec).
6. **Ошибки и permission**: текстовое объяснение + действие («Открыть настройки»), без color-only индикации; userFriendlyError для сетевых ошибок (конвенция проекта).
7. **Admin-web**: фокус-видимость в форме рубрики, слайдеры критериев управляются с клавиатуры, aria-labels для плеера.

---

## Приложение: ключевые пути репозитория

- PRD: `docs/prd/SPEAKING-TRAINER-001.prd.md`
- Текущая DS-спека: `docs/DESIGN_SYSTEM_SPEC.md`
- Модуль дизайн-системы: `design/src/commonMain/kotlin/com/sotospeak/design/` (theme/, components/, icons/CustomIcons.kt)
- Дублированная DS в монолите: `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/` (учесть при миграции токенов — источник истины `:design`)
- Тема админки: `admin-web/src/theme/Theme.ts`, `admin-web/src/theme/ThemeProvider.tsx`
- Ресурсы клиента: `composeApp/src/commonMain/composeResources/drawable/` (создать), `composeApp/src/androidMain/res/`
- Референсы DS 2.0: `.docs/Kimi_Agent_So_to_Speak_Design_System_2.0/`
- MCP-конфиг: `.kimi-code/mcp.json`
