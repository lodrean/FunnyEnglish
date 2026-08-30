# 02-execute — bd FunnyEnglish-2oz.10 «DS: единый EmptyState + скелетоны списков»

## Что сделано

1. **Единый `EmptyState`** (иконка + заголовок + опциональные подпись и CTA), по образцу `ErrorMessage`:
   - Добавлен в `app/components/Common.kt`: `EmptyState(icon, title, subtitle?, ctaLabel?, onCtaClick?, modifier)`.
   - Отступы/размеры — только токены ДС (`SpaceXl/SpaceMd/SpaceSm`, `IconSizeXLarge`), без magic numbers (detekt maxIssues: 0).
   - Иконка — `ImageVector`; вызывающие передают кастомные `SpeakingIcons.*` (material-иконки не рендерятся в WASM-canvas, грабля №75).
2. **Скелетоны списков** вместо `CircularProgressIndicator` на первой загрузке:
   - В `designsystem/animations/LoadingSkeleton.kt` добавлен `ListSkeleton(itemCount = 6)` — повторяющиеся `SkeletonListItem` (уже существовавшие, shimmer + reduce-motion aware).
   - Подключён на экранах Library / Topics / MySubmissions (ветка `isLoading && list.isEmpty`).
3. **Empty-состояния переведены на `EmptyState`**:
   - Library: иконка `Mic`, «Пока нет доступных тем» + подпись + CTA «Обновить» (`onLoad`); тег `library_empty` сохранён; приватный `LibraryEmptyState` удалён.
   - Topics: добавлен отсутствовавший empty-state — иконка `Play`, «В этой теме пока нет топиков» + CTA «Обновить» (`onRetry`), тег `topics_empty`.
   - MySubmissions: иконка `Upload`, «У вас пока нет отправленных записей» + подпись + CTA «Обновить» (`onRefresh`); тег `submissions_empty` сохранён; приватный `SubmissionsEmptyState` удалён.

Тексты и тестовые теги, на которые опираются существующие UI-тесты (`LibraryScreenTest`, `MySubmissionsScreenTest`), не изменены. `LoadingIndicator` оставлен для остальных экранов (Practice/Video/Training/Profile/Messages/Questions) — вне скоупа задачи. Публичные сигнатуры экранов не менялись (превью `AppPreviews.kt` не затронуты).

## Изменённые файлы

- `composeApp/src/commonMain/kotlin/com/sotospeak/app/components/Common.kt` — +`EmptyState`
- `composeApp/src/commonMain/kotlin/com/sotospeak/designsystem/animations/LoadingSkeleton.kt` — +`ListSkeleton`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/LibraryScreen.kt` — скелетон + EmptyState
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/TopicsScreen.kt` — скелетон + EmptyState (новый)
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/screens/MySubmissionsScreen.kt` — скелетон + EmptyState

## Как проверить

- `./gradlew :composeApp:desktopTest` — UI-тесты Library/MySubmissions (empty-state по тегам и текстам).
- `./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinWasmJs --no-configuration-cache` — компиляция.
- `./gradlew :composeApp:detekt` — новых findings быть не должно (magic numbers не добавлялись, использованы токены).
- Вручную (desktop): открыть «Библиотека тем»/«Топики»/«Отправки» — при загрузке shimmer-скелетоны; при пустом списке — иконка + текст + кнопка «Обновить».

Сборки/тесты сам не запускал (гейты прогоняет драйвер).
