# 02-execute — bd FunnyEnglish-5tf.6: KMP: UiText вместо error: String?

## Что сделано

Устранено «три способа выразить ошибку» (сырой `exception.message` в state, fallback-строки в VM,
перевод технических сообщений в компоненте `ErrorMessage` — грабля №15/№55):

1. **Новый `app/error/UiText.kt`** — типизированный sealed `UiText`
   (`Message` / `NoConnection` / `ServerUnavailable` / `SessionExpired` / `Forbidden` / `NotFound` / `Unknown`)
   + `asString()` (строки захардкожены по-русски, как весь UI — локализации нет)
   + единый маппер `Throwable.toUiText()`:
   - `ApiException(0, …)` (сбой до HTTP) → `NoConnection` по паттернам сообщения (host/refused/connect/timeout), иначе `Unknown`;
   - 401 + `INVALID_CREDENTIALS` → `Message(message)` (неверные креды на логине ≠ истёкшая сессия);
   - прочие 401 → `SessionExpired`; 403 → `Forbidden`; 404 → `NotFound`; 5xx → `ServerUnavailable`;
   - прочие 4xx с распарсенным ErrorResponse (errorCode != null) → человеческое `message` backend'а, иначе `Unknown`.
2. **Все 10 VM переведены** на `error: UiText?`; в `onFailure` — `error.toUiText()`;
   локальные строки («Не удалось прочитать файл записи» и т.п.) — `UiText.Message(...)`.
3. **`ErrorMessage` принимает `UiText`** и больше не переводит строки — `userFriendlyError` удалён из `Common.kt`.
4. `MergeProgressDialog` — `error: UiText?`; Login/Register — `state.error.asString()`.
5. Специальные случаи сохранены: `EMAIL_NOT_VERIFIED` перехватывается в AuthViewModel до маппинга,
   `DUPLICATE_SUBMISSION` (409) — в PracticeViewModel до маппинга.
6. Осознанно НЕ тронуты: `SpeakingField.error` (локальная валидация полей) и `VideoPlayerState.error` (плеер, не API).
7. memory.md — запись в «Решения и договорённости»; KDoc `SpeakingRepository` обновлён (follow-up закрыт).

## Изменённые/созданные файлы

**Созданы:**
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/error/UiText.kt`
- `composeApp/src/commonTest/kotlin/com/sotospeak/app/tests/ErrorMappingTest.kt` (9 тестов маппера + asString)

**Изменены (composeApp):**
- `.../app/components/Common.kt` (ErrorMessage → UiText, userFriendlyError удалён)
- `.../app/components/MergeProgressDialog.kt`
- `.../app/data/SpeakingRepository.kt` (только KDoc)
- `.../app/viewmodel/{Auth,Library,Topics,Questions,Video,Training,Practice,MySubmissions,Messages,Profile}ViewModel.kt`
- `.../app/screens/{LoginScreen,RegisterScreen,MySubmissionsScreen}.kt`
- `.../commonTest/.../{LibraryScreenTest,LoginUserFlowTest}.kt` (конструкция state с UiText)
- `memory.md` (запись решения)

`PracticeViewModel.kt` сохранён в CRLF (грабля из 5tf.5 — после правок концы строк восстановлены, diff минимальный).

## Как проверить (гейты драйвера)

- `./gradlew :composeApp:desktopTest` — incl. новый `ErrorMappingTest` и обновлённые Library/Login UI-тесты
- `./gradlew :composeApp:compileDebugKotlinAndroid`
- `./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache`

Пользовательские тексты ошибок не изменились (те же строки, что давал userFriendlyError);
логин по-прежнему показывает сообщение backend'а при неверных кредах.
