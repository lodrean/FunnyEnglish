# 02-execute — bd FunnyEnglish-9bo.1: ADM Dashboard — убрать вымышленные метрики

## Что сделано

Переработан `admin-web/src/screens/Dashboard.tsx` (единственный изменённый файл) по аудиту D-1 (docs/research/PROJECT_AUDIT_2026-08-29.md):

1. **Удалены вымышленные метрики**:
   - `completionRate: 78.5`, `avgSessionTime: 24.3` (хардкод) — убраны;
   - `userGrowth = totalUsers − (7−index−1)×50` (уходил в минус) — график удалён;
   - моковый массив `testCompletions` (Basic Grammar и т.п.) — удалён;
   - хардкод-дельты `change: {value: 12.5/8.2/3.1/5.4, ...}` и фейковые sparkline `chartData` на StatsCard — убраны (пропсы опциональны).
2. **Реальные speaking-метрики** (все из существующих API, backend не тронут):
   - Карточки: **Total Students** (`getAdminAnalytics().totalUsers`), **Active Students (7d)** (distinct `student.id` среди отправок за 7 дней, cap 100 — см. комментарий в коде), **Practice Submissions** (`getSubmissions().totalElements`), **Pending Review** (`status=NEW`).
   - График **Submissions per Day (last 7 days)**: 7 запросов `getSubmissions({from,to,size:1})` → точный `totalElements` на день (backend принимает `dateFrom/dateTo` как LocalDate, UTC-границы).
   - График **Grading Status**: Pending (NEW) vs Reviewed (REVIEWED) — реальные totals.
3. **Честный empty state**: при `totalSubmissions === 0` оба графика показывают текст-заглушку вместо выдуманных данных.
4. **Ошибка больше не глотается**: try/catch с fallback-нулями удалён — ошибка пробрасывается в `useQuery`, показывается существующий error-Alert с кнопкой Retry.
5. **Legacy-термины переименованы**: «Total Tests»/«Completion Rate»/«Avg Session»/«User Growth»/«Test Completions» → speaking-метрики (Students/Submissions/Review).
6. Чистка импортов: убраны `getAdminDailyActivity`, `DailyActivity`, `LineChart/Line/Legend`, `TestIcon`, `useMediaQuery/isMobile`; добавлены `getSubmissions` (из `api/speakingApi`), `MicIcon`, `Cell` (recharts).

## Изменённые файлы

- `admin-web/src/screens/Dashboard.tsx`

## Как проверить

```bash
cd admin-web
npm run typecheck      # tsc --noEmit (гейт драйвера)
npx vitest run         # unit (гейт драйвера)
```

Ручная проверка: `npm run dev` → Dashboard (admin-логин) — 4 карточки с реальными числами, графики submissions/day и Pending/Reviewed; при пустой БД — empty state; при выключенном backend — error-Alert вместо нулей.

## Замечания для драйвера / владельца

- **Visual regression**: скриншот-базлайны Dashboard (e2e/tests/visual/visual-regression.spec.ts) устареют — UI изменён намеренно, базлайны нужно переснять.
- E2E dashboard.spec.ts / DashboardPage.ts метрики не проверяют (только заголовок/сайдбар/карточки) — правок не требуют.
- Unit-тестов и Storybook-stories на Dashboard нет — добавлять не стал (минимальные изменения).
- «Active Students (7d)» — оценка по последним ≤100 отправкам (backend cap size=100); точная метрика потребует агрегирующий эндпоинт (см. аудит F-1) — это уже backend-задача/решение владельца, в scope quick win не входило.
- Спеки/PRD не тронуты (ADR-007): правок спеки задача не потребовала.
