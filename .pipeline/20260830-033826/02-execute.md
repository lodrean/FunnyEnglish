# 02-execute — bd FunnyEnglish-5tf.5: разбор SoToSpeakApi + RecordingSessionController

## Что сделано

Умеренный разбор монолита `SoToSpeakApi` (321 строка, без полного ADR-006) по предложению 1
(§2.2 К4) из `docs/research/PROJECT-REVIEW-2026-08-28.md`:

1. **Интерфейсы API в `shared/api/`** (новые файлы):
   - `AuthApi` — register/login/oauth/resendVerification + getCurrentUser/getUserProfile;
   - `SpeakingApi` — speaking-контент (libraries/topics/detail), submitSpeakingPractice,
     getMySpeakingSubmissions, getTextResource;
   - `MessagingApi` — inbox сообщений + учебные группы (join/leave/detail);
   - `GuestApi` — mergeGuestProgress, submitGuestEvents, sendLogs.
   `SoToSpeakApi` реализует все четыре интерфейса (`override` на 22 публичных метода);
   транспорт (safeCall + single-flight refresh по 401) не тронут. Дефолт `fileName`
   у `submitSpeakingPractice` вынесен в интерфейс (override не может переопределять default).

2. **`SpeakingRepository`** (`composeApp/.../app/data/SpeakingRepository.kt`, новый) —
   единая точка доступа speaking-VM к сети (`SpeakingApi`) и локальным метаданным записей
   (`RecordingStore`). Переведены VM: Library, Topics, Questions, Video, Training, Practice,
   MySubmissions — прямых сетевых вызовов из VM больше нет.

3. **`RecordingSessionController`** (`composeApp/.../app/recorder/RecordingSessionController.kt`,
   новый) — общая механика Training/Practice: обратный отсчёт лимита (`startTimer/stopTimer`)
   и длительность записи (`markRecordingStarted/elapsedMs`). Удалены скопированные
   `startTimer/stopTimer/timerJob` из обеих VM; контроллер создаётся внутри VM на
   `viewModelScope` (в Koin не регистрируется, экран про него не знает).

4. **DI (`AppModule.kt`)**: `single<AuthApi/SpeakingApi/MessagingApi/GuestApi> { get<SoToSpeakApi>() }`,
   `single { SpeakingRepository(get(), get()) }`; фабрики VM обновлены под новые конструкторы;
   `LogUploader` и `GuestAnalytics` переведены на `GuestApi`.

Поведение не менялось: guards (`uploadInFlight`, `inFlightUploads`), B1/B2-фиксы сброса
состояния, гейтинг Practice, offline-retry, DUPLICATE_SUBMISSION — всё сохранено 1:1.

## Изменённые/созданные файлы

Созданные:
- `shared/src/commonMain/kotlin/com/sotospeak/shared/api/AuthApi.kt`
- `shared/src/commonMain/kotlin/com/sotospeak/shared/api/SpeakingApi.kt`
- `shared/src/commonMain/kotlin/com/sotospeak/shared/api/MessagingApi.kt`
- `shared/src/commonMain/kotlin/com/sotospeak/shared/api/GuestApi.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/data/SpeakingRepository.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/recorder/RecordingSessionController.kt`

Изменённые:
- `shared/src/commonMain/kotlin/com/sotospeak/shared/api/SoToSpeakApi.kt` (implements + override)
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/di/AppModule.kt`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/viewmodel/` — Auth, Profile, Messages,
  Groups, Library, Topics, Questions, Video, Training, Practice, MySubmissions (11 файлов)
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/util/GuestAnalytics.kt`
- `memory.md` — запись в «Решения и договорённости» (2026-08-30)

## Как проверить (гейты драйвера)

```bash
./gradlew :composeApp:desktopTest
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache
```

Ожидаемо зелёные: UI-тесты не конструируют VM напрямую (экраны на mock-state),
`SoToSpeakApiTokenRefreshTest` работает с конкретным классом (сигнатура конструктора
и safeCall не менялись).

## Замечания

- `PracticeViewModel.kt` — единственный файл с CRLF в `viewmodel/` (git index i/crlf);
  при правках сохранять CRLF, иначе diff «весь файл» (зафиксировано в memory.md).
- Follow-up'ы вне скоупа задачи: маппинг ошибок в UiText (предложение 5 обзора),
  хранение refreshToken на клиенте (bd nj2.7 follow-up, ADR-007).
