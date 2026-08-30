# 02-execute — bd FunnyEnglish-xic (MySubmissions ↔ frame-submissions, MS1–MS3)

## Что сделано

Экран `MySubmissionsScreen` приведён к мокапу `frame-submissions` (`.docs/design-system/mockups.html`):

- **MS1 (заголовок)**: убран `TopAppBar` «← Мои записи» со стрелкой назад (экран в bottom nav — стрелка избыточна; параметр `onBack` удалён из сигнатуры). Добавлен заголовок «Отправки» + подзаголовок «Записи, отправленные учителю» (теги `submissions_title` / `submissions_subtitle`), показывается во всех состояниях (loading/error/empty/list) — паттерн как в LibraryScreen.
- **MS3 (бейдж + карточка)**: статус-чип теперь `NEW` / `REVIEWED` (терминология мокапа; цвета `statusNew`/`statusReviewed` + containers из SpeakingColors). Для REVIEWED рядом добавлен **grade-chip** мокапа (`.grade-chip`: `secondaryContainer`/`onSecondaryContainer`, pill `SpeakingShapes.StatusPill`, extrabold) с итоговым баллом. Карточка 2-строчная: тема + «dd.mm.yyyy, hh:mm · m:ss» (хелпер `formatSubmissionDate`, KMP-безопасный, без String.format). Сохранены play/stop-кнопка, развёрнутая рубрика `GradeCard` (4 критерия — app-only детализация, завязана desktopTest) и все testTag'и.
- **MS2 (explainer)**: в конец списка добавлен текст мокапа «Повторная отправка по топику запрещена — после REVIEWED топик можно только переиграть в Training» (тег `submissions_explainer`).
- Гостевая заглушка в App.kt переименована «Отправки доступны после входа» (старого имени экрана больше нет; зависимостей в тестах/флоу не найдено).

## Изменённые файлы

1. `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/MySubmissionsScreen.kt` — основная переработка (MS1–MS3).
2. `composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt` — убрана передача `onBack`; заголовок LockedFeature гостя → «Отправки доступны после входа». VM-экшен `MySubmissionsAction.OnBack`/событие `NavigateBack` намеренно оставлены (не используются UI, минимальное вмешательство).
3. `composeApp/src/androidMain/kotlin/com/sotospeak/app/preview/AppPreviews.kt` — `onBack` убран из 2 превью MySubmissions.
4. `composeApp/src/androidInstrumentedTest/kotlin/com/sotospeak/app/screenshot/ScreenshotTest.kt` — `onBack` убран из `mySubmissions()`.
5. `composeApp/src/commonTest/kotlin/com/sotospeak/app/tests/MySubmissionsScreenTest.kt` — ожидания статусов «На проверке»/«Проверено» → `NEW`/`REVIEWED`; проверка total «7.5» сужена внутрь `grade_card_sub-2` (значение теперь дублируется grade-chip'ом); fixture без `onBack`; KDoc.
6. `.maestro/flows/design-audit/audit_auth.yaml` — ожидание `visible: "На проверке"` → `"NEW"`.
7. `.maestro/flows/design-audit/audit_auth_finish.yaml` — то же.
8. `.maestro/flows/speaking_practice_auth.yaml` — то же.

## Как проверить (гейты драйвера, сам не запускал)

- `./gradlew :composeApp:desktopTest` — MySubmissionsScreenTest (5 тестов: статусы NEW/REVIEWED, рубрика, pending-retry, empty).
- `./gradlew :composeApp:compileDebugKotlinAndroid` — AppPreviews + ScreenshotTest-подпись.
- `./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache` — commonMain экран (formatSubmissionDate без String.format).
- Maestro (по желанию владельца): 3 флоу обновлены на `visible: "NEW"` синхронно с UI.

## ADR-007 (для владельца, НЕ блокирует)

Спека `docs/SPEAKING_TRAINER_SPEC_PART2.md` описывает старые русские подписи чипов: стр. 795 «статус-чип `NEW` («На проверке») / `REVIEWED` («Проверено»)» и стр. 938 (§10, маестро-флоу «статус "На проверке"»). Мокап (источник по задаче и аудиту DC-A1) требует «NEW»/«REVIEWED» — код приведён к мокапу, спека НЕ тронута. Рекомендуется patch-bump спеки Part 2 владельцем (стр. 795, 938) для устранения дрейфа.
