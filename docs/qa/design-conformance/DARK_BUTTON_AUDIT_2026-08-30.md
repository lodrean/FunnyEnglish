# Dark primary-кнопка: мокап ↔ приложение (tokens.css v1.3.1)

> Дата: 2026-08-30 · bd `FunnyEnglish-2oz.2` · Источник: `docs/research/PROJECT-REVIEW-2026-08-28.md` §3.1 «Важно».
> До фикса: тёмные мокапы рисовали primary-кнопки `#3B6FD4` + белый текст (light primaryStrong, унаследованный в dark),
> а приложение по errata v1.3.1 — `#8FB3F5` + `#1A2F5E`. Эталон ≠ продукт по самому заметному элементу.

## Фикс

- `.docs/design-system/tokens.css` → **v1.3.1**: dark `onPrimary`/`onSecondary` = `#1A2F5E`, dark `primaryStrong` = `#8FB3F5`;
  удалён `--color-surface-warm` (отсутствует в каноне `tokens.json`).
- `mockups.html` / `styleguide.html`: filled-кнопки — `color: var(--color-on-primary)` вместо хардкода `#fff`;
  потребители `surface-warm` переведены на канонические `surface-container*` / `outline-variant`.

## Пара скриншотов (dark)

| Сторона | Файл | Кнопка | Фон (пиксельно) | Текст (пиксельно) |
|---|---|---|---|---|
| Мокап (frame-onboarding) | `mockup-dark-onboarding.png` | «Начать» | `#8FB3F5` | `#1A2F5E` |
| Мокап (frame-questions) | `mockup-dark-questions.png` | «Тренировка · 3 попытки» | `#8FB3F5` | `#1A2F5E` |
| Приложение (wasm, dark) | `wasm-dark-onboarding.png` | «Далее» (onboarding) | `#8FB3F5` | `#1A2F5E` |

**WCAG:** `#1A2F5E` на `#8FB3F5` = **6.16:1** — AA (и AAA для обычного текста). Белый на `#8FB3F5` был бы ~2.2:1 FAIL (причина errata v1.3.1).

## Как перегенерировать

```bash
cd e2e-cmp && node shoot-dark-button-audit.js
```

Мокап рендерится из `file://.docs/design-system/mockups.html` с `data-theme="dark"`.
Приложение — статическая раздача `composeApp/build/wasm-dist` (порт 8093) с эмуляцией
`prefers-color-scheme: dark`; backend не нужен (onboarding/guest-first — локальные).
Для свежего бандла приложения предварительно: `./gradlew :composeApp:wasmJsBrowserDistribution`.
