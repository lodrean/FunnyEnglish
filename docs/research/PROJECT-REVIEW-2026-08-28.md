# Исследование проекта So to Speak / FunnyEnglish — улучшения (дизайн, архитектура, функционал)

- **Дата:** 2026-08-28
- **Тип:** исследование кодовой базы (research) — файлы не менялись
- **Метод:** прочитаны `memory.md` (101 грабля), все спеки/PRD/планы (`docs/prd/SPEAKING-TRAINER-001.prd.md`, `docs/SPEAKING_TRAINER_SPEC_PART{1,2,3}.md`, `docs/plan/SPEAKING-TRAINER-001.md`), дизайн-токены (`.docs/design-system/tokens.json` v1.3.1, `mockups.html` v2.1), отчёты конформити (`docs/qa/design-conformance/*`); проведены 4 параллельных глубоких ревью (backend, KMP-клиент, admin-web, дизайн-система); каждая ключевая находка перепроверена grep'ом/чтением кода.

---

## 1. Общая картина

Продукт — speaking-тренажёр (видео + голосовые ответы): ученик смотрит видео с субтитрами, отвечает голосом (Training — локально, 3 попытки 80/50/30с; Practice — 30с, автоотправка учителю), учитель оценивает по рубрике в admin-web. Пивот выполнен за ~3 недели с высоким качеством инженерной культуры: дисциплинированный MVI, спеки-источник-истины (SDD + ADR-007), 100+ задокументированных граблей в `memory.md`, многоуровневое тестирование (desktopTest ~120, Dropshots golden 12 экранов, e2e-cmp WASM, Maestro, admin Playwright, Newman).

Три системных слоя напряжения:

1. **Невычищенный legacy-пласт** в каждом модуле: backend (~70 legacy-файлов, часть сломана на runtime), shared (legacy-модели, замороженные backend'ом), admin-web (~7–8 тыс. строк мёртвого кода), composeApp (зомби-тема, мёртвые роуты).
2. **«Целевая» модульная архитектура (ADR-006), которая компилируется, но не подключена**: `core/*`, `feature-*`, `:design` — рантайм приложения их не использует; создаётся иллюзия модульности без её выгод.
3. **Дрейфы между дизайн-артефактами и кодом**: 6 копий токенов, из которых актуальны две; незакрытая errata dark-ролей (WCAG FAIL); устаревший `tokens.css`.

Плюс точечные, но реальные дыры безопасности (demo-аккаунт в миграции V1, публичная утечка черновиков, OAuth без проверки у провайдера) и фейковые данные в админке (Analytics/Settings/Dashboard/Users), на основе которых владелец может принимать продуктовые решения.

---

## 2. АРХИТЕКТУРА

### 2.1. Backend (Spring Boot 3.4.1, Kotlin 2.1, PostgreSQL, Flyway, JWT)

#### Сильные стороны

- **Speaking-модуль образцовый**: тонкие контроллеры (`SpeakingPublicController`, `SpeakingSubmissionController`, `SpeakingAdminController`), бизнес-логика в `SpeakingContentService`/`PracticeSubmissionService` с явными `@Transactional(readOnly = true)`, join-fetch запросы без N+1, идемпотентный soft-delete топиков, backend-гейт дублирующих отправок `DUPLICATE_SUBMISSION` (409), generated column `grades.total` с `entityManager.refresh`.
- **Безопасность дефолтов**: fail-fast при старте для пустого/короткого `JWT_SECRET` и пустого `ADMIN_PASSWORD`, stacktrace скрыт из 500-ответов, demo-юзер только при `DEMO_USER_ENABLED=true` (в initializer), BCrypt, rate-limit с token-bucket и `Retry-After`, CORS-allowlist через env, email-верификация с anti-enumeration resend и одноразовыми токенами (TTL 24ч).
- **API-консистентность внутри speaking**: единый `ErrorResponse {error, message, details}`, машиночитаемые коды (`EMAIL_NOT_VERIFIED`, `DUPLICATE_SUBMISSION`, `TOKEN_EXPIRED`), `jakarta.validation` на request-DTO, пагинация Page для admin-inbox с `size.coerceIn(1,100)`.
- **Flyway-дисциплина после инцидента V19–V21**: миграции за Hibernate-схемой пишутся с `IF NOT EXISTS`; `ddl-auto: validate` в prod; индексы под аналитические запросы (V14, V16–V18).
- **Полезные мелочи**: `RequestLoggingFilter` без тел запросов, `MediaUrlService` нормализует MinIO-URL (BUG-004), whitelist расширений/типов в `StorageService`, `jackson-module-kotlin` зафиксирован (грабля №18).
- **Тесты флоу**: `SpeakingFlowIntegrationTest` (11 сценариев) + `TokenRefreshIntegrationTest`, `EmailVerificationIntegrationTest`, unit-тесты rate-limit.

#### Критично (безопасность)

| # | Проблема | Где |
|---|---|---|
| Б1 | **Demo-аккаунт с известным паролем вставляется миграцией V1 на каждой чистой БД, включая prod** (`demo@sotospeak.app/demo123`); `AdminUserInitializer` при `DEMO_USER_ENABLED=false` его не удаляет, ни одна миграция не удаляет | `backend/src/main/resources/db/migration/V1__initial_schema.sql:142-148`, `AdminUserInitializer.kt:68-72` |
| Б2 | **Публичная утечка черновиков тестов**: `GET /tests/** permitAll()`, а `TestService.getTestById` не фильтрует `isPublished`; в паре с `@Cacheable("testDetails")` без инвалидации при публикации — черновик доступен и из кэша до 15 мин | `SecurityConfig.kt:37`, `TestService.kt:81-129` |
| Б3 | **OAuth-логин без проверки у провайдера**: клиентский `token` принимается как `providerId`, аккаунт создаётся/связывается без обращения к Google/VK/Telegram → при включении OAuth возможна подделка входа (account-takeover). Плюс `user.copy()` на entity | `AuthService.kt:72-118` |
| Б4 | **Rate-limit обходится через `X-Forwarded-For`**: доверие первому значению заголовка без whitelist доверенных прокси; при прямом доступе к приложению IP спуфится тривиально | `RateLimitingFilter.extractClientIp` (RateLimitingFilter.kt:168-184) |

#### Важно

- **OSIV включён** (`spring.jpa.open-in-view` не отключён) — сессия/коннекшн держатся весь HTTP-запрос, маскирует отсутствие `@Transactional` и ленивую загрузку, провоцирует N+1 и исчерпание пула. Отключить и пройтись по read-методам.
- **`data class`-entity + `copy()`** по всему legacy (`User`, `Question`, `Test`, `Progress`, `GamificationEntities`) — каждый save = merge с лишним SELECT, сломанный dirty-checking; equals/hashCode по всем полям, включая lazy-коллекции → риск `LazyInitializationException` и нарушения инвариантов HashSet.
- **N+1 и чтение всего в память**: `AdminUserController.getUsers` — `findAllByOrderByCreatedAtDesc()` + фильтрация в памяти + `getUserStats` (3 count-запроса) на пользователя, без пагинации; `AdminService.getAnalytics` — 8 отдельных COUNT; `TestService.getAllTestsForAdmin` — `findAll()` + lazy questions.
- **Две URL-конвенции**: новые контроллеры маппятся без `/api` (context-path добавляет сам), legacy — с `/api/v1/...` → фактически доступны по `/api/api/...`; `SecurityConfig` матчит единственный `/api/audio-tests/**` — запутанные контракты и документация.
- **Legacy-контроллеры сломаны на runtime**: `GamificationController`/`AdaptiveLessonController` принимают `@AuthenticationPrincipal userDetails: UserDetails`, а `JwtAuthenticationFilter` кладёт `UserPrincipal` (НЕ реализует `UserDetails`) → любой вызов 500. Мёртвый код без тестов.
- **Нет уникального constraint `(user_id, topic_id)`** на `practice_submissions` (V18 создаёт только индексы) — race двух параллельных POST обходит 409-гейт и создаёт дубли.
- **Refresh-токен = тот же access-токен** (`AuthService.refreshToken`): нет ротации/revocation/blacklist, logout чисто клиентский; украденный токен живёт до 7 дней окна.
- **HTTP-семантика ошибок**: анонимный доступ → 403 вместо 401, неверные креды → 400 вместо 401 — клиентам нужны обходные пути (зафиксировано в memory как расхождение с Newman).
- **Валидация загрузок**: глобальный multipart-лимит 200MB, практика capped 5MB в коде, но файл буферизуется Spring **до** проверки; `/speaking/submissions` не rate-limit'ится; тип файла проверяется только по расширению+content-type (без magic-bytes/перекодирования) — можно залить произвольное содержимое под видом .png/.m4a.
- **Нет версионирования API** (кроме случайного legacy `/api/v1`), нет OpenAPI/Swagger, нет пагинации на публичных списках и на `/admin/users`.
- **`StreakService` без `@Transactional`** — `recordActivity`/`recoverStreak` делают несколько `save()` в отдельных транзакциях; `getRecentActivities` — заглушка (пустой список), `xpEarned = 0` — endpoint'ы отдают недоделанные данные.

#### Низко

- Нет request-id/трейсинга (ни Sleuth/OTel, ни MDC) — сложно связывать логи по запросам.
- Инверсия зависимостей в legacy: `AchievementService.getAchievementStats` возвращает `com.sotospeak.controller.AchievementStats`; DTO живут прямо в контроллерах.
- JSONB-недоделка: `Question.content` (jsonb) закомментирован в entity, скоринг идёт по legacy Answer-строкам, `deleteTest` требует workaround «delete questions first».
- Rate-limit в памяти одного инстанса (не масштабируется на несколько подов).
- `MediaUrlService.normalize` — строковая замена endpoint→public-url, хрупкая к форматам URL.

#### Предложения (по приоритету)

1. **Выпилить legacy-слой** (или изолировать за `@Profile("legacy")`, off по умолчанию): Gamification/AdaptiveLesson/AudioTest/FeatureToggle/Question/Test/Groups/Message + удалить `implementation(project(":shared"))` из `backend/build.gradle.kts:19`. Зачем: убрать сломанные endpoint'ы, снизить поверхность атаки, разорвать coupling с shared-моделями (memory №51), ускорить сборку/тесты. Отдельное решение владельца (memory: legacy до после MVP), но пакеты уже можно пометить deprecated.
2. **Отключить OSIV** (`spring.jpa.open-in-view: false`) и добавить `@Transactional(readOnly = true)` + join-fetch/проекции на все read-методы. Зачем: вернуть коннекшны в пул и выявить реальные N+1 на ранних этапах.
3. **Testcontainers-Postgres** для интеграционных тестов (сейчас H2 + create-drop, а грабли №31/81 всплывают только на живом Postgres); добавить тесты AdminController/аналитика, security-контракты (401/403/TOKEN_EXPIRED/rate-limit e2e).
4. **Миграция с удалением demo-юзера** из V1 (`DELETE FROM users WHERE email = 'demo@sotospeak.app'`), создание — только в `AdminUserInitializer` под `DEMO_USER_ENABLED`.
5. **`UNIQUE (user_id, topic_id)`** на `practice_submissions` новой миграцией + обработка `DataIntegrityViolationException` → 409 как fallback.
6. **Аутентификация**: отдельный refresh-токен (JTI + ротация + revocation/logout), сверка роли с БД в фильтре (кэш userId→role на 1–5 мин), 401 для анонимных и неверных кредов.
7. **springdoc-openapi** + пагинация для `/admin/users` и `/public/speaking/*` (limit/offset или cursor по displayOrder) — самодокументируемые контракты вместо ручного docs/API.md.
8. **Валидация загрузок**: magic-bytes/перекодирование, лимиты по типу, стриминг в S3 без полной буферизации, rate-limit на upload.
9. **Убрать `data class` с entity** (обычные классы + equals/hashCode по id, как уже сделано в `Topic`/`PracticeSubmission`) и заменить `entity.copy(...)` на мутацию managed-инстанса.

### 2.2. KMP-клиент (composeApp + shared)

#### Сильные стороны

- **Дисциплинированный MVI**: единый паттерн State/Action/Event на всех экранах, гонки закрываются guards (`uploadInFlight`, `inFlightUploads`, `refreshMutex`), фазовые машины Training/Practice выдержаны.
- **Аккуратный expect/actual**: узкие контракты (`VideoPlayerController`, `VoiceRecorder`, `MicPermission`, `PlatformBackHandler`, `VideoFullscreenEffect`, `PlatformReduceMotion`, `SystemBarStyle`); Android-реализации качественные (аудиофокус с автостопом при звонке, проверка свободного места, compose-first Media3 с кастомными слотами контролов).
- **Продуманный сетевой слой** (`shared/api/SoToSpeakApi.kt`): `expectSuccess=true`, типизированные `ApiException(code, errorCode)`, single-flight refresh по 401 с одним retry и корректным поведением при сетевой ошибке vs отклонённом refresh; токен — в `defaultRequest` на каждый запрос; `getTextResource` снимает Authorization (JWT не утекает на медиа-хост); `sendLogs` намеренно без safeCall (анти-рекурсия логов).
- **Многослойная защита продукта**: backend-гейт 409 поверх клиентского `hasSubmitted`, минимальная длительность записи 5с, offline-retry неотправленных записей, ошибка субтитров не блокирует видео.
- **Высокий тестовый охват**: UI-тесты на реальных экранах (не заглушках), golden-скриншоты (Dropshots), E2E двух уровней (WASM-playwright + Maestro), MockEngine-тесты API.
- **Единый HTTP-стек**: KtorDataSource в ExoPlayer (bd 4d1) — один движок и версия (Ktor 3.0.3) на всём клиенте.

#### Критично

- **К1. Три из четырёх платформ — стабы; iOS фактически не существует как продукт**: `iosMain` — нет ни `MainViewController`, ни iosApp-модуля, ни Xcode-проекта (glob: 0 совпадений); `VideoPlayerController.ios/desktop` и `VoiceRecorder.ios/desktop` — стабы «недоступно на этой платформе»; wasmJs — видео работает (DOM `<video>` поверх canvas), рекордер — стаб. **Запись голоса и видео работают только на Android.** Декларация ios-таргетов (framework собирается, компиляция зелёная) создаёт ложное впечатление кросс-платформенности.
- **К2. Навигация без back-стека и без сохранения состояния**: `App.kt:127` — `remember { mutableStateOf(AppScreen.Splash) }`; при process death приложение всегда стартует заново; deep links нет (для веб-версии, шеринга топика, push «учитель оценил»); на WASM history-guard делает «Назад» браузера нерабочим; параметры маршрута (`libraryId`, `libraryTitle`, `topicId`) продублированы в 5 data-классах `AppScreen` — типобезопасность маршрутов ручная.
- **К3. ViewModel-скоуп = Activity, а не экран**: `koinViewModel()` резолвится в ViewModelStore Activity — все 12 VM переживают переходы между экранами; следствие — компенсаторный паттерн «ручной сброс в load()» разбросан по всем VM (дал залипший экран логина, грабля №36); таймеры/джобы живут между визитами.
- **К4. `SoToSpeakApi` — монолит на 321 строку без интерфейса**, VMs общаются с сетью напрямую; нет repository/domain-слоя в рабочем коде; глобальный mutable-мост `Logger.remoteQueue/remoteMeta/onRemoteEnqueued` настраивается из `App()`.

#### Важно

- **Вся «целевая» архитектура (ADR-006) — мёртвый код**: composeApp не импортирует ни одного символа из `com.sotospeak.core.*` (grep: 0) и не зависит от `core`-модулей; `feature-tests`, `feature-leaderboard`, `feature-learning`, `feature-gamification` — вообще без исходников (в settings.gradle.kts подключены); `feature-{home,auth,profile}` компилируются «в стол»; `design/` (40 файлов) реально используется только как `SpeakingIcons` (9 импортов) при параллельной рабочей системе `designsystem/`.
- **Три способа выразить ошибку**: `kotlin.Result` (реальный), `core/domain/util/Result` (feature-модули), `core/domain/Result` + `DataError` (устаревшие дубли, грабля №6) — легко импортировать не тот.
- **`RecordingStore` — JSON в Settings с полным переписыванием списка** на каждую операцию; `LibraryViewModel.loadProgress` вызывает `list()` на каждый топик → O(библиотеки × топики × размер JSON) парсингов; файлы Training-записей никуда не чистятся.
- **Legacy shared-модели заморожены backend'ом** (Achievement, AdaptiveLesson, AudioTest, Quest, Streak, Test, LessonModels): клиент их не использует, но удаление ломает `:backend:compileKotlin` (~15 файлов); контракт «модель клиента = DTO backend'а» не версионирован (инцидент `newBestScore`/`isNewBestScore`, грабля №18-фикс).
- **Локализация мёртвая**: `localization/Strings.kt` — legacy-ключи, все экраны пишут русские строки инлайн; i18n для веб-версии (WASM — самый дешёвый канал привлечения) отсутствует.
- **WASM без автотестов** (`wasmJsTest` srcDirs обнулены) — самый платформенно-специфичный код (DOM-видео, координатный позишининг) покрыт только хрупкими координатными e2e-cmp (грабля №55).
- **detekt объявлен, но не подключён ни к одному модулю** (грабля №8) — `./gradlew lint` не quality gate; VM напрямую не тестируются (только через экраны с моками).

#### Низко

- `uploadProgress` — фейковый (30→60→100), нет реального прогресса загрузки.
- `SessionEvents` — mutable-хак `var listener` для моста single→viewModel.
- `generateGuestId()` — ручная генерация UUID v4 (можно `kotlin.uuid.Uuid`).
- `App.kt` — 732 строки: дублированный transitionSpec, `else -> onNavigate(...)` вызывается во время композиции (side-effect), дубли LaunchedEffect-логики Login/Register.
- Мёртвые остатки: `GroupsViewModel` в DI при 0 UI-ссылок, роуты Settings/Messages недостижимы, `ModernAudioPlayer.kt.disabled`, `app/theme/Theme.kt`, три темы в репозитории.

#### Предложения

1. **Разбор монолита «умеренный», не полный ADR-006** (для 12-экранного приложения полная Clean Architecture — YAGNI): один `feature-speaking` (screens + VMs + `SpeakingRepository` + `RecordingStore`) + интерфейсы API (`SpeakingApi`/`AuthApi`/`MessagingApi`/`GuestApi`) + общий `RecordingSessionController` для Training/Practice (сейчас два VM почти копируют друг друга).
2. **Скоуп VMs по маршруту** (`koinViewModel(key = ...)`/nav-scope) — закрывает К3, «load()-сбросы» уйдут.
3. **Навигация поэтапно**: (а) дешёвый промежуточный шаг — `BackStack`-абстракция поверх `AppScreen` + `rememberSaveable` (~100 строк, закрывает К2); (б) затем navigation-compose 2.8 (в каталоге уже есть) с типизированными маршрутами, back-стеком, SavedStateHandle и deep links; `FeatureNavigator` из feature-api уже спроектирован. Риски миграции: перенос анимаций экранов, WASM-history (перекалибровка e2e-cmp).
4. **Удалить мёртвые модули** из `settings.gradle.kts` (`:feature-tests`, `:feature-leaderboard`, `:feature-learning`, `:feature-gamification` — нет исходников; временно исключить `:core*` и `:design`, если миграция не в ближайших спринтах; из `:design` оставить только `SpeakingIcons`).
5. **Ошибки**: типизированный `UiText`/sealed error вместо `error: String?`; маппинг `ApiException → UiText` в repository, а не в компонентах (`ErrorMessage` сейчас сам переводит технические сообщения — грабля №15/№55 остаётся).
6. **Legacy shared-модели**: не удалять (решение владельца), но пометить `legacy/` и запретить новые клиентские импорты; активные контракты вынести в `shared/contracts/`; среднесрочно — генерация моделей из OpenAPI backend'а.
7. **Дубли Result**: оставить один стандарт (практически — kotlin.Result на границе API, удалить кастомные дубли при переработке feature-модулей).

### 2.3. Admin-web (React 18 + TS + MUI 6 + TanStack Query)

#### Сильные стороны

- **Контрактный адаптер `src/api/speakingApi.ts`** — образцовый паттерн изоляции бэкенд-контракта: все расхождения спеки Part 3 ↔ реального API сконцентрированы в одном файле с мапперами.
- **Query-паттерны в `src/hooks/useSpeaking.ts`**: централизованные `speakingKeys`, точечная инвалидация, `keepPreviousData` для пагинации, ручной `setQueryData` после сохранения оценки; кэш как замена отсутствующих GET-by-id.
- **GradingInbox — лучший экран**: фильтры в query-string (шаринг URL, «назад» работает), debounce поиска, серверная пагинация, `isPlaceholderData`-затемнение, осмысленные empty-states.
- **Глубокие E2E**: сериальные CRUD-чейны с сидированием через реальный API, Page Objects, мобильные/планшетные проекты, обход известных граблей MUI (Select клавиатурой, `dispatchEvent`).
- **M3-тема с аудитом контраста**, продуманный `logger.ts` (анти-рекурсия, FIFO-буфер, батчи ≤50), качественные speaking-компоненты (RubricForm с touched-защитой, MediaUploader с автодлительностью, SubmissionAudioPlayer с cache-buster).

#### Критично — фейковые данные и тихий ложный успех

- **К1. Analytics — целиком на мок-данных** (`src/screens/Analytics.tsx:93-159`): `setTimeout(800)` + хардкод (12 458 «пользователей», выдуманные topTests/topUsers/scoreDistribution); выбор дат ни на что не влияет; Export — `console.log`. Реальные данные только в блоке Guest Users. `getAdminLevelDistribution`/`getPopularTests`/`getRecentActivity` существуют в `client.ts`, но не используются.
- **К2. Settings — полностью фиктивный экран** (`Settings.tsx:156-159`): `saveSettings` = `setTimeout(1000)` + возврат входных данных; SMTP/логотипы/уведомления никуда не сохраняются; «Send Test Email» — только snackbar; пользователь видит «Settings saved successfully!» — изменения теряются при перезагрузке. Backend `getAdminSettings` не используется.
- **К3. Dashboard — смесь реальных и выдуманных метрик без маркировки** (`Dashboard.tsx:100-107`): `completionRate: 78.5`, `avgSessionTime: 24.3` — «Mock — not in API yet»; `userGrowth` — «Approximation»; тренды карточек — константы. `fetchDashboardData` глотает ошибку → ветка error недостижима, реальные ошибки маскируются в «0».
- **К4. Users — «действия, которые молча ничего не делают»** (`Users.tsx:104-123`): `createUser`/`_updateUser` бросают «not implemented»; `deleteUser`/`bulkDeleteUsers` — только `console.log`, но мутация резолвится и `onSuccess` срабатывает → **диалог закрывается и выглядит успешным, ничего не удаляя**. Плюс `avgScore: 0`, `totalTimeSpent: 0`, `status: 'active'` — хардкод. Самый опасный из «заглушек»: тихий ложный успех.
- **К5. ErrorBoundary не смонтирован**: `src/components/feedback/ErrorBoundary.tsx` (472 строки) и `ErrorDisplay.tsx` (290 строк) не подключены в `App.tsx` — любой рантайм-краш = белый экран (уже случалось, memory №42).
- **К6. Мёртвый код ~7–8 тыс. строк + выключенные гейты**: `tsconfig.json:14` — `"strict": false`, `noUnusedLocals/Parameters: false`; eslint `no-unused-vars: warn`. Мёртвый инвентарь: `components/users/*` (UserTable 792, UserFilters 578, PermissionEditor 588, GroupManager 824, UserCard 376), `components/charts/*` (BarChart 422, LineChart 334, PieChart 492), `ErrorDisplay`, `data/Pagination` (176), дубль `data/SkeletonCard`, legacy `Toast` (barrel `feedback/index.ts:5` экспортирует **старую** `useToast` — футган), `useTable` (271 строка тестов, содержит баг выбора: индексы vs id), `utils/validators.ts`/`formatters.ts` (тест-онли), Groups/Settings API-функции в `client.ts`.

#### Важно

- **N+1 и хрупкие «детали из кэша»** как следствие отсутствующих backend-endpoint'ов: `getAllSpeakingTopics` = GET libraries + Promise.all(GET topics на каждую); `getTopicQuestions` — на каждый просмотр записи в GradingDetail; детали читаются из кэша со `staleTime: Infinity` → deep-link бросает «Запись не найдена — откройте из списка».
- **Дублирование**: `formatDuration` три семантики (`utils/format.ts` vs `utils/formatters.ts` vs `formatMmSs`), `formatDate`/`formatFileSize` в двух файлах с разными выводами; `formatRelativeTime` — копия в Dashboard и Users; **два vitest-конфига** (`vite.config.ts` + `vitest.config.ts`) и два setup-файла с разными моками; четыре рассинхронизированных списка маршрутов (App.tsx, RouteValidator — там нет `/logs`, nestedRoutes, navItems).
- **Конфиг-мины**: `playwright.config.ts:140-142` ждёт vite на 5173 (слушает 3000) — грабля №11 не исправлена в конфиге; закоммиченный `.env` с `VITE_API_URL=http://localhost:8080` без `/api` (контекст-пат `/api`) — свежий клон без `.env.local` получает 404; coverage-пороги vitest = 0 (закомментированы).
- **a11y-дыры**: IconButton без aria-label в row-actions `DataTable.tsx:439`, кнопка закрытия тоста, иконки экспорта; **публично показаны demo-креды в `Login.tsx:177-187`** (admin@sotospeak.com/admin123) — утечка в prod-сборку; `autoFocus` на кнопке подтверждения ConfirmDialog — риск случайного удаления по Enter.
- **Смешение языков** (Dashboard/Users/Settings/Analytics/Login целиком английские, остальные русские), i18n нет; хардкод-палитра `#4A90D9`/`#F5F5F5`/`#212121` в ~15 файлах, `GlobalStyles.ts` переопределяет body-цвета вразрез с темой.

#### Предложения

1. **Убрать все моки, ввести «честный» контракт данных**: Dashboard/Analytics/Settings должны ходить в реальные эндпоинты (часть уже есть: `/admin/analytics/*`, `/admin/settings`); недостающие метрики — либо добавить на бэкенд, либо показывать явный блок «Нет данных». Прозрачность важнее «красоты» дашборда.
2. **Удалить мёртвый код одним проходом** + включить `strict: true`, `noUnusedLocals`, eslint `no-unused-vars: error`; убрать футган с двумя `useToast`.
3. **Один источник маршрутов**: генерировать Routes/navItems из единого конфига; удалить `VALID_ROUTES` и `RouteValidator` из прода.
4. **Закрыть контрактные дыры бэкенда** (приоритет): GET `/admin/speaking/topics/{id}`, GET `/admin/speaking/submissions/{id}`, PATCH publish, batch-reorder, `GET /admin/speaking/submissions/count?status=NEW` — убирает N+1-агрегации и чинит deep-links.
5. **Единый слой форматеров/ошибок**: один `utils/format` (Intl + locale ru), один хелпер `apiErrorMessage(err, fallback)`, одна `formatRelativeTime`.
6. **Подключить ErrorBoundary** на уровне роута + ErrorDisplay + логирование в `logger`.
7. **Свести тестовую инфраструктуру к одному набору** (один vitest.config, один setup), вернуть coverage-пороги, зарегистрировать `addon-a11y` в Storybook.
8. **Устранить конфиг-мины**: playwright webServer (порт 3000 или `--port 5173 --strictPort`), `.env` с `VITE_API_URL=/api`, убрать demo-креды из UI.
9. **Zod-валидация на границе API** (как уже сделано для форм) — рано ловить дрейф контрактов бэкенда.

---

## 3. ДИЗАЙН

### 3.1. Дизайн-система (Playful Coach × M3)

**Сводка зрелости:** система зрелая по процессу (спека v3.1.1 с версионированием, механический M3-маппинг с запретом импровизации, пиксельные аудиты против мокапов, токенизированный brand-слой, reduce-motion на 4 платформах, WCAG-аудиты) и **незрелая по консистентности артефактов**: 6 копий токенов, из которых актуальны только две.

| Копия токенов | Статус |
|---|---|
| `.docs/design-system/tokens.json` v1.3.1 | ✅ Канон (DTCG, ченджлог, WCAG-пары) |
| `composeApp/.../designsystem/theme/SpeakingTokens.kt` (+ `SpeakingColorScheme.kt`) | ✅ Активная тема приложения, близка к канону |
| `admin-web/src/theme/Theme.ts` | ✅ Активная тема админки, но с локальными дрейфами (dark-hover, статус-чипы) |
| `.docs/design-system/tokens.css` v1.3.0 | ⚠️ Устарел: нет errata v1.3.1; тёмные мокапы рендерятся не по канону |
| `design/.../theme/*.kt` | ⚠️ Мёртвая параллельная система с дрейфами; используется только `SpeakingIcons` |
| `composeApp/.../designsystem/tokens/FunnyColors.kt` (+ Funny-компоненты) | ⚠️ Зомби DS 1.x: компилируется, экранами не используется |

#### Критично

- **Д1. Errata dark-ролей не доехала до extended-палитры → WCAG FAIL в тёмной теме.** `DarkSpeakingColors = LightSpeakingColors.copy(...)` (`SpeakingTokens.kt:97-119`) не переопределяет `onPrimary`/`onSecondary` (остаются `#FFFFFF`), а поля `onSecondary` в `SpeakingColors` **вообще нет**. Реальный кейс: аватар профиля — фон `speaking.secondary` (#B79EED в dark) + белые инициалы ≈ **2.2:1 FAIL** (`ProfileScreen.kt:135,143`; в светлой теме 3.4:1 — ниже AA для обычного текста). Токены v1.3.1 и M3-схема уже исправлены — исправлена только половина.
- **Д2. Таймер не озвучивается TalkBack** — слепой пользователь не знает остаток времени. Токен `timerAnnounceInterval: 5s` и требование брифа (`liveRegion`/`stateDescription`, обновление ≤1/5с) не реализованы: grep по `app/` — 0 использований `.liveRegion(`/`.stateDescription(` вне самого `AccessibilityUtils.kt`. Для приложения, где таймер — центральная механика (80/50/30с, автостоп Practice), это провал основного сценария для скринридер-пользователей.
- **Д3. admin Settings — mock-страница с обманной обратной связью и анти-брендовыми опциями**: primaryColor `#4A90D9/#7B1FA2/#00897B` (вне палитры!), fontFamily Roboto/Open Sans/Lato/Montserrat/Poppins (вместо обязательного Nunito), borderRadius 8 (вместо 16) — прямое противоречие дизайн-системе «HEX 1:1»; «Settings saved successfully!» при пустом saveSettings.

#### Важно

- **`tokens.css` устарел на errata** → тёмные мокапы рисуют primary-кнопки `#3B6FD4`+белый, а приложение — `#8FB3F5`+`#1A2F5E`. Эталон dark ≠ продукт по самому заметному элементу. Плюс CSS вводит `--color-surface-warm: #252B4A`, которого нет в JSON. Версии: styleguide v2.0, mockups v2.1, css v1.3.0, json v1.3.1 — четыре разных «v».
- **`:design` — мёртвая параллельная система с активными дрейфами**: `RecordActive #FFB27D` (канон `#D97238`), `TextMutedLight #6E76A8` (старый WCAG-FAIL вместо `#58609A`), timerLevels не затемнены, radius 8/12/16 (активная 12/16/22), `Chip = 8dp` (канон 12), scrim непрозрачный. Ловушка: любой новый код, импортировавший `:design`, молча получит устаревшие значения.
- **Nunito не подключён в Compose**: `FunnyTypography.kt:27` — `NunitoFontFamily = FontFamily.SansSerif`; Android/iOS/desktop/WASM рендерятся системным шрифтом, хотя мокапы и админка — Nunito. Pixel-аудиты списывали на «растеризацию Chromium vs Skia», причина глубже — другой типфейс. JetBrains Mono в админке упомянут в font-family, но **не импортирован**.
- **Массовый хардкод-цвет**: admin-web — `#4A90D9` (легаси-синий, не токен!) в ~15 файлах, `#F5F5F5/#212121/#757575/#E0E0E0`, `#8a5200` в `GradingDetail.tsx:159` и `GradingNavBadge.tsx:20` (спека прямо запрещает); `GlobalStyles.ts:12-13` и `Login.tsx:73` — body `#121212/#F5F5F5` (не токены! фон бренда — `#EEF3FF/#161A2E`); composeApp — `THEME_GRADIENTS` (`LibraryScreen.kt:281-286`) с `#4A90D9/#2E6BB0` и `#5C6BC0/#3F51B5`, `VideoScreen.kt:469,497` `#1A2E42`, `VideoScreen.kt:622` `#FFD666` (CC-актив) — даже не в tokens.json; `SubmissionAudioPlayer.tsx:32` — хардкод-цвет волны без dark-варианта.
- **Dark-hover кнопок MUI использует светлые токены**: `Theme.ts:695-708` — dark containedPrimary hover → `#5B8DEF` (это светлый primary!), text `#161A2E` (вместо errata `#1A2F5E`); dark containedSecondary hover → `#9B7EDE`. M3 state-layer оверлеи для кнопок не применены — hover «прыгает» на чужие токены.
- **Статус-чипы админки в dark остаются светлыми**: `speakingDark` не переопределяет `status.*`; Compose в dark рисует `#3D2A0A/#1B4D1F` — две платформы по-разному рисуют один статус.

#### Низко

- Motion-хелперы (`PageTransitions.kt`/`ScaleTransition`) используют стоковые Ease-функции вместо токенов; `LoadingSkeleton` при reduce-motion **замедляет** shimmer до 3000ms вместо отключения (мёртвый код, но две motion-системы в одном `designsystem/`).
- Скелетоны не используются вообще: загрузка списков — `CircularProgressIndicator`, хотя UX_GUIDELINES рекомендует skeleton для списков.
- Зомби DS 1.x: `FunnyButton/Card/TextField/Snackbar/Progress/Badge`, `FunnyColorScheme`, `FunnyColors` (#2563EB!), `FunnySpacing` (SpaceMd=16 при токене 12, CardRadius=16 при 22) — компилируются и провайдится `LocalFunnyColorScheme`.
- a11y-строки (`AccessibilityUtils`) — на английском при русском UI, не вынесены в ресурсы.
- Маппинг DSM-5 §2 для MUI не доведён: `h3 = timerDisplay 64px` не реализован (h3 = 28px), `subtitleText 17/600 → body1` не применён (body1 = 16/400).
- Тёмная тема Android **не аудировалась** пиксельно (пробел QA) — а именно там живут Д1 и В1.
- `WindowSize.toNavigationType()` маппит EXPANDED → `NAVIGATION_DRAWER`, а реализация и спека — medium/expanded → NavigationRail.

#### Предложения

1. **Один источник токенов → генерация остальных**: скрипт, генерирующий `tokens.css`, `SpeakingTokens.kt`/`SpeakingColorScheme.kt`, `Theme.ts` из `tokens.json`, с CI-гейтом «diff-пусто». Это устранит 80% дрейфов (В1, В2, часть В4).
2. **Ликвидировать/заморозить `:design`** (рекомендуется — оставить только `SpeakingIcons`, приложение уже на `designsystem/`) и зомби DS 1.x (`Funny*`).
3. **Вернуть `onSecondary` в `SpeakingColors`** и задать в `DarkSpeakingColors` `onPrimary/onSecondary = #1A2F5E`; аватар профиля — на `secondaryContainer`/`onSecondaryContainer`. Закрывает Д1.
4. **Подключить Nunito в Compose** (bundled font через composeResources) и импортировать JetBrains Mono в админку — реальная кросс-платформенная консистентность типографики.
5. **«Хардкод-аудит»**: ни одного hex вне токенов; запрет `#4A90D9/#F5F5F5/#212121/#8a5200`; `#FFD666` — добавить токеном в tokens.json (v1.3.2); `GlobalStyles`/`Login` body — на `theme.palette.background.default`.
6. **Обновить `tokens.css` до v1.3.1** (errata + тёмные on-роли) и перегенерировать тёмные рендеры мокапов; добавить в аудит пару «dark-кнопка мокапа ↔ dark-кнопка приложения».
7. **MUI dark-hover по M3 state layers** (alpha-оверлей 8% поверх `#8FB3F5`), убрать `#5B8DEF/#9B7EDE` из dark-оверрайдов.
8. **Статус-токены dark для админки** + единый компонент `StatusChip` в обоих клиентах.
9. **Вынести a11y-строки в ресурсы**, синхронизировать версии артефактов, убрать мёртвые параметры темы.

### 3.2. UX-предложения (топ-5)

1. **Таймер, который «слышно»** (stateDescription/liveRegion по `timerAnnounceInterval` + вибро/звук последних 5 секунд) — основная механика перестаёт быть недоступной незрячим; все реже «обрывают запись на середине слова».
2. **Вернуть объяснение Training vs Practice на экран вопросов** (потеряно в QA2) + explainer «повторная отправка запрещена» — снимает страх «запись уйдёт» и злость от 409.
3. **Починить MySubmissions**: убрать стрелку «назад» (экран в bottom-nav), вернуть подзаголовок/бейдж/карточку по frame-submissions (открытые bd `xic`, `0zl`).
4. **Скелетоны + пустые состояния с иллюстрацией и CTA** для Library/Topics/Submissions (компоненты уже есть в `LoadingSkeleton.kt`, но не используются).
5. **Честная тёмная тема админки** (чипы, body-фон, hover) — убрать «мигание» между тёмным интерфейсом и яркими не-брендовыми элементами.

**Непокрытые дизайном зоны:** skeleton-композиции и error-banner'ы на уровне экранов; iOS-специфика (safe-area, back-swipe, VoiceOver); планшеты/landscape/двухпанельные раскладки (`supportsListDetail()` заявлен, не реализован); анимации bottom-sheet и состояния «отправляется»; иконки (`SpeakingIcons` не хватает Close/X, Volume, Edit, Settings, Info, Alert, Download — приложение на WASM не может использовать Material-иконки, грабля №75); контраст аватара, fontScale 1.3–2.0, high-contrast режим.

---

## 4. ФУНКЦИОНАЛ

### 4.1. Для ученика

1. **Эталонный ответ после Training** — аудио/текст модельного ответа на вопрос + кнопка «Прослушать пример» после каждой попытки: самостоятельная работа над произношением без учителя (ключевая ценность тренажёра в офлайне/между занятиями).
2. **Детальная карточка оценки + тренд**: разбор по 4 критериям (`SpeakingGrade` уже содержит grammar/vocabulary/pronunciation/fluency, но MySubmissions показывает только суммарный балл/чип), «прогресс по темам: 6.2 → 7.1» — видимость прогресса → мотивация и retention; учителю меньше ручной обратной связи.
3. **Офлайн-режим топика**: кэш видео/субтитров/вопросов (Coil + KtorDataSource уже умеют) + фоновая доставка Practice-записей (сейчас retry только при входе на MySubmissions) — сценарий метро/перелётов, снимает «красный» кейс потери записи.
4. **Словарь из транскрипта**: тап по слову в TranscriptPanel (пословная подсветка уже есть) → карточка слова (перевод, сохранить в личный словарь) — vocabulary building внутри контента, дифференциация от «просто видео+запись».
5. **Цель дня / мягкие напоминания**: «Цель: 1 запись в день» + локальное напоминание (WorkManager на Android / push на WASM-подписку) — регулярность практики = главная метрика retention; реализуется локально, без backend.

### 4.2. Для учителя (admin-web)

1. **Реальная grading-аналитика** (в Analytics или отдельной вкладке): средний балл по рубрике, время от записи до проверки, очередь NEW, распределение оценок по топикам + рабочий экспорт CSV (`getAdminLevelDistribution`/`getPopularTests` уже есть).
2. **Потоковая проверка в GradingDetail**: hotkeys ←/→ по очереди NEW, шаблоны комментариев, быстрые «+1 балл» — закрывать очередь в разы быстрее.
3. **Рабочие Users и Settings** (см. К2/К4 выше) — вместо тихого вранья интерфейса; серверная пагинация/поиск учеников.
4. **Дашборд «что проверять»**: «N записей ждут проверки» (бейдж NEW уже есть), «M топиков опубликованы без видео/вопросов» (предикат `isNotPlayable` уже написан в `SpeakingTopics.tsx:51`), «K учеников неактивны 7+ дней» — всё реально считается из существующих API.

### 4.3. Для продукта (backend/инфраструктура)

1. **Аналитика speaking-модуля**: агрегаты по grades (средние по критериям по topicId/libraryId, распределение оценок, время NEW→REVIEWED, retention по топикам) + CSV; сейчас админ-аналитика считает только legacy-тесты.
2. **Уведомление об оценке** (email уже настроен / SSE/WebSocket) — «Мои записи» обновляется без поллинга.
3. **Кэш + ETag для публичного контента** (Caffeine + HTTP cache headers для `/public/speaking/*`, инвалидация при publish) — быстрый старт веб-версии, меньше нагрузки на БД.
4. **Геймификация на реальных данных speaking**: переиспользовать `user_streaks`/`achievements` — стрики за практику, ачивки за N отправок/оценку ≥ 8, прогресс по топикам (сейчас либо мёртвый legacy, либо фиктивные данные).
5. **Пагинация + фильтры в «Моих записях»**, агрегат «средний балл по всем топикам» — продуктовая ценность профиля ученика.

---

## 5. Специфичные находки, требующие решения владельца (ADR-007)

- **Дрейф спеки vs код по дублирующим отправкам**: `openspec/specs/speaking-practice/spec.md` («Повторные отправки на один и тот же топик SHALL быть разрешены») и PRD (`SPEAKING-TRAINER-001.prd.md` — «Разрешено; каждая отправка — отдельная запись в inbox`) расходятся с реализацией: backend с 2026-08-03 отклоняет повторные отправки 409 `DUPLICATE_SUBMISSION` (решение владельца, bd 7vm), клиент это обрабатывает. По правилу 5 AGENTS.md спека, расходящаяся с решением владельца, обновляется немедленно — требуется bump спек (openspec speaking-practice + PRD) через ADR-007 (human-in-the-loop).
- **Открытые bd-задачи уже существуют**: `c47` (Video: субтитры без карточки, CTA прижата), `xic` (MySubmissions: заголовок/бейдж/карточка), `0zl` (Questions: CTA + explainer), `j8r` (media3-session cleanup), `4d1` (KtorDataSource — in_progress, ждёт живого Android-гейта); deferred `8zm` (судьба backend legacy-эндпоинтов).
- **README.md описывает легаси-продукт** (тесты/достижения/лидерборд, 0 упоминаний speaking-тренажёра) — вводит в заблуждение новых участников; обновить под текущий продукт.
- **Гигиена репозитория**: в git закоммичены артефакты отладки в корне (admin-*.png, wasm_*.png, backend-*.log, compile_*.log, sb-*.png, test-editor-hotspots.png и др.) — вынести в `.gitignore`/удалить; в корне ~40 PNG/лог-файлов.

---

## 6. Приоритизированный план (предлагаемый порядок работ)

### Quick wins (1–2 дня, без спека)

1. Миграция с удалением demo-юзера из V1; отключить OSIV.
2. Подключить ErrorBoundary в App.tsx; включить `strict: true` и `no-unused-vars: error`; удалить мёртвый код (админка ~8 тыс. строк, мёртвые Gradle-модули, зомби DS).
3. `DarkSpeakingColors` — errata `#1A2F5E` + добавить `onSecondary`; обновить `tokens.css` до v1.3.1.
4. Исправить `playwright.config.ts` (порт 3000) и закоммиченный `.env` (`VITE_API_URL=/api`); убрать demo-креды из Login.
5. Хардкод-аудит: `#4A90D9/#F5F5F5/#212121/#8a5200` → токены.

### Средние (1–2 спринта, через OpenSpec)

6. Честные данные в админке: Analytics/Settings/Dashboard/Users на реальные endpoint'ы (или явные «Нет данных»); закрыть контрактные дыры бэкенда (GET-by-id, count NEW, PATCH publish).
7. Навигация клиента: BackStack + rememberSaveable → затем navigation-compose; скоуп VMs по маршруту.
8. Разбор `SoToSpeakApi` на интерфейсы; общий `RecordingSessionController`; `UiText` вместо `error: String?`.
9. Nunito в Compose; скелетоны; a11y таймера (TalkBack).
10. Testcontainers-Postgres; security-контракты в тестах.

### Долгие (стратегические решения владельца)

11. Судьба legacy: backend-эндпоинты (удалить/изолировать за профилем) → разморозка shared-моделей; единый контракт через OpenAPI.
12. Продуктовое решение по iOS (доделать AVPlayer/AVAudioRecorder или убрать таргеты) и WASM-рекордеру (MediaRecorder API) — веб дешевле iOS для привлечения.
13. Один источник дизайн-токенов с генератором и CI-гейтом.
14. ASR-предскоринг произношения — экономия времени учителя на грейдинге.

---

## 7. Итог

Инженерная культура проекта — сильная (паттерны, тесты, спеки, грабли), а основные риски — не качество кода, а три типа долга:

1. **Legacy-пласт**, который «компилируется, но не подключён/сломан» и создаёт иллюзию модульности (`core`/`feature-*`/`:design` без использования, сломанные legacy-контроллеры, мёртвый код админки).
2. **Фейковые данные в админке**, на основе которых владелец может принимать продуктовые решения (Analytics/Settings/Dashboard/Users).
3. **Расходящиеся дизайн-артефакты и незакрытая errata** (6 копий токенов, dark-WCAG-FAIL, устаревший tokens.css, Nunito не в приложении).

Плюс точечные дыры безопасности (demo-аккаунт в миграции, публичная утечка черновиков, OAuth без проверки у провайдера, X-Forwarded-For).

**Приоритет:** безопасность (Б1–Б4) → честность данных (К1–К4 админки) → скоуп VMs + навигация клиента → чистка мёртвого кода → единый источник токенов → стратегические решения (legacy, iOS/WASM, ASR).
