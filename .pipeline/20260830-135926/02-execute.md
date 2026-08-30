# 02-execute — bd FunnyEnglish-5tf.7: KMP RecordingStore производительность + чистка файлов

## Что сделано

### 1. Производительность (O(библиотеки × топики × размер JSON) устранено)
- **`RecordingStore`** — распарсенный список метаданных кэшируется в памяти: JSON из Settings
  читается и парсится один раз за жизнь процесса; `add/remove/markUploaded/...` обновляют кэш
  и делают одну запись в Settings (раньше каждый `list()` парсил JSON заново).
  Все вызовы — с главного потока (VM на viewModelScope, экраны), синхронизация не нужна (задокументировано).
- Новый bulk-запрос `RecordingStore.recordedTopicIds(kind): Set<String>` — один проход по кэшу.
- **`SpeakingRepository.trainingTopicIds()`** — проброс bulk-запроса.
- **`LibraryViewModel.loadProgress`** — один снапшот `trainingTopicIds()` на весь проход вместо
  `listRecordings(topic.id)` на каждый топик (сетевые вызовы топиков по библиотекам не менялись).
- `TopicsViewModel`/`TrainingViewModel`/`MySubmissionsViewModel` не тронуты — кэш store
  сам убирает повторные парсинги.

### 2. Чистка файлов Training-записей
- **`RecordingStore.prune(nowEpochMs)`** — вызывается один раз при старте приложения
  (`App.kt`, `AppThemedContent`, существующий `LaunchedEffect(Unit)`):
  1. метаданные, чей файл уже не существует → мета удаляется;
  2. TRAINING-записи старше **TTL 30 дней** (`TRAINING_TTL_MS`) → мета + файл удаляются.
  - Pending PRACTICE TTL не касается (offline-retry, спека §6.4).
  - Удаляются только файлы из собственных метаданных store (чужие файлы директории не трогаются
    — иначе тесты с отдельным Settings повредили бы реальные записи desktop-приложения).
  - Ошибки ФС/платформенные стабы (ios/wasm) не роняют старт — try/catch внутри.
- **Принятое следствие (зафиксировано в memory.md):** бейдж «N пройдено» (DC-2) и попытки
  Training по топику обнуляются после 30 дней неактивности. Если владелец хочет другую
  retention-политику — это правка спеки Part 2 §5.1 (ADR-007); спека сейчас retention
  не регламентирует, поэтому спека не менялась.

### 3. Тесты
- `commonTest/RecordingStoreTest.kt` (+3 кейса): `recordedTopicIds` по kind,
  prune удаляет мету с несуществующим файлом, prune удаляет протухшую TRAINING-мету.
- `desktopTest/.../RecordingStorePruneFileTest.kt` (новый, java.io.File): свежая TRAINING с
  файлом сохраняется; протухшая TRAINING → мета+файл удалены; старая pending PRACTICE с
  файлом сохраняется. Созданные файлы чистятся в `@AfterTest`.

## Изменённые/созданные файлы
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/storage/RecordingStore.kt` — кэш + `recordedTopicIds` + `prune` + `TRAINING_TTL_MS`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/data/SpeakingRepository.kt` — `trainingTopicIds()`
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/viewmodel/LibraryViewModel.kt` — loadProgress на снапшоте
- `composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt` — вызов `recordingStore.prune()` при старте
- `composeApp/src/commonTest/kotlin/com/sotospeak/app/tests/RecordingStoreTest.kt` — +3 теста
- `composeApp/src/desktopTest/kotlin/com/sotospeak/app/tests/RecordingStorePruneFileTest.kt` — новый
- `memory.md` — запись решения в разделе 5

## Как проверить (гейты драйвера)
```bash
./gradlew :composeApp:desktopTest --tests "*RecordingStore*"
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinWasmJs --no-configuration-cache
```
Ожидание: существующие 6 кейсов RecordingStoreTest + 3 новых commonTest + 3 desktopTest — зелёные;
компиляция android/wasm без ошибок (String.format/ java.io в commonMain не использовались;
java.io.File — только в desktopTest).

## Замечания
- Спеки/PRD не менялись. Retention-политика (TTL 30 дней для TRAINING) — реализационное решение
  в рамках задачи; при несогласии владельца требуется правка спеки Part 2 §5.1 (ADR-007).
- Gradle-сборки/тесты сам не запускал (гейты драйвера), git-коммитов не делал.
