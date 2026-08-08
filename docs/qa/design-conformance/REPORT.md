# DC-1: Дизайн-аудит — приложение vs демо-макеты

**Дата:** 2026-08-01 · **Источник истины:** `.docs/design-system/mockups.html` (11 фреймов: 5 исходных + auth/onboarding/profile из A0) · **Метод:** скриншоты живого приложения (WASM 390×844, admin-web 1400×950) vs скриншоты фреймов мокапов.

> **Обновление 2026-08-01 (после решения владельца «всё под мокап»):** все пункты «расхождение» реализованы (DC-2…DC-6), статусы отмечены ниже как ✅ ИСПРАВЛЕНО.

| Фрейм | Мокап | Приложение |
|---|---|---|
| Library | `mockup-library.png` | `app-library.png` |
| Video | `mockup-video.png` | `app-video.png` (wasm-стаб) |
| Training | `mockup-training.png` | `app-training.png` |
| Practice | `mockup-practice.png` | `app-practice.png` |
| Grading | `mockup-grading.png` | `admin-grading.png` (inbox), `admin-grading-review.png` (review) |

Токены (primary #5B8DEF, record #FF9F6B, фон #EEF3FF, радиусы карточек, Nunito) — **совпадают визуально** во всех фреймах. Расхождения — по элементам и компоновке.

## Реестр расхождений

### Library (→ DC-2)

| # | Элемент | Мокап | Приложение | Вердикт |
|---|---|---|---|---|
| L1 | Тайл иконки темы | Цветной квадрат с инициалами (Tr/Fd/Jb/Hb, разные цвета) | Одинаковая синяя иконка-книга | ✅ ИСПРАВЛЕНО (ThemeCover, градиент по хешу id) |
| L2 | Бейджи статуса | «2 ПРОЙДЕНО» (зелёный), «НОВАЯ» (оранжевый) | нет | ✅ ИСПРАВЛЕНО (ThemeStatusChip, прогресс из RecordingStore) |
| L3 | Прогресс темы | progress-bar под названием | нет (чип «1» справа) | ✅ ИСПРАВЛЕНО (ThemeProgressBar 4dp, анимация width) |
| L4 | Подзаголовок | «Выбери тему и начни говорить» | нет | ✅ ИСПРАВЛЕНО |
| L5 | Заголовок | «Библиотека тем» | «Библиотека» | ✅ ИСПРАВЛЕНО |
| L6 | Bottom nav | «Темы / Отправки / Профиль» | «Библиотека / Мои записи / Профиль» | ✅ ИСПРАВЛЕНО (лейблы мокапа, блок B) |
| L7 | Счётчик топиков | «6 топиков» текстом | цифра в чипе | ✅ ИСПРАВЛЕНО (плюрализация) |

### Video (→ DC-5)

| # | Элемент | Мокап | Приложение | Вердикт |
|---|---|---|---|---|
| V1 | Плеер | Кастомный: play-круг, прогресс 0:00/1:35, CC-кнопка с подсветкой | wasm — стаб «видео недоступно» (by design, Part 2 §3.2); Android — Media3 с нативными контролами | ✅ ИСПРАВЛЕНО (MockupVideoControls: big-play 64dp, control-bar, CC #FFD666; нативные отключены) |
| V2 | Тоггл субтитров | Segmented-чипы на экране | чипы есть ✅ + дублирующий bottom-sheet при входе (app-only) | ✅ ИСПРАВЛЕНО (bottom-sheet убран, сразу видео) |
| V3 | Субтитры | Карточка с текстом реплики под плеером | на wasm не видно (стаб) | проверить на Android |
| V4 | Подсказка | «Смотреть всё видео необязательно — …» | есть «…» | уточнить |
| V5 | CTA | «Перейти к вопросам» | «Перейти к вопросам» ✅ | совпадает |
| V6 | Error-state | нет | «Не удалось загрузить видео» + Повторить/К вопросам (app-only, хорошо) | ok (app) |

### Training (→ DC-3)

| # | Элемент | Мокап | Приложение | Вердикт |
|---|---|---|---|---|
| T1 | Таймер | Крупный «1:20» внутри кольца + «лимит попытки» | мелкий шрифт | ✅ ИСПРАВЛЕНО (TimerDisplay 64sp в кольце) |
| T2 | Idle-кольцо | видно ДО записи | нет в idle | ✅ ИСПРАВЛЕНО (training_timer_idle) |
| T3 | Record-кнопка | большой оранжевый круг #FF9F6B | маленькая серая squircle с микрофоном | ✅ ИСПРАВЛЕНО (SpeakingRecordButton 72dp + recPulse) |
| T4 | Карточки вопросов | нумерованные (1..5) | без нумерации | ✅ ИСПРАВЛЕНО (question_number_N) |
| T5 | Подпись попытки | «Попытка 1 · ответь на все вопросы одной записью» | «Ответь на все вопросы одним голосовым сообщением» (без № попытки) | ✅ ИСПРАВЛЕНО (+ «Попытки · N из 3») |
| T6 | Level chip + точки | «Уровень 1 · 80 сек» + 3 точки | ✅ совпадает | ok |
| T7 | Плашка хранения | «Записи хранятся только на твоём устройстве» | ✅ совпадает | ok |
| T8 | Mic-warning | нет | «Для записи голоса нужен доступ к микрофону» (контекстный) | ok (app) |

### Practice (→ DC-4)

| # | Элемент | Мокап | Приложение | Вердикт |
|---|---|---|---|---|
| P1 | Заголовок | «Practice» + тема подзаголовком | back + «Знакомство» | ✅ ИСПРАВЛЕНО |
| P2 | Record-кнопка | большой оранжевый круг | серая кнопка «Начать запись» | ✅ ИСПРАВЛЕНО (SpeakingRecordButton) |
| P3 | Нумерация вопросов | 1..5 | нет | ✅ ИСПРАВЛЕНО |
| P4 | Плашка автоотправки | жёлтая, внизу: «В отличие от Training, эта запись уйдёт учителю автоматически сразу после остановки таймера…» | фиолетовая, сверху: «Запись уйдёт учителю автоматически — изменить её нельзя» | ✅ ИСПРАВЛЕНО (жёлтая #FFE0B2/#8a5200, внизу, текст мокапа) |
| P5 | Чипы | «Контрольная · 30 сек» (peach) + «1 ЗАПИСЬ НА ВСЕ ВОПРОСЫ» (жёлтый, caps) | те же чипы, другой стиль второго | ✅ ИСПРАВЛЕНО (#FBEAE8/#B3261E + caps) |
| P6 | Таймер | крупный 0:30 + «на все ответы» | ✅ совпадает (кольцо есть в idle!) | ok |
| P7 | Mic-warning | нет | красный текст над кольцом | ok (app) |

### Grading, admin (→ DC-6)

| # | Элемент | Мокап | Приложение | Вердикт |
|---|---|---|---|---|
| G1 | Плеер | waveform с прогрессом 0:12/0:30 | стандартный слайдер + Download audio | ✅ ИСПРАВЛЕНО (56 баров, played/unplayed, seek по клику, метки времени) |
| G2 | Рубрика | слайдеры с крупным значением справа (7/6/8/7) | слайдеры + number-input «5» | ✅ ИСПРАВЛЕНО (big value, number-input убран) |
| G3 | Общий балл | фиолетовая плашка «Общий балл (среднее) 7.0» | «5.0» + оранжевый чип «авто-усреднение» | ✅ ИСПРАВЛЕНО (avg-панель #E5DCFF) |
| G4 | Действия | «Пропустить» + «Сохранить оценку» | только «Save grade» | ✅ ИСПРАВЛЕНО (client-side skip → след. NEW) |
| G5 | Карточка студента | аватар + имя + тема + дата | текстовый заголовок «Submission: Demo — Знакомство» | ✅ ИСПРАВЛЕНО |
| G6 | Навигация admin | top-bar «So to Speak Admin» + вкладки | MUI sidebar (осознанный шаблон админки) | by design (принято владельцем) |
| G7 | Вопросы | нумерованные | нумерованные ✅ | ok |
| G8 | Бейдж NEW в табе | «Grading 7 NEW» | есть счётчик в меню? | ✅ ИСПРАВЛЕНО (GradingNavBadge в sidebar) |

## Выводы

1. ~~**Системные расхождения (приоритет):**~~ — все реализованы 2026-08-01 (DC-2…DC-6, решение владельца «всё под мокап»).
2. **Тексты** (T5, P4, L6) — приведены к мокапу по решению владельца.
3. **By design (принято владельцем):** wasm-стаб плеера (V1), error-state видео (V6), mic-warnings (T8/P7), MUI-sidebar админки (G6).
4. **A0 (2026-08-01):** mockups.html дополнен фреймами auth/onboarding/profile (`frame-onboarding`, `frame-login`, `frame-register`, `frame-locked`, `frame-profile`, `frame-profile-guest`) — редизайн auth/профиля по ним (bd `clv`/`2tv`).

## Воспроизведение

```bash
# Мокапы → docs/qa/design-conformance/mockup-*.png
cd e2e-cmp && node shoot-mockups.js
# Приложение (нужен wasm dev server на 8081 + backend :8080)
node shoot-app.js        # guest/auth флоу (калиброванные клики внутри)
node shoot-admin.js      # admin :3000, логин admin@sotospeak.com
```

---

# DC-7: E2E-верификация после реализации (2026-08-01, вторая итерация)

Прогон `shoot-mockups.js` (11 фреймов), `shoot-app2.js`/`shoot-app-auth.js` (wasm 390×844, свежая сборка), `shoot-admin.js` (docker-стек, пересобранный admin).

## Сверка по фреймам

| Фрейм | Вердикт | Скриншоты |
|---|---|---|
| Library | ✅ соответствует: заголовок «Библиотека тем» + подзаголовок, цветные тайлы с инициалами (EG/РА), бейдж «НОВАЯ», прогресс-бар, chevron, bottom nav «Темы/Отправки/Профиль» | `app-library.png` vs `mockup-library.png` |
| Video | ✅ кастомные контролы (big-play, control-bar, CC), hint мокапа; error-state на wasm — by design (V6) | `app-video.png` |
| Training | ✅ idle-кольцо 1:20 «лимит попытки», level-chip «Уровень 1 · 80 сек» + точки, нумерация вопросов, squircle rec-кнопка (микрофон disabled — нет permission в wasm, by design T8), «Попытка 1 · …», «Попытки · 0 из 3», privacy-note | `app-training.png` |
| Practice (gate) | ✅ locked-гейт «Ты почти у цели!» + «Зарегистрироваться»/«Войти» на экране вопросов (вопросы и Training остаются доступны гостю — осознанное отклонение от full-screen макета, чтобы не скрывать контент) | `app-questions.png` |
| Onboarding | ✅ emoji-карточка, dots (активный — пилюля), «Далее» primaryStrong | `app-onboarding-1/2/3.png` |
| Login | ✅ «С возвращением!», поля Email/Пароль (label сверху, radius 16, border outline), «Войти» primaryStrong, «Нет аккаунта? Регистрация», «Продолжить как гость» | `app-login.png` vs `mockup-login.png` |
| Register | ✅ «Создай аккаунт», Имя/Email/Пароль, плейсхолдеры мокапа | `app-register.png` vs `mockup-register.png` |
| Profile (auth) | ✅ аватар-круг с инициалом, имя/email, stat-карточки «записи отправлено»/«темы пройдено», «Выйти» danger-ghost | `app-profile.png` vs `mockup-profile.png` |
| Profile (guest) | ✅ 📬-круг, текст мокапа, «Зарегистрироваться» + «Уже есть аккаунт? Войти» | `app-profile-guest.png` |
| Grading (admin) | ✅ waveform-плеер с метками времени + download icon, карточка студента (аватар/имя/мета/NEW-чип), рубрика big-value слайдеры 1/5/10, фиолетовая avg-панель, бейдж «8 new» в sidebar | `admin-grading-review.png` vs `mockup-grading.png` |

## Аудит-токены (применены 2026-08-01, tokens v1.2.0)

- `primaryStrong #3B6FD4` — белый текст на кнопках/nav/CTA (composeApp + MUI `primary.main`).
- `textMuted #58609A` (5.32:1 AA), timer `#4A7FE8/#8A68D6/#D97238`, `recordContainer/onRecordContainer`, `onRecord #2D3561`.
- Rec-кнопка — squircle 22dp (НЕ круг, tokens.json + аудит): `SpeakingRecordButton` обновлён, `.rec-btn` в mockups.html тоже squircle.

## Анимации (H)

- `Modifier.speakingPressable()` (scale .98, tweenFast 150ms ease-standard) на карточках Library.
- recPulse 1600ms на SpeakingRecordButton при записи; RecordingWaveform 1100ms alternate (1:1 мокапу); CheckPopAppear на sent-badge/✅; idle-кольцо таймера; big-play fade+scale tweenFast; upload-track 180ms linear; reduce-motion покрыт везде.

## Тесты (по exit code)

- `:composeApp:desktopTest` — **83/83** (новые: DC-2/DC-3/DC-4/DC-5 сьюты + auth/profile).
- admin-web `vitest` — **250/250**, `lint` — 0 warnings.

## Доработка аудита (итерация 3, 2026-08-01)

Доприменены оставшиеся пункты аудита дизайн-агента:
- `recordActive #D97238` (waveform записи ≥3:1) — tokens.json/css, SpeakingTokens.kt, MUI Theme.ts.
- Touch target 48px: `.vc-btn`/`.mode-chip` в mockups.html; `vc_play_pause`/`vc_cc` 48dp в VideoScreen.
- Bottomnav-иконки `i-home`/`i-send`/`i-user` добавлены в icons.svg, мокапы и SpeakingIcons.kt (Home/Send/User); bottom nav приложения переведён на них.
- ease-bounce для ✅ — уже был (CheckPopAppear на SpeakingMotion.tweenBounce).
- desktopTest 83/83 после всех правок.

---

## M3-конформити (2026-08-07, эпик bd `FunnyEnglish-2mz`/`oyh`)

**База сверки обновлена**: mockups.html v2.0 / styleguide.html v2.0 (M3) — см. DESIGN_SYSTEM_SPEC v3.0 §7.
Компонентная база реализации переведена на Material 3 (реестр `docs/design/M3_REPLACEMENT_REGISTRY.md`,
маппинг `docs/design/M3_IMPLEMENTATION_MAPPING.md`).

**Гейты M3-реализации:**
- composeApp: `desktopTest` 95/95, `compileKotlinWasmJs`, `:app:assembleDebug` ✅
- admin-web: `vitest` 256/256, `lint` 0, `typecheck` 0, `build` ✅
- Maestro (эмулятор, docker-стек): **4/4** ✅ (login, practice_auth, guest_gating, training — на M3-UI)
- e2e-cmp (WASM): **51 passed / 11 skipped / 0 failed** ✅ — спеки перекалиброваны под M3
  (NavigationRail на wide, Q4; `shoot-calibrate-m3.js`; global-setup purge E2E-библиотек)
- admin-web Playwright smoke (theme-toggle + navigation, все проекты): **17/17** ✅

Скриншоты M3-UI (1280x720): `e2e-cmp/test-results/calib-m3/` (library/topics/questions/training/profile/login,
light). Полное визуальное ревью против mockups v2.0 — за владельцем (DSM-7, bd `FunnyEnglish-dmb`).

### Спот-сверка app ↔ mockups v2.0 (2026-08-08)

- **frame-library ↔ app Library** — структурно совпадает: ElevatedCard темы, chip НОВАЯ/ПРОЙДЕНО (container + тёмный текст), LinearProgressIndicator 4dp, nav pill-индикатор (bottom nav на compact / NavigationRail на wide). Скриншоты: `e2e-cmp/test-results/mockup-frame-library.png` vs `bg-audit/{light,dark}/library.png`.
- **frame-questions ↔ app Questions** — ⚠️ **расхождение текстов CTA (вопрос Q5 владельцу, DSM-7)**: мокап v2.0 (новый фрейм DSM-4) — «Начать Training · 80 сек» / «Сразу Practice · 30 сек»; приложение — «Тренировка · 3 попытки» / «Практика · 30 сек» (тексты эпохи DC-2…DC-5, на них завязаны Maestro-флоу). Также структура: мокап — первая карточка вопроса укрупнённая + список остальных; приложение — однородные карточки. Решение (мокап править или приложение) — за владельцем; самостоятельно не меняем.
- **Фоны экранов** — bg-аудит (`e2e-cmp/shoot-bg-audit.js`, pixel-сэмплы light+dark, 11 экранов × 2 темы): везде фирменный фон `#EEF3FF`/`#161A2E`; «дыр» нет. Замечание «нет заполненности» относилось к протухшему prod-бандлу (2026-08-03, до M3) — пересобран 2026-08-08.
