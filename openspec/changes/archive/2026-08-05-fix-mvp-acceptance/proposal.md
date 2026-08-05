# Proposal: Исправления MVP-приёмки — admin login, theme toggle, WASM onboarding

## Что меняем
1. **Backend admin login**: сделать так, чтобы креды из `docker-compose.yml` и README (`admin@sotospeak.com / admin123`) работали out-of-the-box после `docker compose up -d`.
2. **Theme toggle**: довести до рабочего состояния переключатель светлой/тёмной темы в admin-web (Header) и в приложении (Settings), с сохранением выбора и переживанием поворота/system splash.
3. **WASM onboarding**: заменить иллюстрации слайдов на векторные иконки из `SpeakingIcons.kt`, чтобы они отображались в WASM canvas.

## Почему сейчас
Блокируют ручную приёмку MVP: нельзя зайти в admin-web, не видно онбординг в web, тема не переключается корректно.

## Non-goals
- Редизайн экранов.
- iOS/Desktop/WASM-реализации записи/видео.
- Новые фичи вне трёх заявленных проблем.

## Связанные бисеры
- TBD: `bd create` после approve.
