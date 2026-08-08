# Speaking-тренажёр — Техническая спецификация (Part 2)
## Клиент: composeApp (Android-first)

**Feature ID:** SPEAKING-TRAINER-001
**Version:** 1.6 (2026-08-08: §3.2 WASM-стаб заменён на реальный HTML5-плеер (DOM `<video>` поверх canvas, control-bar под плеером, `supportsOverlayControls`); §3.4 SubtitlePanel заменена на TranscriptPanel — полный текст видео с пословной подсветкой (karaoke-таймкоды `<mm:ss.mmm>` или интерполяция по длине слова), отдельного транскрипта нет; §2.2–2.6 аппбары по мокапу — без стрелки «назад», с breadcrumb-подзаголовком, системный «назад» через PlatformBackHandler; список топиков по frame-topics («N вопросов · видео m:ss» + чип). Дифф утверждён владельцем (`docs/plan/SPEC_DIFFS_TRANSCRIPT_APPBAR.md`, ADR-007). v1.5 — онбординг-иллюстрации SpeakingIcons; v1.4 — guest-first; v1.3 — §8.2/§8.3 контракт; v1.2 — попытка Training = одна запись; v1.1 — Playful Coach)
**Date:** 2026-08-08
**Estimated Effort:** 8–12 дней
**Связанные документы:**
- PRD: `docs/prd/SPEAKING-TRAINER-001.prd.md`
- Backend-спека (Part 1): `docs/SPEAKING_TRAINER_SPEC_PART1.md` — эндпоинты и DTO берутся оттуда, здесь не переопределяются
- Дизайн-система (авторитетна по флоу Training/Practice/Video): `.docs/design-system/` (tokens.json, mockups.html, icons.svg)
- Конвенции и грабли: `memory.md`

---

## 📑 Содержание

0. [Скоуп и ключевые решения](#0-скоуп-и-ключевые-решения)
1. [Навигация](#1-навигация)
2. [MVI-контракты экранов](#2-mvi-контракты-экранов)
3. [Видеоплеер и субтитры](#3-видеоплеер-и-субтитры)
4. [Запись голоса (VoiceRecorder)](#4-запись-голоса-voicerecorder)
5. [Training-режим](#5-training-режим)
6. [Practice-режим](#6-practice-режим)
7. [Экран MySubmissions](#7-экран-mysubmissions)
8. [DI, shared/api, модели](#8-di-sharedapi-модели)
9. [Обработка ошибок и UX-состояния](#9-обработка-ошибок-и-ux-состояния)
10. [Тестирование](#10-тестирование)
11. [План задач (→ bd issues)](#11-план-задач--bd-issues)

---

## 0. Скоуп и ключевые решения

Part 2 покрывает **только клиент `composeApp`** (монолит, ADR-006). Backend, admin-web и миграции — в Part 1.

Ключевые решения (зафиксированы здесь, при реализации перенести в `memory.md`):

| # | Решение | Причина |
|---|---------|---------|
| R1 | Код — в монолите `composeApp`: экраны `app/screens/`, VM `app/viewmodel/`, DI `app/di/AppModule.kt` | ADR-006: feature-модули не подключены к приложению |
| R2 | Платформенные возможности (видео, запись, файлы) — expect/actual в `composeApp` (НЕ в `shared`): `app/player/`, `app/recorder/`, `app/storage/` | `shared` не зависит от Compose; UI-поверхность плеера нужна в composeApp. Media3-зависимости добавляются в `composeApp/androidMain` (артефакты `media3-ui` уже есть в `gradle/libs.versions.toml:109`) |
| R3 | Метаданные записей — JSON в `multiplatform-settings` (класс `Settings` из `shared/platform/Platform.kt`), файлы — в приватной директории через expect/actual `RecordingFileStorage` | Room не заведён в проекте (KSP, Android-only); метаданные малы (~десятки записей); `Settings` уже используется для `auth_token`, `onboarding_completed` |
| R4 | Субтитры — собственный парсер WebVTT (~100 строк) в `commonMain`, панель под плеером (дизайн v1.0) | Стабильной KMP-библиотеки WebVTT нет; парсинг на клиенте снимает нагрузку с backend (вариант «cues с backend» из PRD отклонён) |
| R5 | Ошибки в State — `String?`, маппинг в человеческий текст — через существующий `userFriendlyError` (`app/components/Common.kt`) | Монолит composeApp **не зависит от `:core:presentation`** — `UiText` там недоступен (проверено: в `composeApp/build.gradle.kts` нет `projects.core.presentation`; существующие VM, напр. `AudioTestViewModel`, используют `error: String?`). UiText — долгосрочная цель (memory.md §15), внедрение в этой фиче не делаем, чтобы не тащить зависимость |
| R6 | Android-first: ios/desktop/wasm — стабы «недоступно на этой платформе» для записи и видео | PRD, решения владельца 2026-07-30 |
| R7 | Practice — гейтинг по `AuthMode`: гость видит `SpeakingGate` (`app/components/SpeakingAuth.kt`) в нижней зоне QuestionsScreen — «Ты почти у цели!» + кнопки «Зарегистрироваться»/«Войти» | PRD Story 3/5, мокап frame-locked |

**Что НЕ делаем (Out of Scope, по PRD):** iOS/Desktop/WASM-реализации, ASR-скоринг, удаление legacy-фич, пуши.

---

## 1. Навигация

### 1.1 Как устроена навигация сейчас

`composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt`:

- `sealed class AppScreen` (строки 444–461) + `var currentScreen by remember { mutableStateOf<AppScreen>(...) }`.
- **Back stack отсутствует** — навигация одноячейковая. Каждый экран обязан явно указывать цель `onBack` (см. граблю: WASM browser history guard, memory.md §5 запись 2026-07-20).
- Bottom nav показывается только для `Home/Categories/Groups/Leaderboard/Profile` (функция `MainAppContent`, `showBottomNav`).
- Экраны получают VM через `koinViewModel()` в `MainAppContent`; VM **переживают** переходы между экранами → состояние нужно сбрасывать при входе (паттерн `testViewModel.resetTest()` в ветке `AppScreen.TestPlay`).

### 1.2 Новые записи AppScreen

```kotlin
// composeApp/src/commonMain/kotlin/com/sotospeak/app/App.kt — дополнение sealed class AppScreen

sealed class AppScreen {
    // ... существующие записи без изменений ...

    // Speaking-тренажёр
    data object Library : AppScreen()                                // список тем (Libraries)
    data class Topics(val libraryId: String) : AppScreen()           // топики внутри темы
    data class Video(val topicId: String, val withSubtitles: Boolean) : AppScreen()
    data class Questions(val topicId: String) : AppScreen()
    data class Training(val topicId: String) : AppScreen()
    data class Practice(val topicId: String) : AppScreen()
    data object MySubmissions : AppScreen()
}
```

### 1.3 Flow (по PRD «Navigation flow»)

```
  Splash ──► (первый запуск ──► Onboarding: 3 слайда, «Начать») ──► Library
                    │
                    │   UNKNOWN-сессия ──► startGuestSession() синхронно до навигации
                    ▼
  Library ──► Topics(libraryId) ──► Video(topicId)
                    │                   │  (mode-chips «С субтитрами/Без субтитров»
                    │                   │   на экране видео; bottom-sheet удалён, DC-5)
                    │                   ▼
                    │            Questions(topicId)
                    │               │           │
                    │               ▼           ▼
                    │        Training(topicId)  Practice(topicId)  [гость → SpeakingGate]
                    │               │           │
                    └───────────────┘           ▼
                      «К библиотеке»     MySubmissions («Отправки»)
                                              │
                              (после REVIEWED — просмотр оценки)

  Login/Register — только из авторизованной зоны: Practice-гейт (QuestionsScreen)
  и гостевой профиль. Экрана выбора режима «Как начнём?» НЕТ (мокап frame-onboarding).
```

Правила переходов (явные, т.к. back stack отсутствует):

| Экран | onBack | Доп. переходы |
|---|---|---|
| Onboarding | — | 3 слайда («Смотри видео» → «Тренируйся вслух» → «Отправь учителю»), иллюстрации — векторные иконки `SpeakingIcons.*` (Play/Mic/Send) для совместимости с WASM canvas; CTA «Далее»×2 → «Начать» → `Library`; показывается только при первом запуске (`onboarding_completed` в Settings) |
| Library | `Home` | `Topics(libraryId)` по клику на тему; заголовок «Библиотека тем» |
| Topics | `Library` | `Video(topicId)` по клику на топик — сразу видео (bottom-sheet удалён, DC-5) |
| Video | `Topics(libraryId)` | mode-chips «С субтитрами/Без субтитров» на экране; `Questions(topicId)` — кнопка «Перейти к вопросам» (доступна всегда, PRD Story 2; в error-стабе плеера — «К вопросам») |
| Questions | `Video(topicId)` или `Topics` | `Training(topicId)` / `Practice(topicId)` (гость → SpeakingGate: «Зарегистрироваться»/«Войти») |
| Training | `Questions(topicId)` | `Library` — кнопка «К библиотеке» (PRD: «из любого места Training можно вернуться в Library») |
| Practice | `Questions(topicId)` (запрещён во время записи — см. §6) | `MySubmissions` после успешной отправки |
| MySubmissions | `Library` | — |

> ⚠️ Грабля «залипший экран» (memory.md, запись 2026-07-27 про onboarding): при переходах из VM-событий всегда выставлять `currentScreen` явно — никаких LaunchedEffect-«автопереходов» по косвенным признакам.

### 1.4 Изменения в `MainAppContent` и bottom nav

- В `showBottomNav` добавить `AppScreen.Library` и `AppScreen.MySubmissions`.
- Bottom nav (пивот + guest-first): пункты `Library` («Темы», `SpeakingIcons.Home`), `MySubmissions` («Отправки», `SpeakingIcons.Send`), `Profile` («Профиль», `SpeakingIcons.User`). Старые пункты (`Categories`, `Groups`, `Leaderboard`) из bottom nav убраны; legacy-код позже удалён (2026-08-01, bd `8tg.7`).
- Стартовый экран после Splash/Onboarding меняется: `AppScreen.Home` → `AppScreen.Library` (ветка Splash в `AppContent`, строки 91–99, и `onContinueAsGuest`, строка 115).
- Каждая новая ветка `when (currentScreen)` — по существующему шаблону (`collectAsState()` + `LaunchedEffect(id) { vm.load(id) }` + колбэки навигации), см. ветку `AppScreen.TestPlay` как референс.

---

## 2. MVI-контракты экранов

Конвенция (memory.md §2): тройка State (data class) / Action (sealed interface) / Event (sealed interface), VM: `MutableStateFlow(State())` + `Channel<Event>.receiveAsFlow()` + `fun onAction(...)`. Референс-паттерн — `feature-home/.../presentation/HomeViewModel.kt` (канонический MVI), в монолите — `app/viewmodel/AudioTestViewModel.kt`.

Файловая раскладка (монолит, плоско, как существующие VM):

```
composeApp/src/commonMain/kotlin/com/sotospeak/app/
├── screens/
│   ├── LibraryScreen.kt
│   ├── TopicsScreen.kt
│   ├── VideoScreen.kt
│   ├── QuestionsScreen.kt
│   ├── TrainingScreen.kt
│   ├── PracticeScreen.kt
│   └── MySubmissionsScreen.kt
└── viewmodel/
    ├── LibraryViewModel.kt        # + LibraryState/Action/Event (в этом же файле, как AudioTestViewModel)
    ├── TopicsViewModel.kt
    ├── VideoViewModel.kt
    ├── QuestionsViewModel.kt
    ├── TrainingViewModel.kt
    ├── PracticeViewModel.kt
    └── MySubmissionsViewModel.kt
```

Общие элементы всех State: `isLoading: Boolean = false`, `error: String? = null` (решение R5). События навигации — через `Channel` (паттерн feature-home), обработка в экране через `ObserveAsEvents`-подобный LaunchedEffect (в монолите своего `ObserveAsEvents` нет — добавить 15-строчный helper в `app/util/`, копия из `core/presentation/ui/ObserveAsEvents.kt`).

### 2.1 LibraryScreen

```kotlin
// app/viewmodel/LibraryViewModel.kt
data class LibraryState(
    val isLoading: Boolean = false,
    val libraries: List<SpeakingLibrary> = emptyList(),
    val error: String? = null
)

sealed interface LibraryAction {
    data object OnRefresh : LibraryAction
    data class OnLibraryClick(val libraryId: String) : LibraryAction
    data object OnClearError : LibraryAction
}

sealed interface LibraryEvent {
    data class NavigateToTopics(val libraryId: String) : LibraryEvent
}
```

Загрузка — `api.getSpeakingLibraries()` при входе (LaunchedEffect в App.kt) и по `OnRefresh` (pull-to-refresh). Пустые темы (topicCount == 0) фильтруются на клиенте как страховка (backend тоже фильтрует — Part 1).

### 2.2 TopicsScreen

```kotlin
// app/viewmodel/TopicsViewModel.kt
data class TopicsState(
    val isLoading: Boolean = false,
    val libraryTitle: String = "",
    val topics: List<TopicUiModel> = emptyList(),
    val error: String? = null
)

/** UI-модель топика: DTO + локальный прогресс из Settings (просмотрен / есть training-записи) */
data class TopicUiModel(
    val id: String,
    val title: String,
    val durationSeconds: Int,
    val hasSubtitles: Boolean,          // из DTO SpeakingTopicListItem.hasSubtitles — иначе выбор «с субтитрами» скрыт (PRD Edge Cases)
    val isWatched: Boolean,             // локальный флаг (Settings, ключ topic_watched_<id>)
    val hasLocalRecordings: Boolean     // есть training-записи в RecordingStore
)

sealed interface TopicsAction {
    data object OnRefresh : TopicsAction
    data class OnTopicClick(val topicId: String) : TopicsAction        // открывает bottom-sheet выбора субтитров
    data class OnSubtitleChoice(val topicId: String, val withSubtitles: Boolean) : TopicsAction
    data class OnSkipVideo(val topicId: String) : TopicsAction
    data object OnBack : TopicsAction
}

sealed interface TopicsEvent {
    data class NavigateToVideo(val topicId: String, val withSubtitles: Boolean) : TopicsEvent
    data class NavigateToQuestions(val topicId: String) : TopicsEvent
    data object NavigateBack : TopicsEvent
}
```

### 2.3 VideoScreen

```kotlin
// app/viewmodel/VideoViewModel.kt
data class VideoState(
    val isLoading: Boolean = false,
    val topic: SpeakingTopicDetail? = null,
    val playerState: VideoPlayerState = VideoPlayerState(),   // см. §3
    val subtitlesEnabled: Boolean = false,
    val subtitleCues: List<SubtitleCue> = emptyList(),        // см. §3.3
    val videoError: Boolean = false,                          // «видео не загружается» — retry + «К вопросам» (PRD Edge Cases)
    val error: String? = null
)

sealed interface VideoAction {
    data class OnLoad(val topicId: String, val withSubtitles: Boolean) : VideoAction
    data object OnToggleSubtitles : VideoAction               // переключатель доступен во время просмотра (PRD Story 2)
    data object OnPlayPause : VideoAction
    data class OnSeek(val positionMs: Long) : VideoAction
    data object OnRetryVideo : VideoAction
    data object OnGoToQuestions : VideoAction
    data object OnBack : VideoAction
}

sealed interface VideoEvent {
    data class NavigateToQuestions(val topicId: String) : VideoEvent
    data object NavigateBack : VideoEvent
}
```

При успешном досмотре (или нажатии «К вопросам» после старта воспроизведения) VM ставит локальный флаг `topic_watched_<id>` в `Settings`.

### 2.4 QuestionsScreen

```kotlin
// app/viewmodel/QuestionsViewModel.kt
data class QuestionsState(
    val isLoading: Boolean = false,
    val topicTitle: String = "",
    val questions: List<SpeakingQuestion> = emptyList(),
    val isGuest: Boolean = false,          // Practice заблокирован для гостя (PRD Story 3)
    val error: String? = null
)

sealed interface QuestionsAction {
    data class OnLoad(val topicId: String) : QuestionsAction
    data object OnStartTraining : QuestionsAction
    data object OnStartPractice : QuestionsAction           // гостю недоступен — SpeakingGate на экране
    data object OnBack : QuestionsAction
}

sealed interface QuestionsEvent {
    data class NavigateToTraining(val topicId: String) : QuestionsEvent
    data class NavigateToPractice(val topicId: String) : QuestionsEvent
    data object NavigateBack : QuestionsEvent
}
```
> Событие `ShowLoginCta` удалено (2026-08-01, guest-first): гейт гостя — не VM-событие, а перманентный `SpeakingGate` в нижней зоне экрана с прямыми колбэками `onRegisterClick`/`onLoginClick`.

### 2.5 TrainingScreen

```kotlin
// app/viewmodel/TrainingViewModel.kt
data class TrainingState(
    val isLoading: Boolean = false,
    val topicTitle: String = "",
    val questions: List<SpeakingQuestion> = emptyList(),   // весь список виден на экране — отвечаем на все сразу
    val attempts: List<RecordingMeta> = emptyList(),       // попытки топика (макс. 3); запись = все вопросы одним дублем
    val recorder: RecorderUiState = RecorderUiState.Idle,
    val attemptNumber: Int = 1,                               // 1..3; лимит = timerLimitFor(attemptNumber), см. §5.3
    val remainingSeconds: Int = 0,                            // видимый обратный отсчёт
    val isFinished: Boolean = false,                          // true после 3-й попытки → финальный блок CTA
    val playingRecordingPath: String? = null,
    val micPermission: MicPermissionState = MicPermissionState.Unknown, // см. §4.3
    val error: String? = null
)

sealed interface RecorderUiState {
    data object Idle : RecorderUiState
    data object RequestingPermission : RecorderUiState
    data class Recording(val startedAtMs: Long) : RecorderUiState
    data object Saving : RecorderUiState
    data class Error(val message: String) : RecorderUiState       // микрофон занят / нет места и т.п.
}

sealed interface TrainingAction {
    data class OnLoad(val topicId: String) : TrainingAction
    data object OnStartRecording : TrainingAction
    data object OnStopRecording : TrainingAction                 // досрочный стоп — попытка засчитывается
    data class OnPlayRecording(val path: String) : TrainingAction
    data object OnStopPlayback : TrainingAction
    // удаления/перезаписи попыток НЕТ (дизайн-система v1.0): записи только прослушиваются, ✅ автоматически
    data object OnGoToPractice : TrainingAction                  // финальный CTA «Перейти к практике»
    data object OnRestartAttempts : TrainingAction               // «Начать заново с попытки 1»: удаляет записи топика, attemptNumber=1
    data object OnInterruption : TrainingAction                  // звонок/сворачивание → автостоп (§4.4), попытка засчитывается
    data class OnPermissionResult(val granted: Boolean) : TrainingAction
    data object OnBackToLibrary : TrainingAction
    data object OnBack : TrainingAction
}

sealed interface TrainingEvent {
    data object NavigateToLibrary : TrainingEvent
    data object NavigateBack : TrainingEvent
    data class ShowMessage(val text: String) : TrainingEvent       // snackbar («Черновик сохранён» и т.п.)
}
```

### 2.6 PracticeScreen

```kotlin
// app/viewmodel/PracticeViewModel.kt
data class PracticeState(
    val isLoading: Boolean = false,
    val topicTitle: String = "",
    val questions: List<SpeakingQuestion> = emptyList(),      // показываем списком — отвечать нужно на все
    val phase: PracticePhase = PracticePhase.Ready,
    val remainingSeconds: Int = PRACTICE_LIMIT_SECONDS,       // 30
    val takeFilePath: String? = null,                         // единственный тейк (файл для upload)
    val uploadProgress: Int = 0,                              // 0..100 для панели «Отправка учителю…»
    val uploadError: Boolean = false,                         // retry; файл не теряется (PRD Story 5)
    val micPermission: MicPermissionState = MicPermissionState.Unknown,
    val error: String? = null
) { companion object { const val PRACTICE_LIMIT_SECONDS = 30 } }

// Дизайн-система v1.0: фазы Review и ручной отправки НЕТ — после остановки запись уходит автоматически
enum class PracticePhase { Ready, Recording, Uploading, Sent }

sealed interface PracticeAction {
    data class OnLoad(val topicId: String) : PracticeAction
    data object OnStart : PracticeAction                       // запуск записи + таймер 30с
    data object OnStopEarly : PracticeAction                   // ручная остановка → немедленная автоотправка
    data object OnRetryUpload : PracticeAction
    data object OnInterruption : PracticeAction                // автостоп → автоотправка (PRD Edge Cases)
    data class OnPermissionResult(val granted: Boolean) : PracticeAction
    data object OnBack : PracticeAction                        // заблокирован в фазах Recording/Uploading (подтверждение-диалог)
}

sealed interface PracticeEvent {
    data object NavigateToMySubmissions : PracticeEvent        // после успешной отправки
    data object NavigateBack : PracticeEvent
    data class ShowMessage(val text: String) : PracticeEvent
}
```

### 2.7 MySubmissionsScreen

```kotlin
// app/viewmodel/MySubmissionsViewModel.kt
data class MySubmissionsState(
    val isLoading: Boolean = false,
    val submissions: List<SpeakingSubmission> = emptyList(), // DTO из shared, см. §8.3
    val pendingUploads: List<RecordingMeta> = emptyList(),    // локальные неотправленные (offline retry, §6.4)
    val error: String? = null
)

sealed interface MySubmissionsAction {
    data object OnRefresh : MySubmissionsAction
    data class OnRetryPending(val path: String) : MySubmissionsAction
    data object OnBack : MySubmissionsAction
}

sealed interface MySubmissionsEvent {
    data object NavigateBack : MySubmissionsEvent
}
```
## 3. Видеоплеер и субтитры

### 3.1 Размещение и зависимости

Media3 ExoPlayer уже используется в проекте для аудио — `shared/src/androidMain/.../Platform.android.kt` (`actual class AudioPlayer`). Видеоплеер размещаем в **composeApp** (решение R2):

```
composeApp/src/
├── commonMain/kotlin/com/sotospeak/app/player/
│   ├── VideoPlayerController.kt        # expect class + VideoPlayerState
│   ├── VideoPlayerView.kt              # common composable-обёртка + expect NativeVideoSurface
│   └── ...
├── androidMain/kotlin/com/sotospeak/app/player/
│   └── VideoPlayerController.android.kt
├── desktopMain/kotlin/com/sotospeak/app/player/
│   └── VideoPlayerController.desktop.kt   # СТАБ
├── iosMain/...  # СТАБ
└── wasmJsMain/... # HTML5-плеер (v1.6): DOM `<video>` поверх canvas, см. §3.2
```

`composeApp/build.gradle.kts`, `androidMain.dependencies` — добавить:

```kotlin
androidMain.dependencies {
    // ... существующие ...
    implementation(libs.androidx.media3.exoplayer)   // 1.5.1, уже в libs.versions.toml:108
    implementation(libs.androidx.media3.ui)          // уже в libs.versions.toml:109 (PlayerView)
}
```

### 3.2 Контракт

```kotlin
// composeApp/src/commonMain/kotlin/com/sotospeak/app/player/VideoPlayerController.kt
package com.sotospeak.app.player

import kotlinx.coroutines.flow.StateFlow

data class VideoPlayerState(
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val error: String? = null
)

expect class VideoPlayerController() {
    val state: StateFlow<VideoPlayerState>
    fun prepare(url: String)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
}

// VideoPlayerView.kt
@Composable
expect fun NativeVideoSurface(
    controller: VideoPlayerController,
    modifier: Modifier = Modifier
)
```

Android actual:

```kotlin
// composeApp/src/androidMain/kotlin/com/sotospeak/app/player/VideoPlayerController.android.kt
actual class VideoPlayerController {
    private val context = ApplicationProvider.get()   // см. примечание ниже
    private var player: ExoPlayer? = null
    private val _state = MutableStateFlow(VideoPlayerState())
    actual val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    actual fun prepare(url: String) {
        val p = player ?: ExoPlayer.Builder(context).build().also { player = it }
        p.setMediaItem(MediaItem.fromUri(url))
        p.addListener(/* Player.Listener: onPlaybackStateChanged → isReady/isBuffering,
                         onPlayerError → state.error; onIsPlayingChanged */)
        p.prepare()
    }
    // play/pause/seekTo/release — прямые делегаты ExoPlayer
    // positionMs: корутина-тикер 250 мс пока isPlaying (обновляет state, нужен для субтитров)
}

@Composable
actual fun NativeVideoSurface(controller: VideoPlayerController, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = true                     // play/pause/seek из коробки (PRD Story 2)
                player = (controller as VideoPlayerController).player
            }
        }
    )
}
```

Примечания реализации:
- Контекст: как в `Platform.android.kt` для AudioPlayer — через тот же механизм (проверить, как shared получает Context; вариант — передавать `PlatformContext`/`Context` в фабрику Koin android-модуля; НЕ хранить Activity-context).
- `release()` вызывать из `DisposableEffect(Unit)` в VideoScreen и из `VideoViewModel.onCleared()`.
- Поворот экрана: VM переживает конфигурацию; контроллер пересоздаёт `PlayerView`, позицию восстанавливаем из `state.positionMs` (`seekTo` после `prepare`).
- Десктоп/iOS/WASM-стабы: `NativeVideoSurface` рендерит заглушку «Видео недоступно на этой платформе» + кнопку «К вопросам»; контроллер — no-op с `state.error = "unsupported"`.

### 3.3 Парсер WebVTT (commonMain, свой)

```kotlin
// composeApp/src/commonMain/kotlin/com/sotospeak/app/subtitles/WebVttParser.kt
package com.sotospeak.app.subtitles

data class SubtitleCue(
    val startMs: Long,
    val endMs: Long,
    val text: String          // многострочный текст cue; теги <b>/<i>/<c> вычищаем regex'ом
)

object WebVttParser {
    /**
     * Минимальный парсер WebVTT:
     * - пропускает шапку "WEBVTT" и NOTE-блоки;
     * - тайминги "00:00.500 --> 00:02.000" или "00:00:00.500 --> ...";
     * - игнорирует настройки cue (align/position — всё после второго пробела в строке тайминга);
     * - склеивает многострочный текст cue до пустой строки.
     */
    fun parse(vtt: String): List<SubtitleCue> { /* ~60 строк */ }

    private fun parseTimestamp(ts: String): Long { /* hh:mm:ss.mmm | mm:ss.mmm → мс */ }
}
```

Загрузка субтитров: обычный GET через существующий Ktor-клиент (`client.get(subtitlesUrl).bodyAsText()`) — добавить в `VideoViewModel`, не в `SoToSpeakApi` (файл лежит в MinIO по публичному URL, не API-эндпоинт; URL приходит в `SpeakingTopicDetail.video?.subtitleUrl`). Юнит-тесты парсера в `commonTest` (минимум: тайминги обоих форматов, NOTE, многострочный cue, мусорные строки).

### 3.4 Транскрипт: полный текст с пословной подсветкой (v1.6, заменяет SubtitlePanel)

Под плеером — **TranscriptPanel**: весь текст видео сразу (LazyColumn по cue-абзацам), скроллится независимо; CTA «Перейти к вопросам» доступен всегда. Источник текста — существующий WebVTT-файл (отдельного поля/файла транскрипта НЕ вводится).

Подсветка по `positionMs`: произнесённые слова — `speaking.text`, непроизнесённые — `speaking.textMuted`, текущее слово — плавный `lerp(textMuted→text)` по доле прогресса внутри слова + полужирный; автоскролл к активному cue. Reduce-motion платформы — мгновенное переключение без заливки.

Пословные тайминги (`SubtitleWord(text, startMs, endMs)` в `SubtitleCue.words`):
- karaoke-таймкоды `<mm:ss.mmm>` внутри cue — точная синхронизация (файлы готовятся внешними инструментами, напр. Whisper); karaoke-теги вычищаются из текста отдельным regex (tagRegex их не покрывает — грабля №89);
- иначе интерполяция: слова делят окно cue пропорционально длине слова.

Переключение — mode-chips «С субтитрами / Без субтитров» + CC (`testTag("subtitles_toggle")`); панель видна только в режиме «С субтитрами» при наличии cue. Если `topic.video?.subtitleUrl == null` — выбор и панель скрыты (PRD Edge Cases).

**WASM (v1.6)**: видео воспроизводится через HTML5 `<video>` в DOM поверх canvas (canvas-only CMP; позиционирование по `onGloballyPositioned` области плеера, координаты в CSS px = layout px / density — грабля №90). Compose-оверлей контролы поверх видео невозможны (DOM выше canvas) → `VideoPlayerController.supportsOverlayControls=false`: control-bar рисуется ПОД плеером (те же элементы/testTag'и), big-play/replay — только когда DOM-video скрыт (до старта/после конца), клик по видео — play/pause. Android — overlay-контролы как раньше (`supportsOverlayControls=true`). Desktop/iOS — стабы.

**Высота видео**: бокс 16:9 ограничен 45% высоты экрана (heightIn до aspectRatio — иначе на низких viewport CTA/транскрипт вытесняются за экран, грабля №91).

**Аппбары (v1.6)**: экраны §2.2–2.6 — `SpeakingAppBar` без стрелки «назад» (мокап), с breadcrumb-подзаголовком: Topics — «{тема} / N топиков · выбери и начни говорить», Video — «{топик} / {тема} · видео m:ss», Questions — «Вопросы / {тема} · {топик} · N вопросов», Training/Practice — «Training|Practice / {тема} · {топик}» (`libraryTitle` пробрасывается по AppScreen-роутам). «Назад» — системный: expect/actual `PlatformBackHandler` (Android — activity BackHandler, wasm — Escape/BrowserBack, desktop/iOS — no-op); подтверждение выхода в Practice (§6.1) сохраняется через `handleBack`.

---

## 4. Запись голоса (VoiceRecorder)

### 4.1 Контракт (expect/actual)

```
composeApp/src/commonMain/kotlin/com/sotospeak/app/recorder/
├── VoiceRecorder.kt                 # expect class + VoiceRecorderState
├── MicPermission.kt                 # expect rememberMicrophonePermissionState()
└── ...

composeApp/src/androidMain/kotlin/com/sotospeak/app/recorder/
├── VoiceRecorder.android.kt         # MediaRecorder → AAC/m4a
└── MicPermission.android.kt         # ActivityResultContracts.RequestPermission
```

```kotlin
// commonMain/app/recorder/VoiceRecorder.kt
package com.sotospeak.app.recorder

import kotlinx.coroutines.flow.StateFlow

sealed interface VoiceRecorderState {
    data object Idle : VoiceRecorderState
    data object Recording : VoiceRecorderState
    data class Stopped(val filePath: String) : VoiceRecorderState
    data class Error(val message: String) : VoiceRecorderState   // микрофон занят, нет места и т.п.
}

/**
 * Формат вывода: AAC в контейнере MPEG-4 (.m4a), моно, 44.1 кГц, 96 кбит/с.
 * 30 секунд ≈ 360 КБ — укладываемся в лимит PRD (~1–2 МБ).
 */
expect class VoiceRecorder() {
    val state: StateFlow<VoiceRecorderState>
    fun start(outputFileName: String)          // файл создаётся в директории RecordingFileStorage
    fun stop(): String?                        // корректное завершение → filePath
    fun cancel()                               // удалить файл, state → Idle
    fun release()
}
```

### 4.2 Android actual (MediaRecorder)

```kotlin
// androidMain/app/recorder/VoiceRecorder.android.kt — ключевые точки
actual fun start(outputFileName: String) {
    val file = File(recordingsDir(), "$outputFileName.m4a")   // context.filesDir/recordings/
    recorder = MediaRecorder(context).apply {                 // API 31+ конструктор с context
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setAudioSamplingRate(44100)
        setAudioEncodingBitRate(96_000)
        setAudioChannels(1)
        setOutputFile(file.absolutePath)
        prepare()   // IOException → state = Error("Не удалось начать запись")
        start()     // RuntimeException (микрофон занят) → state = Error("Микрофон занят другим приложением")
    }
}
```

- Проверка свободного места до `start()`: `File(recordingsDir()).usableSpace < 5 * 1024 * 1024` → `Error("Недостаточно места на устройстве")` (PRD Edge Cases «Мало места»).
- Стабы desktop/ios/wasm: `start()` → `state = Error("Запись недоступна на этой платформе")` (решение R6).

### 4.3 Разрешение RECORD_AUDIO (без accompanist — его нет в проекте)

Accompanist в зависимостях отсутствует (проверено по `gradle/libs.versions.toml`). Делаем вручную через `androidx.activity.compose` (уже подключён в `composeApp/androidMain`):

```kotlin
// commonMain/app/recorder/MicPermission.kt
enum class MicPermissionState { Unknown, Granted, Denied, PermanentlyDenied }

@Composable
expect fun rememberMicrophonePermissionState(
    onResult: (Boolean) -> Unit
): MicPermissionState

// androidMain — actual через rememberLauncherForActivityResult(RequestPermission())
// + ContextCompat.checkSelfPermission; PermanentlyDenied =
//   denied && !shouldShowRequestPermissionRationale (через Activity)
```

Манифест: добавить `<uses-permission android:name="android.permission.RECORD_AUDIO" />` в `app/src/androidMain/AndroidManifest.xml` (тонкая обёртка `:app`, не composeApp). Поведение при отказе (PRD Edge Cases): запись недоступна, кнопка записи disabled; UI показывает объяснение + кнопку «Открыть настройки» (`Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)`) при `PermanentlyDenied`.

### 4.4 Прерывания (звонок, сворачивание)

- **Сворачивание**: в TrainingScreen/PracticeScreen — `LifecycleEventObserver` на `ON_PAUSE` (androidMain-обёртка; в common — callback `onInterruption` через DisposableEffect+`LocalLifecycleOwner`, доступен в commonMain из lifecycle-compose). VM получает `OnInterruption` → `recorder.stop()` (НЕ `cancel()`) → черновик сохраняется, `TrainingEvent.ShowMessage("Запись остановлена и сохранена")`.
- **Телефонный звонок / потеря аудиофокуса**: android actual VoiceRecorder регистрирует `AudioManager.requestAudioFocusRequest`; на `AUDIOFOCUS_LOSS*` — сам выполняет `stop()` и выставляет `State.Stopped` (VM заметит по state и сохранит метаданные).
- Таймер при этом останавливается (job в VM отменяется по выходу из фазы Recording).
- Поворот экрана: фаза записи и `remainingSeconds` живут в VM → переживают конфигурацию (PRD Edge Cases).

---

## 5. Training-режим

### 5.1 Локальное хранилище записей (решение R3)

```kotlin
// commonMain/app/storage/RecordingFileStorage.kt
package com.sotospeak.app.storage

/** Файловые операции. Android/desktop actual — java.io.File; ios/wasm — стаб UnsupportedOperationException. */
expect class RecordingFileStorage() {
    fun recordingsDir(): String
    fun exists(path: String): Boolean
    fun readBytes(path: String): ByteArray
    fun delete(path: String): Boolean
    fun usableSpaceBytes(): Long
}

// commonMain/app/storage/RecordingStore.kt
@Serializable
data class RecordingMeta(
    val filePath: String,
    val topicId: String,
    val attemptNumber: Int,           // 1..3 для TRAINING (null-эквивалент: 0 для PRACTICE)
    val kind: RecordingKind,          // TRAINING | PRACTICE
    val durationMs: Long,
    val timerLimitSeconds: Int,       // лимит, на котором сделана запись
    val createdAtEpochMs: Long,
    val uploaded: Boolean = false     // для PRACTICE: offline-retry, §6.4
)

enum class RecordingKind { TRAINING, PRACTICE }

class RecordingStore(
    private val settings: Settings,               // shared/platform Settings, уже в Koin
    private val fileStorage: RecordingFileStorage
) {
    // Ключ: "speaking_recordings" — JSON List<RecordingMeta> (kotlinx.serialization).
    // Обоснование против Room: Room не заведён (KSP, Android-only), объём данных — десятки записей,
    // Settings уже используется для auth_token/onboarding_completed (memory.md §2).
    fun list(topicId: String? = null): List<RecordingMeta>
    fun add(meta: RecordingMeta)
    fun remove(filePath: String)                   // + fileStorage.delete(filePath)
    fun markUploaded(filePath: String)
    fun pendingPractice(): List<RecordingMeta>     // kind=PRACTICE && !uploaded
}
```

Имя файла: `rec_<topicId>_attempt<N>_<epochMs>.m4a` для Training, `rec_<topicId>_practice_<epochMs>.m4a` для Practice. Попыток на топик максимум 3 (дизайн v1.1); «Начать заново с попытки 1» удаляет все записи топика через `RecordingStore.remove`.

### 5.2 Прослушивание

Через существующий `AudioPlayer` из `shared/platform/Platform.kt` (`play(url)`, `stop()`, `setOnCompletionListener`). Для локального файла на Android ExoPlayer принимает `file://`-URI — передавать `Uri.fromFile(...).toString()`. В Koin: `factory { AudioPlayer() }` (один экземпляр на TrainingScreen, `release()` в `DisposableEffect`).

> ⚠️ Грабля №1 memory.md: `ModernAudioPlayer.kt.disabled` — НЕ использовать, только legacy `AudioPlayer` expect/actual.

### 5.3 Лимиты попыток (state machine)

Детерминированная функция уровня (дизайн-система v1.1: ровно 3 попытки на топик, каждая попытка — одна запись с ответами на ВСЕ вопросы; лимит привязан к номеру попытки; вычисляется из числа записей топика — устойчиво к перезапуску):

```kotlin
// commonMain/app/viewmodel/TrainingViewModel.kt (companion)
/**
 * Лимит попытки (PRD Story 4, дизайн v1.1):
 *   попытка 1 → 80с
 *   попытка 2 → 50с
 *   попытка 3 → 30с
 * attemptNumber = attempts.size + 1 (макс. 3); после 3-й — isFinished=true.
 */
fun timerLimitFor(attemptNumber: Int): Int = when (attemptNumber) {
    1 -> 80
    2 -> 50
    else -> 30
}
```

Конечный автомат записи:

```
        OnStartRecording                таймер истёк / OnStopRecording / OnInterruption
Idle ───────────────────► Recording ───────────────────────────────────────────► Saving ──► Idle
  ▲                         │  remainingSeconds: limit → 0, тик 1с (delay(1000), паттерн
  │                         │  AudioTestViewModel.startTimer); при 0 → автостоп (PRD Story 4)
  └────── OnPermissionResult(denied) / RecorderUiState.Error ────── показать ошибку
```

UI: **весь список вопросов топика на экране** (как в Practice, `testTag("training_questions_list")`, `question_item_<n>`) — ученик отвечает на все вопросы подряд одной записью. Видимый обратный отсчёт `remainingSeconds` крупно (`testTag("training_timer")`), прогресс-кольцо (guard от деления на 0 — грабля №17б memory.md: `progress = remaining / limit.toFloat()`, limit всегда > 0 по построению). Level-chip «Уровень N · X сек» + 3 индикатора-шага (`testTag("level_chip")`). Список попыток (максимум 3): только прослушать (`testTag("recording_item_<n>")`, `play_recording_<n>`), ✅ «принята» ставится автоматически (`attempt_check_<n>`); удаления нет. После 3-й попытки rec-zone скрывается, финальный блок (`final_cta`): «Перейти к практике» (`final_go_practice`), «Вернуться в библиотеку» (`final_back_library`), «Начать заново с попытки 1» (`final_restart`). Privacy-note: «Записи хранятся только на твоём устройстве».

---

## 6. Practice-режим

### 6.1 Фазовая машина (30 секунд, один тейк, автоотправка)

Дизайн-система v1.0: фазы Review и ручной кнопки «Отправить» **нет** — после остановки (автостоп на 0:00, ручной стоп или прерывание) запись сразу уходит учителю.

```
Ready ──OnStart──► Recording(30с, автостоп/OnStopEarly/OnInterruption) ──► Uploading ──► Sent ──► «Вернуться в библиотеку»
                                                                                        │
                                                                                        └──ошибка сети → uploadError=true,
                                                                                           файл остаётся, retry (§6.4)
```

- Таймер: тот же тик-паттерн; по 0 — `recorder.stop()` автоматически (PRD Story 5) → upload.
- На экране: список всех вопросов топика, level-chip «Контрольная · 30 сек», чип «1 запись на все вопросы», плашка «запись уйдёт учителю автоматически — изменить её нельзя».
- В фазах `Recording`/`Uploading` системная кнопка «назад» и `OnBack` — диалог-подтверждение («Запись будет потеряна/отправка прервётся»).
- Sent-экран: бейдж ✅, чип «статус NEW · ждёт проверки», подпись «Оценка и комментарий появятся в «Отправки»», CTA «Вернуться в библиотеку».
- После отправки запись нельзя прослушать/перезаписать (PRD) — тейк помечается `uploaded=true`.

### 6.2 Гейтинг гостя

- `QuestionsScreen` получает `isGuest = authMode == AuthMode.GUEST`. У гостя вместо кнопки Practice в нижней зоне экрана — `SpeakingGate` (`app/components/SpeakingAuth.kt`, мокап frame-locked): иконка Lock, «Ты почти у цели!», «Отправка записи учителю доступна после регистрации», `SpeakingPrimaryButton` «Зарегистрироваться» (`practice_locked_cta` → Register) + `SpeakingGhostButton` «Войти» (`practice_locked_login` → Login). Training при этом остаётся доступным (гейт НЕ full-screen).
- Дополнительная защита: в `PracticeViewModel.onAction(OnStart)` проверять `tokenProvider.getToken() != null` (токен провайдер уже в Koin), иначе — `ShowMessage("Требуется вход")`. Backend тоже ограничивает (ROLE_USER+, Part 1).

### 6.3 Multipart-загрузка через Ktor

Метод `SoToSpeakApi` (уже реализован в Фазе 1; эндпоинт — по Part 1):

```kotlin
// shared/src/commonMain/kotlin/com/sotospeak/shared/api/SoToSpeakApi.kt
suspend fun submitSpeakingPractice(
    topicId: String,
    durationSec: Int,
    audioBytes: ByteArray,
    fileName: String = "recording.m4a"   // "practice_<topicId>_<epochMs>.m4a"
): Result<SpeakingSubmission> = safeCall {
    client.submitFormWithBinaryData(
        url = "/api/speaking/submissions",
        formData = formData {
            append("topicId", topicId)
            append("durationSec", durationSec.toString())
            append("file", audioBytes, Headers.build {
                append(HttpHeaders.ContentType, "audio/mp4")
                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
            })
        }
    ).body()
}
```

Байты читаются через `RecordingFileStorage.readBytes(path)` — commonMain-контракт, платформенной специфики в VM нет. `defaultRequest { contentType(Json) }` в клиенте не мешает: `submitFormWithBinaryData` выставляет multipart content-type сам (проверить на первой интеграции — при необходимости вынести `contentType` в отдельные запросы). Лимит тела: nginx `client_max_body_size 50m` (memory.md, решение 2026-07-20) — для ~0.4 МБ достаточно.

### 6.4 Offline retry

- При ошибке сети (uploadError): `RecordingMeta(kind=PRACTICE, uploaded=false)` остаётся в `RecordingStore`, файл не удаляется (PRD Edge Cases «запись не теряется»).
- Retry: (а) кнопка «Повторить» на PracticeScreen; (б) автоматическая попытка при входе на MySubmissionsScreen (`OnRefresh` → для каждого `pendingPractice()` вызвать upload); (в) кнопка retry на элементе списка MySubmissions.
- После успешного upload: `markUploaded(filePath)` + удалить локальный файл (он уже в MinIO; освобождаем место). Повторная отправка на тот же топик разрешена — каждая отправка отдельная запись (PRD Edge Cases).

---

## 7. Экран MySubmissions

- Список `SpeakingSubmission` (новые сверху): название топика, дата, статус-чип `NEW` («На проверке») / `REVIEWED` («Проверено»).
- У REVIEWED — раскрывающаяся карточка оценки по рубрике (PRD Story 7): 4 критерия с барами 1–10 (grammar / vocabulary / pronunciation / fluency), общий балл (усреднение считает backend — Part 1), текстовый комментарий учителя.
- Прослушивание своей отправленной записи: по `audioUrl` через `AudioPlayer` (публичный MinIO URL; учесть BUG-004 — URL должен быть `S3_PUBLIC_URL`, memory.md грабля №2).
- Секция «Не отправлено» (`pendingUploads` из RecordingStore) с кнопкой retry — визуально отделена.
- Empty state: «У вас пока нет отправленных записей» + CTA в Library (PRD Edge Cases).
- Доступен только авторизованным; гость на этом экране — GuestProfileStub по мокапу frame-profile-guest (`SpeakingGate` с 📬: «Зарегистрируйся, чтобы отправлять записи учителю и видеть оценки» + «Зарегистрироваться»/«Уже есть аккаунт? Войти»). Авторизованный профиль по frame-profile: аватар 88dp с инициалами, stat-карточки (отправки/топики из MySubmissionsViewModel), «Выйти» danger-ghost (logout переехал из Settings в профиль).

---

## 8. DI, shared/api, модели

### 8.1 Koin (`composeApp/.../app/di/AppModule.kt`)

```kotlin
// Дополнения в appModule:
// Speaking-тренажёр
single<RecordingFileStorage> { RecordingFileStorage() }
single { RecordingStore(get(), get()) }
factory { AudioPlayer() }                                   // shared/platform, для прослушивания записей

viewModel { LibraryViewModel(get()) }
viewModel { TopicsViewModel(get(), get()) }                 // api + RecordingStore (hasLocalRecordings)
viewModel { VideoViewModel(get(), get()) }                  // api + Settings (флаги topic_watched_*)
viewModel { QuestionsViewModel(get(), get()) }              // api + TokenProvider (гейтинг)
viewModel { TrainingViewModel(get(), get(), get()) }        // api + RecordingStore (+ VoiceRecorder by inject в экране)
viewModel { PracticeViewModel(get(), get(), get()) }        // api + RecordingStore + TokenProvider
viewModel { MySubmissionsViewModel(get(), get()) }          // api + RecordingStore
```

`VoiceRecorder` и `VideoPlayerController` — НЕ в Koin: создаются экраном через `remember { VoiceRecorder() }` + `DisposableEffect { onDispose { release() } }` (жизненный цикл привязан к композиции, expect class без аргументов конструктора). Альтернатива — `factory { VoiceRecorder() }` в Koin; выбираем remember-подход как более простой.

#### Рантайм-override baseUrl
- `AppConfig` принимает `baseUrlProvider: () -> String`; значение читается лениво перед каждым запросом.
- Android: `Settings("sotospeak.preferences")` ключ `api_base_url_override` перекрывает `BuildConfig.API_BASE_URL`. Изменение применяется без перезапуска приложения.
- WASM/Desktop: env/location определяет URL при старте.

### 8.2 Методы `SoToSpeakApi` (дополнения; пути — по Part 1)

```kotlin
// Публичный контент (доступен гостю, как getCategories):
suspend fun getSpeakingLibraries(): Result<List<SpeakingLibrary>> = safeCall {
    client.get("/api/public/speaking/libraries").body()
}
suspend fun getSpeakingTopics(libraryId: String): Result<List<SpeakingTopicListItem>> = safeCall {
    client.get("/api/public/speaking/libraries/$libraryId/topics").body()
}
suspend fun getSpeakingTopicDetail(topicId: String): Result<SpeakingTopicDetail> = safeCall {
    client.get("/api/public/speaking/topics/$topicId").body()   // топик + видео + вопросы
}
// Авторизованные:
suspend fun submitSpeakingPractice(...) // см. §6.3
suspend fun getMySpeakingSubmissions(): Result<List<SpeakingSubmission>> = safeCall {
    client.get("/api/speaking/submissions/my").body()
}
```

> ⚠️ Все пути с префиксом `/api` — context-path backend (грабля №9 и баг от 2026-07-20 про public-эндпоинты без `/api`).

### 8.3 Модели (shared) — зеркала DTO из Part 1 (реализованы в Фазе 1, здесь сигнатуры)

```kotlin
// shared/src/commonMain/kotlin/com/sotospeak/shared/model/Speaking.kt
@Serializable data class SpeakingLibrary(
    val id: String, val title: String, val description: String? = null,
    val coverUrl: String? = null, val topicCount: Int
)
@Serializable data class SpeakingTopicListItem(
    val id: String, val title: String, val description: String? = null,
    val durationSeconds: Int? = null, val questionCount: Int, val hasSubtitles: Boolean
)
@Serializable data class SpeakingTopicDetail(
    val id: String, val libraryId: String, val title: String,
    val description: String? = null, val video: SpeakingVideo? = null,
    val questions: List<SpeakingQuestion>
)
@Serializable data class SpeakingVideo(
    val videoUrl: String, val subtitleUrl: String? = null, val durationSeconds: Int
)
@Serializable data class SpeakingQuestion(
    val id: String, val text: String, val displayOrder: Int
)
@Serializable data class SpeakingSubmission(
    val id: String, val topicId: String, val topicTitle: String,
    val audioUrl: String, val durationSec: Int, val status: String,   // "NEW" | "REVIEWED"
    val grade: SpeakingGrade? = null, val createdAt: String? = null
)
@Serializable data class SpeakingGrade(
    val grammar: Int, val vocabulary: Int, val pronunciation: Int, val fluency: Int,
    val total: Double,                    // авто-усреднённый балл (generated column в БД, Part 1)
    val comment: String? = null, val reviewerName: String,
    val createdAt: String? = null, val updatedAt: String? = null
)
```

> ⚠️ Грабли: `is`-префиксные Boolean не использовать в DTO (грабля №18 jackson-module-kotlin — поля называть без `is` или сверить сериализацию с Part 1); в shared-моделях даты — `String` (как в существующих `AudioTest.createdAt`), не Instant; при рассинхроне имён полей с backend — `@SerialName` (прецедент: `@get:JsonProperty("newBestScore")` фикс 2026-07-27).

---

## 9. Обработка ошибок и UX-состояния

| Сценарий | Поведение | Реализация |
|---|---|---|
| Сеть недоступна (списки) | `ErrorMessage` с `userFriendlyError` + «Попробовать снова» | готовый компонент `app/components/Common.kt` (грабля №15) |
| Загрузка списков | Скелетоны (плейсхолдер-карточки с shimmer) или `LoadingIndicator` из `core:presentation`-стиля — в монолите: Box+ CircularProgressIndicator, как в существующих экранах | PRD UI/UX |
| Видео не грузится | Плашка поверх плеера «Не удалось загрузить видео» + retry + кнопка «К вопросам» (переход без видео разрешён) | `VideoState.videoError`, `OnRetryVideo` |
| Нет разрешения на микрофон | Кнопка записи disabled + объяснение; `PermanentlyDenied` → кнопка «Открыть настройки» | §4.3 |
| Микрофон занят / ошибка MediaRecorder | Не crash: `RecorderUiState.Error` с человеческим текстом, состояние → Idle | §4.2 try/catch на prepare/start |
| Мало места | Ошибка при старте записи до обращения к MediaRecorder | `usableSpaceBytes()` < 5 МБ, §4.2 |
| Обрыв сети при upload | Файл не теряется, `uploadError=true`, retry ручной + авто на MySubmissions | §6.4 |
| Прерывание записи | Автостоп + черновик сохранён + snackbar | §4.4 |
| Гость в Practice | LockedFeature-стиль + CTA логина | §6.2 |
| Сырой exception в UI | ЗАПРЕЩЕНО: все `error` из VM прогоняются через `userFriendlyError`; kotlinx.serialization-исключения маппятся правилом «notransformationfound…» (грабля №15, баг 2026-07-27) | Common.kt |

Пустые состояния: Library — «Пока нет доступных тем»; MySubmissions — empty state с CTA (§7).

---

## 10. Тестирование

### 10.1 desktopTest (паттерн — commonTest, kotest, реальные экраны + моки)

Референс: `composeApp/src/commonTest/kotlin/com/sotospeak/app/tests/HomeUserFlowTest.kt` + `BaseUiTest` (runComposeUiTest + Koin) + моки `app/di/TestMocks.kt`. Запуск: `./gradlew :composeApp:desktopTest` (uiTest — для `**/tests/**`).

- **TestMocks.kt** — добавить: `mockSpeakingLibraries` (2 темы, одна с `topicCount=0` — проверка фильтрации), `mockSpeakingTopics` (с субтитрами и без), `mockSpeakingQuestions` (3 вопроса), `mockSpeakingSubmissions` (NEW и REVIEWED с полной рубрикой), `mockRecordingMeta`-список.
- **Новые тест-классы** (`app/tests/`): `LibraryScreenTest`, `TopicsScreenTest`, `QuestionsScreenTest` (гость: Practice заблокирован, CTA виден), `TrainingScreenTest` (таймер-тег, level-chip, список попыток без delete, финальный CTA после 3-й, disabled без permission), `PracticeScreenTest` (фазы Ready/Recording/Uploading/Sent, автоотправка после стопа, гейтинг), `MySubmissionsScreenTest` (статусы, рубрика, empty state). Паттерн: реальный экран + моковый State + captured callbacks (грабля №16: `useUnmergedTree = true`, клики внизу через `performScrollTo()` в try/catch Throwable).
- **Юнит-тесты без UI**: `WebVttParserTest` (§3.3), `TrainingTimerTest` (`timerLimitFor`: попытка 1→80, 2→50, 3→30), `RecordingStoreTest` (серилизация метаданных через fake Settings — Settings expect/actual: в desktopTest работает desktop-actual на `java.util.prefs.Preferences`, как в Platform.desktop.kt).

**testTags для новых экранов** (добавлять в реальные композаблы):

| Экран | Теги |
|---|---|
| Library | `library_screen`, `library_card_<id>`, `library_empty` |
| Topics | `topics_screen`, `topic_item_<id>`, `subtitle_choice_sheet`, `subtitle_with`, `subtitle_without`, `skip_video_button` |
| Video | `video_screen`, `video_surface`, `subtitles_toggle`, `subtitle_text`, `video_error`, `go_to_questions_button` |
| Questions | `questions_screen`, `question_item_<n>`, `mode_training_button`, `mode_practice_button`, `practice_locked_cta` |
| Training | `training_screen`, `training_timer`, `training_questions_list`, `question_item_<n>`, `level_chip`, `record_button`, `stop_button`, `recording_item_<n>`, `play_recording_<n>`, `attempt_check_<n>`, `final_cta`, `final_go_practice`, `final_back_library`, `final_restart`, `back_to_library_button`, `mic_permission_rationale` |
| Practice | `practice_screen`, `practice_timer`, `practice_questions_list`, `practice_start_button`, `practice_stop_button`, `practice_auto_send_note`, `upload_panel`, `upload_retry_button`, `sent_panel`, `sent_back_button` |
| MySubmissions | `my_submissions_screen`, `submission_item_<id>`, `submission_status_<id>`, `grade_card_<id>`, `pending_upload_item`, `submissions_empty` |

### 10.2 Maestro (флоу — актуализированы 2026-08-01 под guest-first)

1. `.maestro/flows/speaking_training.yaml` — гость: онбординг-subflow (3 слайда «Смотри видео» → «Далее»×2 → «Начать») → «Библиотека тем» → «Разговорный английский» → «Знакомство» → видео (mode-chips на экране; bottom-sheet удалён) → «Перейти к вопросам» → Questions → «Тренировка · 3 попытки» → запись по record-кнопке (contentDescription «Начать запись»/«Остановить запись») → «Попытки · 1 из 3».
2. `.maestro/flows/speaking_practice_guest_gating.yaml` — гость: Questions → SpeakingGate «Ты почти у цели!» → «Войти» → экран логина.
3. `.maestro/flows/speaking_practice_auth.yaml` — логин demo@sotospeak.app через гостевой профиль («Профиль» → «Войти»; тапы по полям координатами — label-тап не переносит фокус в SpeakingField) → Practice: `launchApp` с `permissions: { android.permission.RECORD_AUDIO: allow }` → «Начать запись» → таймер 0:27 → «Остановить запись» → автоотправка → «Запись отправлена!» → «Вернуться в библиотеку» → «Отправки», статус «На проверке».
4. `.maestro/subflows/onboarding_guest.yaml` — 3 слайда → «Начать» (+retry-тап); вызывается по `visible: "Смотри видео"`.

Грабли Maestro (memory.md №14, №30, №59–61, №63): текст-матчинг ТОЧНЫЙ — regex `".*Текст.*"`; заголовок Library — «Библиотека тем»; SpeakingTextLink разбит на 2 Text — regex; тапы по id/testTag НЕ работают — по видимому тексту/contentDescription/координатам; APK пересобирать перед прогоном; статус — по реальному exit code. Запись звука на эмуляторе — виртуальный микрофон, валидный AAC-файл создаётся.

---

## 11. План задач (→ bd issues)

Оценки в идеальных днях; зависимости указаны. Создание: `bd create` с привязкой к эпику SPEAKING-TRAINER-001 (Part 1 — отдельные задачи).

| # | Задача | Содержание | Оценка | Зависит от |
|---|---|---|---|---|
| T1 | shared: модели + API | `model/Speaking.kt`, 5 методов в `SoToSpeakApi` (§8.2–8.3), включая multipart (§6.3) | 1 д | Part 1 (контракты) |
| T2 | Навигация | Новые `AppScreen`-записи, ветки в `MainAppContent`, bottom nav, стартовый экран → Library (§1) | 1 д | T1 |
| T3 | Library + Topics экраны | MVI по §2.1–2.2, скелетоны, ErrorMessage, выбор субтитров | 1.5 д | T2 |
| T4 | VideoPlayer expect/actual | Media3-зависимости, контроллер + `NativeVideoSurface`, стабы платформ (§3.1–3.2) | 1.5 д | — |
| T5 | WebVTT + VideoScreen | Парсер + юнит-тесты, SubtitlePanel под плеером + mode-chips, VideoScreen MVI (§2.3, §3.3–3.4) | 1.5 д | T3, T4 |
| T6 | VoiceRecorder + permission | expect/actual, MediaRecorder, RECORD_AUDIO в манифесте `:app`, permission-flow, прерывания, стабы (§4) | 2 д | — |
| T7 | Локальное хранилище записей | `RecordingFileStorage` expect/actual, `RecordingStore` на Settings, юнит-тесты (§5.1) | 1 д | — |
| T8 | TrainingScreen | MVI, 3 попытки на топик (лимиты 80/50/30, одна запись = все вопросы), список вопросов + список попыток (только play через AudioPlayer, авто-✅), финальные CTA, прерывания (§2.5, §5) | 2 д | T3, T6, T7 |
| T9 | PracticeScreen | Фазовая машина 30с без Review (автоотправка), upload, offline retry, гейтинг гостя (§2.6, §6) | 1.5 д | T6, T7, T8(паттерны) |
| T10 | MySubmissionsScreen | Список, статусы, рубрика, pending-uploads (§2.7, §7) | 1 д | T9 |
| T11 | DI + интеграционная проверка | Koin-дополнения (§8.1), прогон против docker-стека, фикс контрактов | 1 д | T8–T10 |
| T12 | Тесты | TestMocks-дополнения, desktopTest-сьюты, Maestro-флоу (§10) | 2 д | T8–T10 |

**Итого: ~17 д (с параллелизацией T4/T6/T7 → 8–12 календарных дней).**

Риски и открытые вопросы реализации:
1. Подключение `media3-ui` в composeApp androidMain — проверить конфликты версий с shared (тот же 1.5.1, конфликта быть не должно).
2. `submitFormWithBinaryData` + `defaultRequest { contentType(Json) }` — проверить, что multipart content-type не переопределяется (§6.3).
3. Получение `Context` в android actual'ях composeApp — сверить с механизмом shared (`Platform.android.kt`); возможно, потребуется инициализация из `SoToSpeakApplication`.
4. Maestro-флоу записи звука — 31-секундное ожидание делает флоу медленным; рассмотреть debug-флаг сокращённого лимита (НЕ в проде, аналог `ENABLE_DEBUG_TOOLS`, грабля №5).
5. После стабилизации: дополнить `memory.md` (решения R1–R7) и обновить нижнюю навигацию в `docs/USER_GUIDE.md`.
