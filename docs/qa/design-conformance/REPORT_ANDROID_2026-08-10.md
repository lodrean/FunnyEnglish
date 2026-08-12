# DC-A1: Дизайн-аудит Android — приложение vs демо-макеты

**Дата:** 2026-08-10 · **Источник истины:** `.docs/design-system/mockups.html` v2.1 (15 фреймов) · **Метод:** скриншоты живого Android-приложения (эмулятор Medium_Phone 1080×2400@420, Maestro-flows `.maestro/flows/design-audit/`) vs phone-рендеры мокапов (360×800, `e2e-cmp/test-results/pixel-report/mockups-light-phone/`, свежие — перегенерированы перед прогоном) · **Инструмент:** `e2e-cmp/compare-android-mockups.py` (обрезка системных баров 66/132px, масштаб по ширине, diff-overlay, порог пикселя >16).

> Первый аудит именно **Android**-сборки; предыдущие (DC-1 и далее) делались на WASM. Pixel-% — индикатор, а не вердикт: базовый шум ~6–14% дают фейковый статус-бар мокапа, рендер шрифтов (Nunito: браузер vs Skia) и живые данные seed-БД вместо выдуманного контента мокапа. Вердикты — по визуальному ревью diff-overlay (`diffs/`).

## Сводная таблица

| Экран | Мокап | Приложение | Diff | Вердикт |
|---|---|---|---|---|
| Onboarding 1–3 | `frame-onboarding.png` (1 фрейм) | `onboarding-1/2/3.png` | 13.7–14.0% | ⚠️ см. O1–O2 |
| Login | `frame-login.png` | `login.png` | 17.2% | ✅ соответствует (мокап — состояние с ошибкой валидации, app — пустая форма) |
| Register | `frame-register.png` | `register.png` | 15.9% | ✅ соответствует (мокап — заполненная форма) |
| Library guest/auth | `frame-library.png` | `library-guest.png`, `library-auth.png` | 14.6% | ✅ соответствует (L1) |
| Topics guest/auth | `frame-topics.png` | `topics-guest.png`, `topics-auth.png` | 6.3% | ✅ соответствует |
| Video | `frame-video.png` | `video-auth.png` | 43.8% | ⚠️ см. V1–V3 |
| Questions guest (гейт) | `frame-locked.png` | `questions-guest-locked.png` | 20.7% | ⚠️ композиция (QG1) |
| Questions auth | `frame-questions.png` | `questions-auth.png` | 22.5% | ❌ QA1–QA2 |
| Training idle | `frame-training.png` | `training-idle.png` | 21.9% | ✅ соответствует (контент 3 vs 5 вопросов — seed) |
| Practice ready | `frame-practice.png` | `practice-ready.png` | 30.1% | ⚠️ PR1–PR2 |
| MySubmissions | `frame-submissions.png` | `submissions.png` | 7.6% | ❌ MS1–MS3 |
| Profile guest | `frame-profile-guest.png` | `profile-guest.png` | 14.7% | ✅ соответствует (стаб в карточке — app-only, ок) |
| Profile auth | `frame-profile.png` | `profile-auth.png` | 8.7% | ✅ соответствует |
| Debug menu | `frame-debug.png` | `debug.png` | 7.7% | ⚠️ наполнение разошлось (D1, dev-only) |
| Grading | `frame-grading.png` | — | — | ➖ экран admin-web, вне скоупа Android-аудита |

## Реестр расхождений

### Существенные (требуют решения владельца / правки)

| # | Экран | Мокап | Приложение | Комментарий |
|---|---|---|---|---|
| QA1 | Questions auth | «Практика · 30 сек» — outlined secondary (белая, синий текст) | Filled **оранжевая** кнопка с иконкой микрофона | Возможно, намеренное app-only усиление CTA — зафиксировать решение |
| QA2 | Questions auth | Под кнопками пояснение: «Training — три попытки, записи только на устройстве. Practice — одна запись, сразу учителю» | Текста нет | Пояснение разницы Training/Practice потеряно |
| MS1 | MySubmissions | Заголовок «Отправки» + подзаголовок «Записи, отправленные учителю» | «← Мои записи» (стрелка назад, другой заголовок, без подзаголовка) | Экран в bottom nav — стрелка назад избыточна |
| MS2 | MySubmissions | Под списком explainer: «Повторная отправка по топику запрещена — после REVIEWED топик можно только переиграть в Training» | Текста нет | Важное правило DUPLICATE_SUBMISSION не объяснено пользователю |
| MS3 | MySubmissions | Бейдж статуса «NEW» (оранжевый чип); карточка 2-строчная (библиотека · дата · длительность), grade-chip для REVIEWED | Бейдж «На проверке»; карточка 1-строчная «0:04 · 2026-08-10» | Терминология и плотность карточки разошлись |
| V1 | Video | Текст реплики — белая карточка под плеером | Просто текст на фоне, без карточки | DC-5 V3 помечен «проверить на Android» — проверено: карточки нет |
| V2 | Video | CTA «Перейти к вопросам» сразу после карточки субтитров | CTA прижата к низу экрана | Компоновка |

### Допустимые / app-only (зафиксированы, действий не требуют)

| # | Экран | Расхождение | Вердикт |
|---|---|---|---|
| L1 | Library | Доп. «›» (chevron) на карточках тем; в dev-БД тестовая библиотека «E2E Grading Library 1786…» (грабля №54) засоряет список | ok (app); тест-данные — чистить перед будущими аудитами |
| O1 | Onboarding | Мокап — 1 фрейм («Тренируйся вслух»), app — 3 слайда; слайды 1/3 сравниваются с чужим фреймом | ok (методика) |
| O2 | Onboarding | Полупрозрачный «SoToSpeak»-логотип под иллюстрацией выглядит washed-out | уточнить: задуманный водяной знак или артефакт (низкий приоритет) |
| PR1 | Practice | App-only оранжевый info-баннер «В отличие от Training, эта запись уйдёт учителю автоматически…» | ok (app) — полезное пояснение, но частично дублирует QA2 |
| PR2 | Practice | Record-squircle с иконкой микрофона; в мокапе пустой | ok (app), как в DC-4 |
| QG1 | Questions guest | Мокап — отдельный экран Practice-locked; app — гейт встроен в Questions под списком вопросов | ok (app, спека §10.2), содержимое гейта совпадает |
| D1 | Debug | Наполнение разошлось: app добавил URL-override, «Проверить соединение», «Отправить логи»; в мокапе — «Сбросить onboarding/записи/всё» | ok (dev-only), мокап устарел |
| V3 | Video | Плеер: мокап — светлый плейсхолдер «ВИДЕО 16:9»; app — чёрный кадр реального видео 0:10 (seed) | ok (контент) |

## Погрешности метода

- Pixel-% включает шум: фейковый статус-бар мокапа (в app обрезан), растеризация Nunito (Chromium vs Skia), вертикальный сдвиг из-за разной высоты контента → дифф «протекает» на всю область ниже сдвига.
- Auth-скриншот submissions: запись создана через API (`POST /api/speaking/submissions`, fixture `practice.m4a`) — на эмуляторе MediaRecorder падает («stop while neither recording nor paused», нет микрофона хоста), живую запись в flow не снять. Скриншот `practice-sent` не получен (не входит в пары сравнения).
- Тёмная тема не аудировалась (рендеры `mockups-dark-phone/` готовы — отдельный прогон).

## Артефакты

- Скриншоты: `docs/qa/design-conformance/android-2026-08-10/*.png` (19 шт.)
- Diff-overlays + `summary.json`: `docs/qa/design-conformance/android-2026-08-10/diffs/`
- Flows: `.maestro/flows/design-audit/audit_guest.yaml`, `audit_auth.yaml`, `audit_auth_finish.yaml`
- Скрипт: `e2e-cmp/compare-android-mockups.py`
- Сборка: `./gradlew :app:assembleDebug -PSOTOSPEAK_API_BASE_URL=http://192.168.1.148:8080/` (LAN-IP — грабли №13/85)
