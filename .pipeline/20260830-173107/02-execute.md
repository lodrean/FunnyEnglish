# 02-execute — bd FunnyEnglish-2oz.9 «DS: аудит тёмной темы Android»

## Что сделано

Выполнен пиксельный аудит тёмной темы Android (D-5 из PROJECT_AUDIT_2026-08-29) одним прогоном по готовой инфраструктуре:

1. **Среда**: эмулятор Medium_Phone 1080×2400@420, `cmd uimode night yes` (приложение в режиме SYSTEM → dark), APK `app-debug.apk` (develop 2026-08-27, base URL 10.0.2.2 → dev docker-стек). Gradle-сборки/тесты не запускались.
2. **Прогон Maestro**: `audit_guest.yaml` (10/10 скриншотов, EXIT=0), `audit_auth.yaml` (6 скриншотов до practice-ready; ожидаемый стоп на живой записи), `audit_auth_finish.yaml` (submissions/profile/debug; потребовалась починка флоу — см. ниже). Итого 18 экранов в `docs/qa/design-conformance/android-dark-2026-08-30/`.
3. **Найдено и устранено протухание базы сравнения**: готовые dark-фреймы (Aug 10) рендерили кнопки pre-v1.3.1 (#3B6FD4+белый текст), что противоречит утверждённым токенам v1.3.1 (dark filled = #8FB3F5/#1A2F5E). Фреймы перегенерированы `node e2e-cmp/shoot-mockups-phone.js`; light-фреймы восстановлены из бэкапа (light-база 2026-08-10 не тронута).
4. **Pixel-diff**: создан `e2e-cmp/compare-android-mockups-dark.py` (клон light-скрипта, источники — mockups-dark-phone + android-dark-2026-08-30). 18 diff-overlay + summary.json. Все overlay'и ревьюнуты визуально.
5. **Отчёт**: `docs/qa/design-conformance/REPORT_ANDROID_DARK_2026-08-30.md`.

## Выводы аудита

- **Тёмная тема корректна**: фон всех экранов #161A2E (пиксельный замер = токен), surface/текст/чипы/навигация по токенам v1.3.1; светлых «протечек», нечитаемого контраста и hardcoded-светлых цветов не найдено. Новых dark-специфичных дефектов нет; все расхождения с мокапами — известные из light-реестра (QA1/QA2, MS1–MS3, QG1, O1, PR1/PR2, D1).
- **V-D1 (НЕ dark-дефект)**: экран Video на эмуляторе — весь фон #000000 вместо токена и нет кадров при играющем плеере; воспроизводится и в LIGHT-теме → theme-independent (media3 SurfaceView на swiftshader). Требует проверки живого плеера на физ. устройстве (на железе 2026-08-10 видео было в error-state). Зафиксировано в отчёте и memory.md №106(е).
- **Инфра-находки** (в отчёте F1–F5): протухание phone-рендеров при обновлении tokens.css; `audit_auth_finish.yaml` ждал «NEW» вместо «На проверке» (починено); debug-меню 7 тапов на эмуляторе — adb-тапами; грабля №97 на Medium_Phone не воспроизвелась (запись дошла до авто-отправки).

## Изменённые/созданные файлы

- `docs/qa/design-conformance/REPORT_ANDROID_DARK_2026-08-30.md` — новый отчёт
- `docs/qa/design-conformance/android-dark-2026-08-30/*.png` (19) + `diffs/` (18 overlay + summary.json) — новые артефакты
- `e2e-cmp/compare-android-mockups-dark.py` — новый скрипт сравнения (dark)
- `e2e-cmp/test-results/pixel-report/mockups-dark-phone/*.png` — перегенерированы (актуальные токены v1.3.1); light-фреймы восстановлены без изменений
- `.maestro/flows/design-audit/audit_auth_finish.yaml` — ожидание бейджа «На проверке» вместо «NEW»
- `memory.md` — грабля №106 (инфра dark-аудита) + команды dark-прогона в §3
- `.pipeline/20260830-173107/tmp/` — maestro-логи и доказательства (video-playing, video-light)

Продуктовый код (composeApp/shared/backend) **не менялся** — задача аудитная. Спеки/PRD не тронуты. После прогона `practice_submissions`/`grades` в dev-БД очищены (неидемпотентность audit_auth). Эмулятор остановлен.

## Как проверить

1. Открыть `docs/qa/design-conformance/REPORT_ANDROID_DARK_2026-08-30.md` — сводная таблица dark vs light + реестр F1–F5/V-D1.
2. Визуально: `docs/qa/design-conformance/android-dark-2026-08-30/diffs/diff-*.png` (mockup | app | diff).
3. Повтор прогона: эмулятор Medium_Phone → `adb shell cmd uimode night yes` → install debug APK → `maestro test .maestro/flows/design-audit/audit_guest.yaml` (+ audit_auth, audit_auth_finish) → скопировать скриншоты в `android-dark-2026-08-30/` → `python e2e-cmp/compare-android-mockups-dark.py`.
