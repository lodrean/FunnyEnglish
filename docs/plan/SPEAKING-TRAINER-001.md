# План реализации: Speaking-тренажёр (SPEAKING-TRAINER-001)

> Дата: 2026-07-30. Основание: `docs/prd/SPEAKING-TRAINER-001.prd.md` (утверждён владельцем в части ключевых решений), план пивота `~/.kimi/plans/bobbi-morse-moon-girl-sunspot.md`.

## Исходные решения
- Полный пивот: legacy-фичи (тесты, словарь, геймификация, уроки) убираем из навигации продукта; код не удаляем до стабилизации нового ядра.
- Android-first; iOS/Desktop/WASM — стабы.
- Видео/аудио: S3-совместимое хранилище. Dev — MinIO (docker), prod — российский S3 + CDN (Yandex Cloud / VK Cloud / Selectel), смена через env (`docs/research/DIAGRAM_AND_VIDEO_SERVICES.md`).
- Оценивание: рубрика grammar/vocabulary/pronunciation/fluency (1–10) + общий балл + комментарий.
- Practice — только авторизованным; Training и просмотр — гостю.
- Дизайн-система: **Playful Coach v1.1 (вариант B) — выбрана владельцем 2026-07-31** (`.docs/design-system/`: tokens.json/css, styleguide, 5 мокапов, icons.svg). Токены → `:design` и MUI-тему (`docs/DESIGN_BRIEF_SPEAKING_TRAINER.md`).

## Документы-основания
| Документ | Статус |
|---|---|
| `docs/prd/SPEAKING-TRAINER-001.prd.md` | ✅ готов |
| `docs/DESIGN_BRIEF_SPEAKING_TRAINER.md` | ✅ готов |
| `docs/research/DIAGRAM_AND_VIDEO_SERVICES.md` (+ Mermaid-схемы) | ✅ готов |
| `docs/SPEAKING_TRAINER_SPEC_PART1.md` (backend) | ✅ готов (1173 строки) |
| `docs/SPEAKING_TRAINER_SPEC_PART2.md` (клиент) | ✅ готов (949 строк) |
| `docs/SPEAKING_TRAINER_SPEC_PART3.md` (admin-web) | ✅ готов (646 строк) |

## Фазы реализации

> **Quality gate после КАЖДОЙ фазы (обязательно, см. раздел «Проверка и ревью» ниже):**
> фаза считается завершённой только после (1) прогона тестов фазы по реальному exit code,
> (2) регрессии смежных сьютов, (3) ревью кода и контрактов со спекой владельцем/пользователем,
> (4) закрытия bd-задач фазы и обновления `memory.md`. Без gate к следующей фазе не переходим.

### Фаза 0 — Документация (текущая)
PRD → спеки Part 1–3 → дизайн-бриф → схемы. Выход: документы выше + задачи в bd.

### Фаза 1 — Backend (спека Part 1) — ✅ ВЫПОЛНЕНА 2026-07-31
> Реализовано BE-1…BE-13 (bd `8tg.1.1`–`8tg.1.13` закрыты). Backend-тесты 55 (0 падений), Newman chain 16/16 против живого стека. OpenSpec change `add-speaking-backend` заархивирован.
1. Flyway-миграции: libraries, topics, videos, speaking_questions, practice_submissions, grades.
2. Entities + DTO + репозитории.
3. Публичные эндпоинты контента (guest-readable).
4. Practice: multipart upload → MinIO, статусная модель NEW/REVIEWED.
5. Admin CRUD контента + grading endpoints.
6. Тесты: unit + integration.

### Фаза 2 — Клиент Android (спека Part 2) — ✅ ВЫПОЛНЕНА 2026-07-31
> Реализовано CL-T1…CL-T12 (bd `8tg.2.1`–`8tg.2.12` закрыты). Спека Part 2 → v1.3 (синхронизация с контрактом Part 1, согласовано владельцем). DS-1: токены Playful Coach v1.1 в `:design` + `designsystem/theme/SpeakingTokens.kt`. Quality gate: desktopTest 139/139 (42 новых), uiTest починен, `:shared:allTests` зелёный, assembleDebug + wasmJs компиляция, Maestro 4/4 против docker-стека (включая реальную запись и upload в MinIO), seed-контент «Разговорный английский»/«Знакомство». Найдено и исправлено 2 бага платформы: Ktor Auth null-cache (403 после логина в сессии), залипший экран логина. Legacy Maestro-флоу → `.maestro/legacy/`.
1. Модели + shared/api методы.
2. Навигация: новые AppScreen; стартовый экран → Library.
3. Library / Topics / Questions экраны (MVI).
4. VideoPlayer (ExoPlayer) + WebVTT-субтитры под плеером + mode-chips «С субтитрами/Без субтитров» + CC; CTA «К вопросам» доступен всегда.
5. VoiceRecorder (MediaRecorder + permission) — expect/actual.
6. Training: 3 попытки на топик (попытка N = лимит 80→50→30), каждая попытка — одна запись на ВСЕ вопросы (список вопросов виден на экране), без удаления — только прослушивание, авто-✅, финальные CTA (практика/библиотека/заново).
7. Practice: 30с, одна запись на все вопросы, **без Review** — автостоп/ручной стоп → авто-upload → sent, retry при ошибке, гейтинг гостя.
8. MySubmissions: статусы и оценки.
9. UI-тесты (desktopTest + testTags), Maestro-флоу.

**Проверка и ревью (gate Фазы 2):** `./gradlew :composeApp:desktopTest :composeApp:uiTest` + `:app:assembleDebug` (exit code), Maestro-флоу speaking на эмуляторе против docker-стека, ручной смоук Training/Practice на устройстве, ревью экранов на соответствие мокапам Playful Coach v1.1 и спеке Part 2 (владелец), сверка API-контрактов с Part 1 (только реализованные эндпоинты). Результат фиксируется в этом плане + `memory.md`.

### Фаза 3 — Admin-web (спека Part 3) — ✅ ВЫПОЛНЕНА 2026-08-01
> Реализовано AW-T1…AW-T17 (bd `8tg.3.1`–`8tg.3.16` закрыты). Quality gate: `npm run lint` 0 errors/warnings (в eslint.config добавлены ignores coverage/storybook-static), vitest 256/256 (38 новых по §7.1), `npm run build` + `npm run build-storybook` зелёные, полный Playwright **328/328** против docker-стека (~21 мин, последовательно), живые прогоны: создание контента с реальным upload mp4/vtt в MinIO, grading-флоу NEW→оценка 7.5→REVIEWED→edit 8.5. Storybook-preview всех экранов (требование владельца): stories на QueryClient `setQueryData` + MemoryRouter, без msw. Контрактная адаптация спека Part 3 ↔ фактический backend сосредоточена в `speakingApi.ts` (publish через PUT, маппинг полей, детали из кэша списков, reorder цепочкой PUT, вопросы из AdminTopicResponse) — владельцу предложен дифф спеки → v1.1 (ADR-007). Найдено и исправлено 2 реальных бага платформы: ToastProvider props (белый экран через 5с после любого тоста), MUI Tabs direct-child; visual-базлайны обновлены (новые пункты меню в сайдбаре).
1. Раздел Speaking: CRUD Libraries/Topics/Videos/Questions (MediaUploader).
2. Раздел Grading: inbox, плеер, рубрика, REVIEWED.
3. Vitest + Playwright e2e.

**Проверка и ревью (gate Фазы 3):** `npm test` (vitest) + `npm run lint` + `npm run build`, Playwright-сьют против docker-стека (`SKIP_WEB_SERVER=1 ADMIN_URL=http://localhost:3000`), ручной смоук CRUD контента и grading-флоу в админке, ревью UI спеки Part 3 (владелец). Результат фиксируется в этом плане + `memory.md`.

### Фаза 4 — Дизайн-система handoff — ✅ ВЫПОЛНЕНА 2026-08-01
> Реализовано DS-1…DS-6 (bd `8tg.4.1`–`8tg.4.6` закрыты). Токены Playful Coach v1.1 в `:design` и `SpeakingTokens.kt` (DS-1, ранее); полный ребренд admin-web `Theme.ts` (HEX 1:1, палитра `speaking`, Nunito, radius 16/22/12, индиго-тени — владелец выбрал полный ребренд, не только палитру); `SpeakingIcons.kt` — 15 ImageVector из icons.svg; motion-токены `SpeakingMotion` + reduce-motion expect/actual; `SpeakingRecording.kt` (RecIndicator с пульсом 1600ms, CheckPopAppear, waveform-панели декоративные — решение владельца, не реальная амплитуда); `SpeakingTimerRing` вместо LinearProgressIndicator (Training 176dp / Practice 150dp). Quality gate: `:design:build`, compile desktop+wasm, desktopTest 73/73, admin build/vitest 237/237/lint/build-storybook, полный Playwright 156/156 против docker-стека (visual-базлайны пересняты под ребренд + удаление Content), WCAG-фикс чипов NEW/REVIEWED (белый на #FB8C00 = 2.37:1 FAIL → container + тёмный текст 9.2/8.7:1 AA). 6 оставшихся расхождений с мокапом зафиксированы в отчёте фазы.
> **Также в рамках фазы (решение владельца 2026-08-01)**: legacy-код старого приложения УДАЛЁН из admin-web (Tests/Categories/TestEditor + компоненты + API + e2e) и composeApp/shared (~70 файлов: экраны/VM/gamification/модели/эндпоинты/тесты) — bd `8tg.6`/`8tg.7` (заменили ST-2 «скрыть из навигации»). Оставлены: Groups/Messages, GuestSession/аналитика, :design gamification, backend legacy-эндпоинты (решение отложено).
Концепт реализован: Playful Coach v1.1 — вариант B (`.docs/design-system/`). Статус на 2026-08-01 — всё выполнено:
1. ~~`tokens.json` → `:design` theme~~ ✅ (DS-1)
2. ~~Палитра → `admin-web/src/theme/Theme.ts`~~ ✅ (DS-2, полный ребренд)
3. ~~`icons.svg` → `SpeakingIcons.kt`~~ ✅ (DS-3)
4. ~~Верификация~~ ✅ (DS-4): сборки зелёные, WCAG AA (чипы на container-фонах), detekt НЕ подключён ни к одному модулю (memory №8) — не является gate; `docs/DESIGN_SYSTEM_SPEC.md` — дифф до v2.0 предложен владельцу (ADR-007).
5. Дополнительно: DS-5 (анимации) и DS-6 (pixel-perfect: таймер-кольцо, waveform, иконки).

**Проверка и ревью (gate Фазы 4):** визуальная сверка компонентов с мокапами Figma-концепта (владелец), проверка контрастов WCAG AA (record `#FF9F6B` ≠ error `#E53935`), сборки зелёные по exit code.

### Фаза 5 — Стабилизация и пивот навигации — ✅ ВЫПОЛНЕНА 2026-08-01
1. Полный прогон тестов (backend, composeApp, admin vitest+e2e, API-коллекция, Maestro).
2. Скрытие legacy-экранов из навигации (Home → Library как старт).
3. Обновить `docs/API.md`, `docs/ARCHITECTURE.md`, `docs/USER_GUIDE.md`, `memory.md`.
4. Prod-конфиг: российский S3+CDN через env, nginx `client_max_body_size` под видео.

**Проверка и ревью (gate Фазы 5):** полная тест-матрица по exit code (грабля №30: статусы не из tail), чек-лист пивота навигации вручную, ревью владельцем всего продукта end-to-end (ученик + учитель), go/no-go решение владельца.

## Проверка и ревью (quality gates)

Принцип: **каждая фаза завершается явным gate — тесты + ревью, а не «код написан»**. Gate Фазы 1 прошёл 2026-07-31 и служит эталоном:

| Шаг gate | Что делаем | Эталон (Фаза 1) |
|---|---|---|
| 1. Тесты фазы | Новые тесты зелёные, статус по реальному exit code (грабля №30) | backend: 55 тестов, 0 падений |
| 2. Регрессия | Смежные сьюты не сломаны | полный `:backend:test`, `:shared:allTests` |
| 3. Живой прогон | Сквозной сценарий против реального стека, не только моки | Newman chain 16/16 с upload в MinIO |
| 4. Ревью контрактов | Код ↔ спека; отклонения — сначала правка спеки (с согласования, ADR-007), потом код | спека Part 1 v1.1 (SMALLINT→INTEGER) согласована |
| 5. Ревью владельца | Демо/скриншоты/описание результата — подтверждение пользователя | утверждено в сессии 2026-07-31 |
| 6. Фиксация | bd-задачи закрыты, `memory.md` дополнен (решения + грабли), план обновлён | `8tg.1.*` closed, грабли №31–34 |

Промежуточные этапы внутри фазы (группы задач в `tasks.md` OpenSpec change): после каждой группы — компиляция + точечные тесты этапа, полный gate — в конце фазы.

## Сверка контрактов спек (2026-07-30)
Проведена кросс-проверка Part 1 ↔ Part 2 ↔ Part 3:
- Эндпоинты совпадают: Part 3 использует пути относительно `baseURL='/api'` — маппинг 1:1 на Part 1.
- Пагинация inbox — Spring `Page` (Part 1 §5.5), Part 3 использует тип `Page<T>`.
- Фильтр ученика в inbox — по `userId` (UUID); для выбора ученика в UI — существующий admin users endpoint (вопрос №5 Part 3 закрыт).
- Список топиков для фильтров — переиспользуется `GET /api/admin/speaking/topics?libraryId=` (вопрос №2 Part 3 закрыт).
- Лимиты файлов: practice-аудио ≤ 5 МБ (валидация в коде); nginx `client_max_body_size` поднять 50m → **200m** под видео (задача Фазы 1/5).
- StorageService требует расширения под mp4/vtt (задача #4 Part 1).
- Открыто: seed-эндпоинт для e2e grading (вопрос №4 Part 3) — решить в Фазе 3 (предложение: создавать submission через публичный practice-flow тестовым пользователем).

## Порядок зависимостей
Фаза 1 → Фазы 2 и 3 (параллельно) → Фаза 5. Фаза 4 — параллельно, токены подключаются по мере готовности.

## Задачи в bd (сформированы 2026-07-31)
- **Фаза 1** (`8tg.1`): BE-1…BE-13 (`8tg.1.1`–`8tg.1.13`) — по Part 1 §9; критический путь BE-1→BE-3→BE-6→BE-8→BE-9→BE-11.
- **Фаза 2** (`8tg.2`): CL-T1…CL-T12 (`8tg.2.1`–`8tg.2.12`) — по Part 2 §11; спайки VoiceRecorder (CL-T6) и VideoPlayer (CL-T4) без зависимостей.
- **Фаза 3** (`8tg.3`): AW-T1…AW-T16 (`8tg.3.1`–`8tg.3.15`, без опционального T12-badge) — по Part 3 §8.
- **Фаза 4** (`8tg.4`): DS-1…DS-4 (`8tg.4.1`–`8tg.4.4`) — handoff токенов/иконок/верификация.
- **Фаза 5** (`8tg.5`): ST-1…ST-4 (`8tg.5.1`–`8tg.5.4`).
- Зависимости фаз проставлены (`blocks`): 2,3 ← 1; 5 ← 2,3. Стартовые ready: BE-1, BE-5, DS-1, DS-3.

## Риски
| Риск | Митигация |
|---|---|
| Запись аудио/права на Android (прерывания, занятый микрофон) | Ранний спайк VoiceRecorder в Фазе 2 до экранов |
| Большие видео через nginx (413) | Увеличить `client_max_body_size`, валидация размера в админке |
| Расхождение контрактов API между спеками | Единый источник — Part 1; кросс-ревью спек перед Фазой 1 |
| Субтитры рассинхрон | WebVTT-файл обязателен при публикации топика «с субтитрами»; валидация в админке |
