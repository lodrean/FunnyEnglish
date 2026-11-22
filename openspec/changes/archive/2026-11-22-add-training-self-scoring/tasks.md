# Tasks: add-training-self-scoring

> Все задачи отслеживаются в bd `FunnyEnglish-h3l.5` (P3, ~1.5 дня суммарно).

## 1. Спека и proposal (bd h3l.5)
- [x] 1.1 OpenSpec change `add-training-self-scoring` (proposal + delta-spec + tasks)
- [x] 1.2 `docs/SPEAKING_TRAINER_SPEC_PART2.md` → v1.10: §2.5 (TrainingState/TrainingAction),
      §5.3 (позиция карточки), новый §5.4 «Самооценка после попытки» — дифф утверждён владельцем
      вместе с планом сессии 2026-11-22 (ADR-007)

## 2. Data-слой (bd h3l.5)
- [x] 2.1 `RecordingSelfScore` + `RecordingMeta.selfScore` + `RecordingStore.updateSelfScore`
      (паттерн markUploaded); тесты RED-GREEN в `RecordingStoreTest`
- [x] 2.2 Passthrough `SpeakingRepository.updateSelfScore`

## 3. ViewModel (bd h3l.5)
- [x] 3.1 `TrainingState.subtitleCues` + догрузка транскрипта в `load()` (паттерн
      VideoViewModel: subtitleUrl → getTextResource → WebVttParser, тихий failure)
- [x] 3.2 `SelfScoringState` + открытие карточки в `saveAttempt()`; actions OnToggleSelfCheck /
      OnSaveSelfScore / OnSkipSelfScoring / OnOpenSelfScoring; сброс в OnRestartAttempts

## 4. UI + строки (bd h3l.5)
- [x] 4.1 `SelfScoringCard` на TrainingScreen (плейбек + TranscriptPanel в heightIn + чеклист
      4 критерия + «Готово»/«Пропустить»); сводка «N/4» в AttemptCard; testTags
- [x] 4.2 `Strings.kt`: новые строки (интерфейс + RU + EN)

## 5. Тесты и гейты (bd h3l.5)
- [x] 5.1 `TrainingScreenTest`: карточка после попытки, тогл, save/skip, эталон скрыт без
      субтитров, сводка N/4 (camelCase без бэктиков — грабля №114)
- [x] 5.2 Maestro `speaking_training.yaml`: scroll-ассерты под карточку
- [x] 5.3 Гейты: `:composeApp:desktopTest :composeApp:detekt :composeApp:compileKotlinWasmJs`
      (--no-configuration-cache, по exit code)
- [x] 5.4 Живой прогон: свежий APK на эмуляторе Medium_Phone, Maestro speaking_training EXIT=0;
      Dropshots validate (эталоны не меняются — trainingIdle без попыток)
