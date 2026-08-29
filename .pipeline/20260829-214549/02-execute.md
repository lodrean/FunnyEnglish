# 02-execute — bd FunnyEnglish-nj2.5: SEC demo-креды в admin Login.tsx

## Что сделано

В `admin-web/src/screens/Login.tsx` внесены 2 точечные правки:

1. **SEC: блок «Demo credentials: admin@sotospeak.com / admin123» спрятан за `import.meta.env.DEV`**
   (бывшие строки 176–187). В prod-сборке (`vite build`, `DEV === false`) блок не рендерится —
   утечка demo-кредов устранена. В dev (`vite`) подсказка остаётся.
2. **Фон экрана логина (light-режим): `#F5F5F5` → `#EEF3FF`** (строка ~73) — brand-фон из
   палитры Playful Coach v1.1 (tokens.json / admin-web Theme.ts `background.default`).
   Dark-режим (`#121212`) не тронут.

Никаких других файлов/стилей/логики не менялось. Спеки/PRD не затрагивались — правка
не требует решения владельца (ADR-007 не применим).

## Изменённые файлы

- `admin-web/src/screens/Login.tsx` — единственный изменённый файл.

## Как проверить

- Dev: `cd admin-web && npm run dev` → на `/login` блок «Demo credentials» виден, фон `#EEF3FF`.
- Prod: `npm run build && npm run preview` → блок demo-кредов отсутствует; в бандле можно
  дополнительно убедиться, что строки `admin123` нет: `grep -r "admin123" dist/assets` (не найдёт).
- Типы: `import.meta.env` типизирован через `src/vite-env.d.ts` (`vite/client`), tsc-gate не сломан.
- Гейты (vitest/e2e/tsc) прогоняет драйвер — самостоятельно не запускались (по ограничениям задачи).

## Замечания

- `import.meta.env.DEV` в Vite в prod-сборке подставляется литералом `false`, ветка вырезается
  минификатором (dead-code elimination) — текст кредов физически не попадёт в бандл.
