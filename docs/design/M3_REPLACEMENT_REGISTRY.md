# DSM-1: Реестр замен — So to Speak → Material 3

**Версия:** 1.0 · **Дата:** 2026-08-07 · **Тикет:** bd `FunnyEnglish-dmb` (P2) · **Этап:** DSM-1 по `docs/design/M3_REDESIGN_TASK.md`

**Источники инвентаризации:** `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/` (15 экранов), `composeApp/.../app/components/` (5 файлов), `composeApp/.../designsystem/` (токены, компоненты, анимации, a11y), `admin-web/src/screens/` (12 экранов), `admin-web/src/components/` (9 групп), `.docs/design-system/tokens.json` v1.2.0, `docs/qa/design-conformance/REPORT.md` (DC-1).

**Принцип реестра:** маппинг, а не удаление поведения. Цвета/шрифты/UX-флоу — без изменений (§3 брифа). Кастом остаётся только в brand-элементах (§1 ниже).

---

## 0. Сводка покрытия

| Контур | Единиц | Покрытие |
|---|---|---|
| Экраны приложения (Compose) | 15 | 15/15 — §2 |
| App-компоненты (Compose) | 13 composable в 5 файлах | 13/13 — §3 |
| Модуль `designsystem/` (Compose) | 6 групп | 6/6 — §4 |
| Экраны admin-web (MUI 6) | 12 | 12/12 — §5 |
| Admin-компоненты (MUI 6) | 28 файлов в 9 группах | 28/28 — §6 |

**Архитектурный факт инвентаризации:** speaking-экраны используют `LocalSpeakingColors`/`Speaking*` (Playful Coach v1.2.0) поверх обёртки `FunnyTheme`; legacy-палитра `FunnyColorScheme` (DS 1.x: #2563EB/#10B981, стрики/гемы/XP) speaking-экранами **не импортируется** — используется только `designsystem.animations.speakingPressable`, `accessibility.*`, `layout.WindowSize`. Реестр заменяет компонентную базу на M3, токены Playful Coach не трогает.

---

## 1. Brand-элементы — НЕ заменяются (кастом, оформление состояний по M3 state layers)

| # | Элемент | Файл | Статус в M3 | Что меняется |
|---|---|---|---|---|
| B1 | Rec-кнопка (squircle 22, тень `0 4px 0 rgba(217,114,56,.55)`, 72/56dp) | `app/components/SpeakingRecording.kt` → `SpeakingRecordButton` | Brand-компонент, остаётся | Состояния hover/focus/pressed/disabled — через M3 state layer (overlay 8%/12% от onRecord), форма и тень — без изменений |
| B2 | Таймер-кольцо (уровни 80/50/30, TimerDisplay 64, tnum) | `SpeakingRecording.kt` → `SpeakingTimerRing` | Brand-компонент, остаётся | Трек кольца → `surfaceContainerHigh` (см. DSM-2); переходы уровней — M3 Standard easing |
| B3 | Waveform запись (`recordActive`) / воспроизведение (`waveformPlayback`) | `SpeakingRecording.kt` → `RecordingWaveform`, `PlaybackWaveform` | Brand-компонент, остаётся | Без изменений; цвета из tokens v1.2.0 |
| B4 | ThemeCover (градиент по хешу id + инициалы) | `LibraryScreen.kt` (по DC-1 L1) | Brand-компонент, остаётся | Без изменений |
| B5 | CheckPopAppear (overshoot 0.3→1.18→1.0, 500ms) | `SpeakingRecording.kt` | Brand-моушен, остаётся | Без изменений (§4.4 брифа) |
| B6 | recPulse (1600ms) | `SpeakingRecording.kt` | Brand-моушен, остаётся | Без изменений; `LocalReduceMotion` гасит (уже есть) |
| B7 | REC-индикатор | `SpeakingRecording.kt` → `RecIndicator` | Brand-компонент, остаётся | Точка + подпись; контейнер-подложка — `recordContainer` (уже так) |

---

## 2. Экраны приложения (Compose Multiplatform) — 15/15

| # | Экран | Файл | Текущая база | M3-компоненты (целевые) | Что меняется |
|---|---|---|---|---|---|
| A1 | Splash | `SplashScreen.kt` | Кастом-верстка на SpeakingColors | `Scaffold` + brand-logo; переход — M3 Emphasized | Только motion-токены; компоновка без изменений |
| A2 | Onboarding (3 слайда, «Начать») | `OnboardingScreen.kt` | Кастом-пейджер, индикаторы-точки | `HorizontalPager` + M3 page indicator; CTA «Начать» → **FilledButton** (primaryStrong, radius button 16) | Кнопка на M3; тексты/порядок слайдов — без изменений |
| A3 | Login | `LoginScreen.kt` + `SpeakingAuth.kt` | `SpeakingField`, `SpeakingPrimaryButton`, `SpeakingTextLink` | **OutlinedTextField** (label в бордере, error/focused), **FilledButton**, **TextButton** | Поля и кнопки → M3; гостевой флоу — без изменений |
| A4 | Register | `RegisterScreen.kt` + `SpeakingAuth.kt` | то же | то же + supporting text ошибок M3 | Аналогично A3 |
| A5 | Library («Библиотека тем») | `LibraryScreen.kt` | Кастом-карточки тем, ThemeCover, ThemeStatusChip, ThemeProgressBar (по DC-1 L1–L7) | **ElevatedCard** (тема, radius card 22); статус-бейджи → **AssistChip** (new/reviewed контейнеры §3.1); прогресс → **LinearProgressIndicator** (4dp, анимация width сохраняется); ThemeCover — brand (B4) | Карточки/чипы/прогресс → M3; компоновка сетки — без изменений |
| A6 | Topics | `TopicsScreen.kt` | Кастом-список топиков | **ListItem** (M3, touch ≥48) в `Scaffold`; счётчик вопросов — overline labelSmall | Список → M3 ListItem |
| A7 | Video (плеер + субтитры + чипы режима) | `VideoScreen.kt`, `subtitles/SubtitlePanel.kt` | MockupVideoControls (big-play 64dp, control-bar, CC), segmented-чипы субтитров | Видео-контролы — brand-поверхность (scrimVideoControls), кнопки — M3 **IconButton/FilledIconButton**; чипы режима → **FilterChip** (selected → primaryContainer/onPrimaryContainer); субтитры — subtitleText 17 на scrimSubtitle (без изменений); CTA → **FilledButton** | Чипы и кнопки → M3; плеерная механика — без изменений |
| A8 | Questions (список + rec-зона + кольцо) | `QuestionsScreen.kt` | Нумерованные карточки вопросов, rec-зона | Карточки вопросов → **FilledCard** (questionText 25/600); rec-зона — brand (B1, B2); подписи — labelSmall/bodySmall | Только карточки → M3 Card |
| A9 | Training (3 попытки 80→50→30) | `TrainingScreen.kt` | Кастом на Speaking-токенах; кольцо, точки попыток, плашка хранения | Как A8 + level-чип → **AssistChip** (timer-цвета §3.1); точки попыток — M3 indicator dots; плашка хранения → **FilledCard** surfaceContainerLow; финальные CTA → **FilledButton**/**TextButton** | Контейнеры → M3; поведение 3 попыток/авто-✅ — без изменений (§3.2) |
| A10 | Practice (автостоп 30с, автоотправка, без Review) | `PracticeScreen.kt` | Кастом; жёлтая плашка автоотправки (#FFE0B2/#8A5200), чипы «Контрольная·30 сек», «1 ЗАПИСЬ…» | Как A8 + плашка автоотправки → **FilledCard** (statusNewContainer, текст — тёмный AA, внизу — позиция сохраняется по DC-1 P4); чипы → **AssistChip**; ошибка 409 → **Snackbar** | Контейнеры/чипы → M3; поведение — без изменений (§3.2) |
| A11 | MySubmissions («Отправки», чипы NEW/REVIEWED) | `MySubmissionsScreen.kt` | Кастом-список + статус-чипы | **ListItem** + **FilterChip/AssistChip** статусов (newContainer/reviewedContainer, тёмный текст 9.2/8.7:1 — без изменений); пустое состояние — иллюстрация + bodyMedium | Чипы и список → M3 |
| A12 | Profile (+ гостевой вариант) | `ProfileScreen.kt` | M3-части уже есть (6 импортов material3), кастом-блоки | **ListItem**-секции, **FilledCard** статистики, гостевой блок → **FilledCard** + **FilledButton** «Войти»; выход → **AlertDialog** | Доведение до полного M3-набора |
| A13 | Settings | `SettingsScreen.kt` | M3-части уже есть (6 импортов) | **ListItem** + **Switch** (M3), секции — titleMedium | Выравнивание по M3-паттерну settings |
| A14 | Messages | `MessagesScreen.kt` | Кастом-список сообщений | **ListItem** (avatar + 2 строки), непрочитанное — **Badge** | Список → M3 |
| A15 | Debug Menu | `DebugMenuScreen.kt` | M3-части уже есть (8 импортов) | **ListItem** + **OutlinedButton** для действий | Без поведенческих изменений |

**Навигация приложения (сквозная):** bottom nav «Темы/Отправки/Профиль» (`App.kt`, 4 импорта material3) → **NavigationBar + NavigationBarItem** с M3-индикатором активного пункта (pill primaryContainer, иконка onPrimaryContainer; лейблы — по DC-1 L6, без изменений). Wide/desktop: `WindowSize.kt` → M3 **NavigationRail** (каноническая раскладка, §4.5 брифа).

---

## 3. App-компоненты (Compose) — 13/13

| # | Компонент | Файл | M3-аналог | Что меняется |
|---|---|---|---|---|
| C1 | `SpeakingPrimaryButton` | `SpeakingAuth.kt` | **FilledButton** (`Button`): контейнер primaryStrong, onPrimary #FFF (4.76:1), radius 16, высота 56 (CTA) | Замена на M3; цвет/размер — из tokens |
| C2 | `SpeakingGhostButton` | `SpeakingAuth.kt` | **OutlinedButton**: outline #B9C7EE, текст primary | Замена на M3 |
| C3 | `SpeakingDangerGhostButton` | `SpeakingAuth.kt` | **OutlinedButton** с `error`-цветами (border/текст error) | Замена на M3; error #E53935 — без изменений |
| C4 | `SpeakingTextLink` | `SpeakingAuth.kt` | **TextButton** (primary) | Замена на M3 |
| C5 | `SpeakingField` | `SpeakingAuth.kt` | **OutlinedTextField**: label в бордере, focused 2px primary, error #E53935 + supporting text | Замена на M3-стиль (§4.1 брифа) |
| C6 | `SpeakingGate` (гейт регистрации: Practice, гостевой профиль) | `SpeakingAuth.kt` | Композиция **FilledCard** + Filled/TextButton; на wide — **AlertDialog**-паттерн не применять, остаётся inline-блок | Оформление → M3 Card; поведение гейта — без изменений |
| C7 | `LockedFeature` | `LockedFeature.kt` | **FilledCard** + lock-icon + TextButton | Оформление → M3 Card |
| C8 | `MergeProgressDialog` | `MergeProgressDialog.kt` | **AlertDialog** + **LinearProgressIndicator** | Замена на M3 Dialog (radius sheet 28 → dialog 28 M3) |
| C9 | `LoadingIndicator` | `Common.kt` | **CircularProgressIndicator** | Замена на M3 |
| C10 | `ErrorMessage` | `Common.kt` | **Snackbar** (error) / inline error row (ListItem + error-иконка) — по контексту | Замена на M3-паттерны |
| C11 | `FunnyButton` (app/components дубликат) | `Common.kt` | **Устаревший дубликат** C1 — свести к FilledButton | Кандидат на удаление → вопрос Q3 |
| C12 | `FunnyTextField` (app/components дубликат) | `Common.kt` | **Устаревший дубликат** C5 — свести к OutlinedTextField | Кандидат на удаление → вопрос Q3 |
| C13 | `speakingPressable` (scale 0.97 press) | `designsystem/animations/SpeakingPressable.kt` | M3 ripple + press scale — остаётся как brand micro-motion поверх M3-компонентов | Длительности → M3 Standard (§4.4) |

---

## 4. Модуль `designsystem/` (Compose) — 6/6 групп

| # | Группа / компонент | Файл | M3-аналог | Что меняется |
|---|---|---|---|---|
| D1 | `FunnyButton` (+ варианты) | `components/buttons/FunnyButton.kt` | Filled / FilledTonal / Outlined / Text Button | Legacy DS 1.x; speaking-экранами не используется → вывод из DS → вопрос Q2 |
| D2 | `FunnyCard`, `FunnyFeaturedCard` | `components/cards/FunnyCard.kt` | **ElevatedCard / Card(Filled) / OutlinedCard** | Legacy; маппинг зафиксировать для DS 2.0 |
| D3 | `FunnyTextField`, `FunnyPasswordField`, `FunnySearchField` | `components/inputs/FunnyTextField.kt` | **OutlinedTextField** (+ trailing icon, supporting text) | Legacy; маппинг как у C5 |
| D4 | `FunnySnackbar`(+Host), `FunnyProgress`, `FunnyBadge` | `components/feedback/` | **Snackbar/SnackbarHost**, **Linear/CircularProgressIndicator**, **Badge/BadgedBox** | Legacy; маппинг 1:1 на M3 |
| D5 | Геймификация: `FunnyXPCounter`, `FunnyStreakWidget`, `FunnyQuestCard`, `FunnyLevelProgress`, `FunnyAchievementBadge` (+ `ConfettiAnimation`) | `components/gamification/` | Нет M3-аналога; в DS 2.0 «без стриков/гемов» (tokens.json notes) | **Вне палитры v1.2.0** → кандидат на архивацию → вопрос Q2 |
| D6 | Инфра: `FunnyTheme`, `FunnyColorScheme`, `SpeakingTokens`, `FunnyTypography/Spacing/Shapes`, `WindowSize`, `ReduceMotionProvider`, `AnimationEasings/Durations`, `PageTransitions`, `LoadingSkeleton` | `theme/`, `tokens/`, `layout/`, `accessibility/`, `animations/` | `MaterialTheme` (M3 colorScheme поверх Speaking-ролей — см. DSM-2), M3 MotionScheme (Emphasized/Standard), M3 shapes-шкала | Цвета не трогаем; добавить M3-роли (DSM-2); `PageTransitions` → M3 Emphasized easing; skeleton — M3-паттерн поверх surfaceContainer |

---

## 5. Экраны admin-web (React + MUI 6) — 12/12

| # | Экран | Файл | M3-цель (MUI 6 + theme overrides) | Что меняется |
|---|---|---|---|---|
| W1 | Login | `screens/Login.tsx` | `TextField variant="outlined"` + `Button variant="contained"` (primaryStrong), Card radius 22 | Токены радиусов/цветов через Theme.ts |
| W2 | Dashboard | `screens/Dashboard.tsx` | `Card` (StatsCard) + `LinearProgress`; сетка — MUI Grid | Карточки → Elevated-стиль (тонal elevation через overrides) |
| W3 | Speaking: Libraries (CRUD) | `SpeakingLibraries.tsx` | `Table/DataGrid` + `Dialog` (editor) + `Snackbar` | Диалоги → M3-формы (radius 28) |
| W4 | Speaking: LibraryEditor | `SpeakingLibraryEditor.tsx` | `Dialog`/страница-форма: outlined TextField, `Button` contained/text | Аналогично W3 |
| W5 | Speaking: Topics | `SpeakingTopics.tsx` | Таблица + `Chip` фильтров | Чипы → M3-стиль (radius chip 12) |
| W6 | Speaking: TopicEditor (+ MediaUploader) | `SpeakingTopicEditor.tsx`, `components/MediaUploader.tsx` | Форма + dropzone (кастом-зона остаётся, оформление — outlined container, state layer hover) | Dropzone визуально → M3 outlined |
| W7 | Grading: Inbox | `GradingInbox.tsx` | `Table` + `Chip` статусов NEW/REVIEWED (newContainer/reviewedContainer) + `Badge` счётчика | Статус-чипы → тёмный текст на container (AA уже) |
| W8 | Grading: Detail (плеер + рубрика 1–10 + комментарий) | `GradingDetail.tsx`, `speaking/RubricForm.tsx`, `speaking/SubmissionAudioPlayer.tsx` | Waveform-плеер — **brand (B3)**; рубрика → `Slider` M3-стиля + крупное значение (по DC-1 G2); avg-панель → Filled Card secondaryContainer; действия → Button contained («Сохранить») / text («Пропустить») | Слайдеры/кнопки → M3-оформление; поведение — без изменений |
| W9 | Users | `Users.tsx` + `components/users/*` | `Table` + `Dialog` (PermissionEditor, GroupManager) + `Chip` ролей | Диалоги/чипы → M3-стиль |
| W10 | Analytics | `Analytics.tsx` | `Card`-панели + графики (библиотека графиков — вне DS) | Карточки → M3 Elevated-стиль |
| W11 | ClientLogs | `ClientLogs.tsx` | `Table` + моноширинные метаданные (JetBrains Mono tnum) | Без структурных изменений |
| W12 | Settings | `Settings.tsx` | `List`/`ListItem` + `Switch` | Выравнивание по M3 |

**Навигация admin (сквозная):** MUI `Sidebar` + `Header` + `Breadcrumbs` — осознанный шаблон админки (DC-1 G6 «by design») → **сохраняется**; активный пункт сайдбара — M3-индикатор (pill primaryContainer) через overrides.

---

## 6. Admin-компоненты (MUI 6) — 28/28

| # | Компонент | Файл | M3-аналог (MUI) | Что меняется |
|---|---|---|---|---|
| E1 | AdminLayout | `layout/AdminLayout.tsx` | Scaffold-паттерн (sidebar + content) | Без изменений структуры |
| E2 | Sidebar | `layout/Sidebar.tsx` | M3 NavigationDrawer-стиль (по решению G6 — sidebar) | Активный пункт → pill-индикатор primaryContainer |
| E3 | Header | `layout/Header.tsx` | M3 TopAppBar-стиль | Токены цвета/высоты |
| E4 | Breadcrumbs | `layout/Breadcrumbs.tsx` | MUI `Breadcrumbs` (уже) | Только токены |
| E5 | DataTable | `data/DataTable.tsx` | MUI `Table` + hover state layer 8% | State layers по M3 |
| E6 | StatusBadge | `data/StatusBadge.tsx` | MUI `Chip` (container-цвета §3.1) | Радиус chip 12, тёмный текст |
| E7 | StatsCard | `data/StatsCard.tsx` | MUI `Card` elevated (radius 22) | Токены |
| E8 | SkeletonCard | `data/SkeletonCard.tsx` | MUI `Skeleton` на surfaceContainer | Токены |
| E9 | FormField | `forms/FormField.tsx` | `TextField variant="outlined"` (label в бордере) | M3-стиль полей |
| E10 | SearchInput | `forms/SearchInput.tsx` | `TextField` + `InputAdornment` | M3-стиль |
| E11 | RichTextEditor | `forms/RichTextEditor.tsx` | Кастом (нет M3-аналога) — обрамление outlined container | Только обрамление |
| E12 | FormActions | `forms/FormActions.tsx` | `Button` contained + text | M3-кнопки |
| E13 | ImageUploader | `forms/ImageUploader.tsx` | Dropzone-кастом, outlined container + state layer | Визуал → M3 |
| E14 | MediaUploader | `components/MediaUploader.tsx` | Как E13 | Визуал → M3 |
| E15 | Toast / ToastProvider | `feedback/Toast*.tsx` | MUI `Snackbar` + `Alert` | Маппинг на M3 Snackbar |
| E16 | ConfirmDialog | `feedback/ConfirmDialog.tsx` | MUI `Dialog` (radius 28, M3-заголовок/кнопки) | M3-стиль диалога |
| E17 | ErrorBoundary | `feedback/ErrorBoundary.tsx` | Экран ошибки: Filled Card + Button | Визуал → M3 |
| E18 | ErrorDisplay | `components/ErrorDisplay.tsx` | Inline error (error #E53935) | Токены |
| E19 | Logo | `common/Logo.tsx` | Brand (без изменений) | — |
| E20 | navItems / RouteValidator | `navigation/` | Конфиг/логика — вне DS | — |
| E21 | GradingNavBadge | `speaking/GradingNavBadge.tsx` | MUI `Badge` (счётчик NEW, warning-цвет только графика ≥3:1) | Токены |
| E22 | RubricForm | `speaking/RubricForm.tsx` | MUI `Slider` ×4 (grammar/vocabulary/pronunciation/fluency) + big value | M3 Slider-стиль |
| E23 | SubmissionAudioPlayer | `speaking/SubmissionAudioPlayer.tsx` | **Brand waveform (B3)** + M3 IconButton | Плеер — brand, кнопки → M3 |
| E24 | TopicQuestionsEditor | `speaking/TopicQuestionsEditor.tsx` | `List` + нумерованные item + `Dialog` | M3-стиль |
| E25–E28 | UserTable, UserFilters, UserCard, PermissionEditor, GroupManager | `components/users/` | `Table`, `Chip`-фильтры, `Card`, `Dialog` | M3-стиль по общим правилам |

---

## 7. Открытые вопросы владельцу (по §8.4 брифа — зафиксированы, не додуманы)

- **Q1. Экраны вне списка §6 брифа.** В коде найдены `SplashScreen`, `SettingsScreen`, `MessagesScreen` — в §6 брифа их нет. Включены в реестр (A1, A13, A14) как факт инвентаризации. Подтвердить: они в скоупе M3-редизайна?
- **Q2. Legacy-геймификация DS 1.x** (`FunnyXPCounter`, `FunnyStreakWidget`, `FunnyQuestCard`, `FunnyLevelProgress`, `FunnyAchievementBadge`, `ConfettiAnimation`) не используется speaking-экранами и противоречит notes tokens.json («без стриков/гемов»). Архивируем в реестре как out-of-scope или требуется M3-маппинг «на будущее»?
- **Q3. Дубликаты в `app/components/Common.kt`** (`FunnyButton`, `FunnyTextField`) дублируют C1/C5. Помечены кандидатами на удаление при реализации. Подтвердить.
- **Q4. NavigationRail на wide/desktop** (§4.5 брифа) — добавление rail рядом с bottom nav меняет навигационную компоновку wide-экранов. Это в рамках «адаптивность по M3» или требует отдельного решения?

---

## Ченджлог

- **2026-08-07 · v1.0** — Первая версия реестра: 15 экранов приложения, 13 app-компонентов, 6 групп designsystem, 12 экранов и 28 компонентов admin-web; brand-элементы B1–B7 зафиксированы; вопросы Q1–Q4 владельцу.
