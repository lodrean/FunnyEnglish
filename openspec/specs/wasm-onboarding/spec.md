# wasm-onboarding Specification

## Purpose
TBD - created by archiving change fix-mvp-acceptance. Update Purpose after archive.
## Requirements
### Requirement: Векторные иллюстрации онбординга на WASM
Onboarding slides MUST display a visible illustration on the WASM canvas target. Illustrations MUST be vector icons from `design/.../icons/SpeakingIcons.kt` (Compose `ImageVector`), not platform emoji or Material icons that rely on font rendering (WASM canvas renders them as empty placeholders). Slide mapping MUST be: «Смотри видео» → `SpeakingIcons.OnboardingWatch`; «Тренируйся вслух» → `SpeakingIcons.OnboardingMic`; «Отправь учителю» → `SpeakingIcons.OnboardingSend`.

#### Scenario: Иллюстрация видна на WASM
- **WHEN** приложение собрано под wasmJs и открыт онбординг
- **THEN** на каждом слайде отрисована непустая векторная иллюстрация (не квадрат-плейсхолдер)

### Requirement: Визуальное оформление иллюстраций
Visual treatment MUST remain: 180dp rounded card (`SpeakingShapes.CardLarge`), `speaking.primaryContainer` background, 88dp icon, `speaking.primary` tint. No text/emoji fallback is required when vector icons are present.

#### Scenario: Стили карточки иллюстрации по токенам
- **WHEN** отрисовывается слайд онбординга на любой платформе
- **THEN** карточка 180dp со скруглением CardLarge, фон primaryContainer, иконка 88dp с tint primary

