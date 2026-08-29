# 02-execute — bd FunnyEnglish-2oz.5: DS: MUI dark-hover по M3 state layers + dark статус-чипы

## Что сделано

1. **Dark containedPrimary/containedSecondary hover → M3 state layer** (`admin-web/src/theme/Theme.ts`, dark `MuiButton`):
   - Было: hover красил кнопку в **светлые** primary `#5B8DEF` / secondary `#9B7EDE` (дефект из описания задачи).
   - Стало: контейнер остаётся dark-токеном (`#8FB3F5` / `#B79EED`), поверх — alpha-оверлей 8% цвета контента (`rgba(22,26,46,0.08)` = onPrimary/onSecondary dark) через `backgroundImage: linear-gradient(...)` — ровно по паттерну light `containedPrimary` (M3 state layer hover 8%, tokens.json `state.hover`).

2. **Dark-токены статусов** (`Theme.ts`, `speakingDark`):
   - Было: `speakingDark` наследовал светлые `status.*` (`#FB8C00/#FFE0B2/#43A047/#C8E6C9`) — чипы в dark-теме оставались светлыми.
   - Стало: `status` переопределён dark-значениями **1:1 с composeApp `DarkSpeakingColors`** (`SpeakingTokens.kt:122-125`): `new #FFB74D`, `newContainer #3D2A0A`, `reviewed #81C784`, `reviewedContainer #1B4D1F` — единые токены статус-чипов обоих клиентов. composeApp не тронут: его чипы уже читают эти токены.

3. **Единый StatusChip в admin-web** (новый `src/components/speaking/StatusChip.tsx`):
   - Три разрозненные реализации статус-чипа заменены одним компонентом на токенах `theme.palette.speaking.status`: container-фон + `text.primary` (WCAG AA в обеих темах — акцентный/белый текст на warning/success = FAIL).
   - Подключён в `GradingInbox.tsx` (функция `statusChip`), `GradingDetail.tsx` (`data-testid="submission-status-chip"` сохранён), `GradingNavBadge.tsx` (бейдж «N new», `data-testid="grading-new-badge"` сохранён).
   - Побочно устранён хардкод `#8a5200` (GradingDetail) и `#FFE0B2/#8a5200` (GradingNavBadge) — в dark-теме давали нечитаемый текст на новом тёмном контейнере `#3D2A0A`.

## Изменённые/созданные файлы

- `admin-web/src/theme/Theme.ts` — dark hover state layers + `speakingDark.status`.
- `admin-web/src/components/speaking/StatusChip.tsx` — **новый** единый компонент.
- `admin-web/src/screens/GradingInbox.tsx` — на StatusChip; убраны неиспользуемые `Chip`/`useTheme`.
- `admin-web/src/screens/GradingDetail.tsx` — на StatusChip; убраны неиспользуемые `Chip`/`useTheme`/`theme`.
- `admin-web/src/components/speaking/GradingNavBadge.tsx` — на StatusChip.

Спеки/PRD (`docs/`, `openspec/`) и design-токены (`.docs/design-system/tokens.*`) не тронуты.

## Как проверить

- `cd admin-web && npx tsc --noEmit` → **exit 0** (прогнано после правок).
- Витест/Playwright/визуальные базлайны — на драйвере (`npx vitest run`, `npm run test:e2e`); цветовые хардкоды в тестах отсутствуют (проверено grep по `8a5200`/`FFE0B2`/`newContainer`).
- Ручная сверка: админка в dark-теме → hover primary/secondary-кнопок слегка затемняет (оверлей 8%), чипы NEW/REVIEWED в Grading — тёмные контейнеры с читаемым текстом.

## Заметки владельцу (не блокеры)

- Dark-значения `status.*` существуют только в коде (composeApp + теперь admin-web); в `.docs/design-system/tokens.json|css` dark-секции статусов нет — предложить follow-up на внесение в токены (как errata v1.3.1), т.к. это правка design-артефакта, требующая согласования.
