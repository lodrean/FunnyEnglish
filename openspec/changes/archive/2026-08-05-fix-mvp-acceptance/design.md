# Design: Исправления MVP-приёмки

## Admin login
- `application.yml` дефолт `app.admin.email` = `admin@sotospeak.com` (совпадает с docker-compose и README).
- `AdminUserInitializer` ищет существующего admin сначала по `adminEmail`, затем fallback по `role = "ADMIN"`.
- При нахождении обновляет email, пароль, role, `emailVerified=true` одним `save`.
- При отсутствии — создаёт нового.

## Theme toggle
- **Admin-web**: `ThemeProvider` уже оборачивает приложение; toggle в `Header` уже вызывает `toggleTheme`. Добавляем E2E-проверку.
- **App**:
  - `SettingsViewModel` сохраняет `theme_mode` (`system`/`light`/`dark`) и читает его при старте.
  - `MainActivity` до `setContent` устанавливает системный splash-фон по сохранённому режиму.
  - `App.kt` передаёт `darkTheme = resolvedTheme()` в `SoToSpeakTheme`, где `resolvedTheme()` читает `theme_mode` из `Settings` и маппит `system` на `isSystemInDarkTheme()`.
  - Поворот Activity: `ViewModel`/Settings сохраняют состояние, Compose пересоздаётся с тем же режимом.

## WASM onboarding
- В `OnboardingScreen.kt` заменяем `Icons.Filled.PlayCircle/Mic/Send` на три новых `ImageVector` из `design/.../icons/SpeakingIcons.kt` (`OnboardingWatch`, `OnboardingMic`, `OnboardingSend`).
- Размер, цвет и контейнер остаются по мокапу (180dp карточка, 88dp иконка, `speaking.primary`).
- Вектор рендерится Compose canvas без зависимости от системных шрифтов.
