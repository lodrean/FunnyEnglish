# 02-execute — bd FunnyEnglish-2oz.2 «DS: обновить tokens.css до v1.3.1»

## Что сделано

1. **`.docs/design-system/tokens.css` → v1.3.1** (канон `tokens.json` v1.3.1, errata утв. владельцем 2026-08-08):
   - dark: `--color-on-primary: #1A2F5E`, `--color-on-secondary: #1A2F5E` (белый на `#8FB3F5`/`#B79EED` = WCAG FAIL);
   - dark: `--color-primary-strong: #8FB3F5` — dark filled-кнопки = dark primary + onPrimary (как в Compose `speakingDarkColorScheme`);
   - удалён `--color-surface-warm` (обе темы) — токена нет в `tokens.json`;
   - ченджлог v1.3.1 в шапке файла.
2. **Потребители tokens.css синхронизированы**:
   - `mockups.html`: `.btn-primary`/`.player-play`/`.video-cta` — `color: var(--color-on-primary)` вместо `#fff` (light не изменился: `#FFFFFF` на `#3B6FD4`); `surface-warm` → `surface-container` (body) и `surface-container-high` (switcher); «tokens v1.3.0» → v1.3.1 (×2).
   - `styleguide.html`: `.btn-filled` — `var(--color-on-primary)`; nav.toc a → `surface-container-high`; th/td border → `outline-variant`; свотч `surfaceWarm` удалён из `swNeutral`.
3. **Тёмные рендеры перегенерированы** — новый скрипт `e2e-cmp/shoot-dark-button-audit.js` (по образцу `shoot-mockups.js`):
   - `docs/qa/design-conformance/mockup-dark-onboarding.png`, `mockup-dark-questions.png` (мокап, `data-theme="dark"`);
   - `docs/qa/design-conformance/wasm-dark-onboarding.png` (приложение: статическая раздача `composeApp/build/wasm-dist`, `colorScheme:'dark'`; backend не нужен — onboarding/guest-first локальные).
4. **Аудит дополнен парой «dark-кнопка мокапа ↔ приложения»** — `docs/qa/design-conformance/DARK_BUTTON_AUDIT_2026-08-30.md`.
5. `memory.md` — запись о решении.

## Проверка (выполнена, пиксельно через pngjs)

- Мокап dark, кнопка «Начать»: фон `#8FB3F5`, текст `#1A2F5E` ✅ (было `#3B6FD4`+белый).
- Приложение wasm dark, кнопка «Далее»: фон `#8FB3F5`, текст `#1A2F5E` ✅.
- Контраст `#1A2F5E` на `#8FB3F5` = 6.16:1 (AA/AAA) ✅.
- `grep surface-warm` по tokens.css-потребителям — чисто (self-contained `docs/design/sotospeak_logo_transition_guide.html` со своим локальным `--surfaceWarm` осознанно не тронут — не использует tokens.css).
- Light-тема не деградировала: `--color-on-primary: #FFFFFF` в `:root` не менялся.

## Изменённые файлы

- `.docs/design-system/tokens.css`
- `.docs/design-system/mockups.html`
- `.docs/design-system/styleguide.html`
- `memory.md`

## Созданные файлы

- `e2e-cmp/shoot-dark-button-audit.js`
- `docs/qa/design-conformance/DARK_BUTTON_AUDIT_2026-08-30.md`
- `docs/qa/design-conformance/mockup-dark-onboarding.png`
- `docs/qa/design-conformance/mockup-dark-questions.png`
- `docs/qa/design-conformance/wasm-dark-onboarding.png`

## Как проверить

```bash
cd e2e-cmp && node shoot-dark-button-audit.js   # перегенерация dark-рендеров
```

Открыть `.docs/design-system/mockups.html`, переключить «◐ Тема» — primary-кнопки `#8FB3F5`+`#1A2F5E`.
Gradle-гейты не затронуты (правок кода нет — только CSS/HTML-артефакты дизайн-системы и документация).
