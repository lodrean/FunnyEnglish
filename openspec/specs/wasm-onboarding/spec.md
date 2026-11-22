# wasm-onboarding Specification

## Purpose
Онбординг на WASM (canvas-only): иллюстрации слайдов — живые превью продукта, собранные
только из векторных ассетов дизайн-системы (`SpeakingIcons` ImageVector'ы, Canvas), без
платформенных emoji и Material font-иконок (WASM-канвас рендерит их пустыми
плейсхолдерами). Гостевые гейты — без emoji.

## Requirements
### Requirement: Векторные иллюстрации онбординга на WASM
Onboarding slides MUST display a visible live preview on every target (WASM canvas included).
The preview MUST be composed from vector assets only (`SpeakingIcons` ImageVectors, Canvas draws)
— no platform emoji and no Material font icons. Slide mapping MUST be:
«Смотри видео» → `VideoPreview` (мини-плеер); «Тренируйся вслух» → `TrainingPreview`
(мини-Training); «Отправь учителю» → `GradePreview` (мини-оценка).

#### Scenario: Превью видно на WASM
- **WHEN** приложение собрано под wasmJs и открыт онбординг
- **THEN** на каждом слайде отрисовано живое превью (тёмная карточка плеера / таймер-кольцо
  с rec-кнопкой / карточка оценки) без пустых плейсхолдеров

### Requirement: Визуальное оформление иллюстраций
Превью MUST использовать токены v1.3.1 (маппинг по DESIGN_SYSTEM_SPEC §1.1) и роли бренд-компонентов
B1–B3/B15: превью видео — тёмная карточка (`inverseSurface`-тёмный контейнер плеера), белая круглая
play-кнопка, CC-бейдж, персиковая полоса прогресса (`record`), строка транскрипта с mark-подсветкой
(`recordContainer`/`onRecordContainer`); превью тренировки — чип уровня `timer.level80`,
таймер-кольцо (трек `surfaceContainerHigh`, дуга `timer.level80`), squircle rec-кнопка `record`
с фирменной тенью, waveform `record`/`secondary`; превью оценки — score `primaryStrong`,
4 бара критериев (`primary` на `surfaceContainerHigh`), зелёная плашка `success`-пары.
Тексты слайдов, пейджер и CTA («Далее»/«Начать») MUST остаться без изменений.

#### Scenario: Превью собрано из токенов
- **WHEN** отрисовывается слайд онбординга в светлой или тёмной теме
- **THEN** превью использует только токен-значения (нет новых HEX) и сохраняет читаемость в обеих темах

### Requirement: Гейты без emoji
`SpeakingGate` MUST NOT render platform emoji. Все гостевые гейты (GuestProfileStub, LoginScreen,
RegisterScreen) MUST использовать векторную иконку `SpeakingIcons.Send` на круге
`secondaryContainer` с tint `onSecondaryContainer`. Контракты и тексты гейтов MUST остаться
без изменений.

#### Scenario: Гостевой профиль без emoji
- **WHEN** гость открывает вкладку «Профиль»
- **THEN** стаб показывает векторную иконку Send в цветном круге (не 📬), кнопки и тексты прежние

## Changelog
- **2026-09-12 (V2)** — из дельты `add-speaker-path-redesign-v2` (bd эпик `FunnyEnglish-txx`,
  редизайн «Путь спикера» V2, DESIGN_SYSTEM_SPEC v3.3.0): иллюстрации слайдов заменены на
  живые превью (VideoPreview/TrainingPreview/GradePreview), оформление по токенам v1.3.1/B15;
  добавлено требование «Гейты без emoji» (SpeakingGate → SpeakingIcons.Send).
- **2026-08-05** — первоначальная версия (векторные иллюстрации SpeakingIcons вместо emoji).
