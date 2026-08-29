# 02-execute — bd FunnyEnglish-9bo.1: ADM Dashboard — убрать вымышленные метрики

## Итог

Правки уже были полностью внесены предыдущим прогоном пайплайна `.pipeline/20260829-204325/`
(незакоммиченные изменения в working tree). В этом прогоне проведена **верификация
полноты и консистентности** — дополнительных правок не потребовалось. Файл в состоянии
«готово к гейтам драйвера».

## Проверено (соответствие задаче)

1. **Вымышленные метрики удалены** из `admin-web/src/screens/Dashboard.tsx`:
   - хардкод `completionRate: 78.5` / `avgSessionTime: 24.3` — отсутствует;
   - `userGrowth = totalUsers − (7−index−1)×50` (уходил в минус) — график удалён вместе с
     `LineChart/Line/Legend` и `getAdminDailyActivity`;
   - моковый `testCompletions` (Basic Grammar и т.п.) и фейковые дельты/sparkline на карточках — удалены.
2. **Реальные speaking-метрики** (только существующие API, backend не тронут):
   - Карточки: Total Students (`getAdminAnalytics().totalUsers`), Active Students (7d)
     (distinct `student.id` по отправкам за 7 дней, cap 100), Practice Submissions
     (`getSubmissions().totalElements`), Pending Review (`status=NEW`).
   - Графики: Submissions per Day (7× `getSubmissions({from,to,size:1})` → точный
     `totalElements` на день) и Grading Status (NEW vs REVIEWED).
   - Контракт сверен с `src/api/speakingApi.ts`: `SubmissionFilters{status,from,to,size}` ✔,
     `SpeakingSubmission.student.id` ✔; `StatsCard` принимает `variant`/`loading`,
     `change`/`chartData` опциональны ✔.
3. **Честный empty state**: при `totalSubmissions === 0` оба графика показывают текст-заглушки
   («No practice submissions yet…», «No submissions to grade yet») вместо выдуманных данных.
4. **Ошибка не глотается**: try/catch с fallback-нулями удалён — ошибка пробрасывается в
   `useQuery`, рендерится существующий error-Alert с кнопкой Retry.
5. **Legacy-термины переименованы**: «Total Tests»/«Completion Rate»/«Avg Session»/
   «User Growth»/«Test Completions» → Students/Submissions/Review.
6. Grep по `admin-web/src`: остатков `completionRate/avgSessionTime/userGrowth/testCompletions`
   в Dashboard нет (совпадения — только в `screens/Analytics.tsx`, это отдельная задача/scope,
   и в неиспользуемом Dashboard'ом `getAdminDailyActivity` в `api/client.ts` + его тесте —
   удаление не входит в quick win).

## Изменённые файлы

- `admin-web/src/screens/Dashboard.tsx` (изменения внесены прогоном 20260829-204325, в этом прогоне — только верификация, дельта-правок нет)

## Как проверить

```bash
cd admin-web
npm run typecheck      # tsc --noEmit (гейт драйвера)
npx vitest run         # unit (гейт драйвера)
```

Ручная проверка: `npm run dev` → Dashboard — 4 карточки с реальными числами, графики
submissions/day и Pending/Reviewed; при пустой БД — empty state; при выключенном
backend — error-Alert вместо нулей.

## Замечания для драйвера / владельца

- **Visual regression**: скриншот-базлайны Dashboard (e2e/tests/visual/) устареют — UI
  изменён намеренно, базлайны нужно переснять.
- Unit-тестов и Storybook-stories на Dashboard нет — не добавлялись (минимальные изменения).
- «Active Students (7d)» — оценка по последним ≤100 отправкам (backend cap size=100);
  точная метрика потребует агрегирующий эндпоинт (аудит F-1) — backend-задача/решение владельца.
- Моки в `screens/Analytics.tsx` (completionRate/topTests) — вне scope этой задачи.
- Спеки/PRD не тронуты (ADR-007): правок спеки задача не потребовала.
