# Пакет диффов спек на утверждение владельца (ADR-007)

> Дата: 2026-08-01. bd: `So to Speak-61m` (MVP-2), `So to Speak-hxd` (DESIGN_SYSTEM_SPEC v2.0).
> Спеки не изменены — это предложение. После «утверждаю» агент внесёт правки с bump версий и строками ченджлога.

## 1. `docs/SPEAKING_TRAINER_SPEC_PART2.md` → v1.4 (minor)

**Причина:** после guest-first редизайна (bd `clv`/`2tv`, 2026-08-01) код разошёлся со спекой: стартовый флоу, онбординг, гейтинг Practice, bottom nav и профиль реализованы по мокапам Playful Coach v1.1, а не по спеке v1.3.

**Предлагаемые правки:**

| Участок | Было (v1.3) | Станет (v1.4) |
|---|---|---|
| §1 R7 / §6.2 / §10 (гейтинг гостя) | Practice-гейт в стиле `LockedFeature` (замок + CTA «Войти/Зарегистрироваться»), событие `ShowLoginCta` | `SpeakingGate` (компонент `app/components/SpeakingAuth.kt`) в нижней зоне QuestionsScreen: «Ты почти у цели!» + кнопки «Зарегистрироваться» → Register и «Войти» → Login. `ShowLoginCta` удалён как недостижимый |
| §2 (граф навигации, старт) | `Splash/Login ──► Library …` | Guest-first: `Splash → (первый запуск → Onboarding 3 слайда, «Начать») → Library`; UNKNOWN-сессия стартует `startGuestSession()` синхронно до навигации; Login/Register достижимы только из авторизованной зоны (Practice-гейт QuestionsScreen, гостевой профиль) |
| Онбординг | (не описан / старый флоу с выбором режима «Как начнём?») | 3 слайда value-prop по мокапу frame-onboarding (🎬/🎙️/📨), CTA «Далее»×2 → «Начать»; экрана выбора режима НЕТ; регистрации на онбординге НЕТ — гость сразу в Library |
| Bottom nav | «Библиотека/Мои записи/Профиль» | «Темы/Отправки/Профиль» (иконки SpeakingIcons Home/Send/User) |
| Профиль | базовый экран + настройки | По мокапам frame-profile/profile-guest: аватар с инициалами, stat-карточки (отправки/топики) из MySubmissionsViewModel, «Выйти» danger-ghost (logout переехал из Settings); гость — GuestProfileStub с «Зарегистрироваться»/«Войти»; карточки Messages/Settings удалены из профиля |
| §11 (Maestro-флоу) | онбординг-subflow «Смотри и повторяй»/«Продолжить как гость», bottom-sheet субтитров, «Мои записи» | Актуальные тексты: онбординг «Смотри видео» → «Начать»; bottom-sheet удалён (DC-5) — «Перейти к вопросам»; «Отправки»; заголовок Library «Библиотека тем»; record-кнопка по contentDescription «Начать запись»/«Остановить запись» |

**Ченджлог:** `v1.4 (2026-08-01): guest-first старт и онбординг по мокапам Playful Coach (3 слайда, без выбора режима); Practice-гейт SpeakingGate; bottom nav «Темы/Отправки/Профиль»; профиль по frame-profile; Maestro-флоу синхронизированы с реальными текстами (включая удаление bottom-sheet субтитров DC-5)`.

## 2. `docs/SPEAKING_TRAINER_SPEC_PART3.md` → v1.1 (patch)

**Причина:** спека писалась до backend; при реализации (Фаза 3) контрактная адаптация сосредоточена в `admin-web/src/api/speakingApi.ts`. Дифф уже предъявлялся владельцу в отчёте Фазы 3 — фиксируем в спеке.

**Предлагаемые правки (раздел про API-контракты):**

- Publish library/topic — через `PUT` (поле `isPublished`), отдельного `PATCH /publish` в backend нет.
- Маппинг полей backend → спековые типы: `title/topicCount/isDeleted/total/durationSec`.
- Детали library/topic/submission — из кэша списков (GET by id в backend нет).
- Reorder вопросов/топиков — цепочка `PUT` с `displayOrder` (batch-endpoint отсутствует).
- Вопросы топика — из вложенных `questions` `AdminTopicResponse` (публичный detail отдаёт только опубликованное).
- Submissions: плоский ответ нормализуется во вложенную структуру на клиенте; фильтры `dateFrom/dateTo`; пагинация Spring Page (поле `number`).
- DELETE Library — hard delete каскадом; 400 при наличии submissions.

**Ченджлог:** `v1.1 (2026-08-01): контрактная адаптация к реализованному backend (publish через PUT, детали из кэша списков, reorder цепочкой PUT, вопросы из AdminTopicResponse, маппинг полей) — зафиксирован адаптер speakingApi.ts`.

## 3. `docs/DESIGN_SYSTEM_SPEC.md` → v2.0 (major)

**Причина:** спека v1.0 (2024-02-03, gamification-first, аудитория 7–14 лет) устарела после пивота и выбора Playful Coach v1.1 (владелец, 2026-07-31). Дифф представлен в отчёте Фазы 4 (bd `So to Speak-hxd`).

**Предлагаемая структура v2.0 (перезапись):**

- Version 2.0, дата 2026-08-01, причина: пивот So to Speak → Speaking-тренажёр, дизайн-система Playful Coach v1.1 + аудит-токены v1.2.0.
- Источник истины токенов: `.docs/design-system/tokens.json` (HEX 1:1 обязателен): primary `#5B8DEF`, primaryStrong `#3B6FD4`, secondary `#9B7EDE`, record `#FF9F6B` ≠ error `#E53935` (errorText `#B3261E`), bg `#EEF3FF`, textMuted `#58609A`, timer `#4A7FE8/#8A68D6/#D97238`, radius 16/22/12 (squircle rec-кнопка 22), индиго-тени.
- Реализация: `:design` (SoToSpeakColorScheme/SpeakingTypography/SpeakingElevation), `composeApp/designsystem/theme/SpeakingTokens.kt`, `SpeakingIcons.kt` (15 ImageVector), `SpeakingMotion` + reduce-motion, компоненты `SpeakingRecording.kt`/`SpeakingTimerRing`; MUI `admin-web/src/theme/Theme.ts` (полный ребренд, Nunito).
- Поведенческие требования мокапов (`mockups.html`) авторитетны: Training 3 попытки 80/50/30 без удаления, Practice без Review с автоотправкой, guest-first онбординг.
- WCAG: чипы статусов на container-фонах с тёмным текстом (AA), record ≠ error, touch-таргеты 48dp.
- Legacy-разделы v1.0 (gamification-компоненты, детская аудитория) — в приложении «Архив v1.0» со ссылкой на git-историю.

**Ченджлог:** `v2.0 (2026-08-01): полная замена gamification-first спеки на Playful Coach v1.1/v1.2.0 (пивот продукта в speaking-тренажёр)`.

---

**После утверждения:** агент внесёт правки, проставит Version/дату/ченджлог, закроет `So to Speak-hxd` и `So to Speak-61m`, зафиксирует в memory.md.
