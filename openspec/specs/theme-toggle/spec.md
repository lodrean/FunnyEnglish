# theme-toggle Specification

## Purpose
TBD - created by archiving change fix-mvp-acceptance. Update Purpose after archive.
## Requirements
### Requirement: Переключатель темы в admin-web
Theme preference MUST be stored under key `sotospeak-theme-mode` with valid values `light`, `dark`. Default when no stored value MUST be the system preference (`prefers-color-scheme`). A toggle button in `Header.tsx` right section MUST switch mode and icon (`LightMode`/`DarkMode`). Selected mode MUST persist across reloads and apply to the whole MUI theme.

#### Scenario: Переключение темы сохраняется
- **WHEN** пользователь кликает иконку темы в Header и перезагружает страницу
- **THEN** `localStorage['sotospeak-theme-mode']` содержит выбранный режим и вся MUI-тема применяется после reload

### Requirement: Переключатель темы в приложении (CMP)
Theme preference MUST be stored under key `theme_mode` with valid values `system`, `light`, `dark`; default MUST be `system` (respect OS setting). `SettingsScreen`/`ProfileScreen` MUST provide mode selection; `SettingsViewModel` MUST persist it immediately. `MainActivity` MUST read `theme_mode` before `setContent` and set `window.setBackgroundDrawableResource` to the matching `splash_background` color resource (light/dark via `values`/`values-night`). `App.kt` MUST resolve the effective boolean dark value from `theme_mode` and pass it to `SoToSpeakTheme`. Theme selection MUST survive configuration changes (rotation) without reset.

#### Scenario: Splash и первый кадр в выбранной теме
- **GIVEN** пользователь выбрал `dark`
- **WHEN** приложение перезапускается
- **THEN** splash-фон и первый кадр UI оба тёмные (без мелькания светлой темы)

#### Scenario: Поворот экрана не сбрасывает тему
- **WHEN** устройство поворачивается с выбранной темой `light`/`dark`
- **THEN** выбранная тема сохраняется после пересоздания Activity

