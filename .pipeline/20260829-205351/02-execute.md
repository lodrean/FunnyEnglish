# 02-execute — FunnyEnglish-9bo.2: ADM: Analytics — реальные данные или empty state

## Что сделано

Экран `Analytics` полностью переведён с моков на реальные данные backend:

- **Удалён мок** `fetchAnalytics` (setTimeout(800) + хардкод 12 458 пользователей, выдуманные topTests/topUsers/scoreDistribution/userDistribution) и фейковые тренды «12.5% vs last period».
- **Метрики** — из `GET /admin/analytics` (`getAdminAnalytics`): Total Users, Tests Completed (all time), Published Tests (subtitle — total).
- **User Activity (AreaChart)** — из `GET /admin/analytics/daily-activity?days=N` (`getAdminDailyActivity`): New Users + Tests Completed. Backend принимает только «последние N дней», поэтому запрашивается покрывающий диапазон (`daysNeeded` от выбранной start-даты, cap 365), а точный `[start, end]` фильтруется на клиенте (ISO-даты сравниваются строками) — **выбор дат теперь реально влияет на график**. Дефолтный диапазон — последние 14 дней (вместо захардкоженного 2024-01-01).
- **PieChart** — «Score Distribution» (мок) заменён на **User Level Distribution** из `GET /admin/analytics/levels` (`getAdminLevelDistribution`, ранее не использовался).
- **BarChart** — «Test Performance» (мок) заменён на **Popular Tests** (completions) из `GET /admin/analytics/popular-tests` (`getPopularTests`).
- **Таблица Top Performing Tests** — реальные `PopularTest` (Name / Category / Completions); колонки Completion/Avg Score удалены (backend их не отдаёт).
- **Таблица Most Active Users** (выдуманные люди) заменена на **Recent Activity** из `GET /admin/analytics/recent-activity` (`getRecentActivity`): user / type chip (NEW_USER/TEST_COMPLETED/ACHIEVEMENT) / details / timestamp.
- **Export** — вместо `console.log` реальное скачивание CSV (Blob): метрики + daily activity за выбранный диапазон + popular tests + level distribution; CSV-экранирование значений. Пункт «Export as PDF» (был фейком) удалён.
- **Empty states**: при пустых данных backend — осмысленные заглушки («No activity data for the selected period», «No completed tests yet», «No recent activity yet», `data-testid="analytics-empty-state"` для графиков) вместо пустых графиков/таблиц.
- Секция Guest Users (уже была на реальных данных) — без изменений.

В тип `PopularTest` (`src/types/index.ts`) добавлено поле `category: string` — backend (`PopularTestResponse`) его уже возвращает, TS-тип отставал от контракта. Других конструкторов `PopularTest` в коде нет.

## Изменённые файлы

- `admin-web/src/screens/Analytics.tsx` — переписан (моки → реальные запросы через TanStack Query)
- `admin-web/src/types/index.ts` — `PopularTest` + `category: string`

## Как проверить

- Гейты драйвера: `cd admin-web && npm run typecheck` и `npx vitest run` (сам не запускал — по инструкции гейты прогоняет драйвер).
- Вручную: `npm run dev`, страница Analytics (/reports, /statistics) — метрики/графики/таблицы из реального API; смена Start/End Date меняет график User Activity; Export → скачивается CSV; при пустой БД — empty states вместо данных.
- Backend-контракты сверены с `AdminController.kt`/`AdminDto.kt`/`AdminService.kt` (daily-activity — только `days`, точный диапазон фильтруется на клиенте).

## Замечания

- Спеки/PRD не трогал (ADR-007). Эндпоинтов для «score distribution», «avg score by test» и «top users» в backend нет — соответствующие блоки заменены реальными данными (levels/popular tests/recent activity), а не новыми моками. Если нужны именно те метрики — требуется решение владельца + backend-задача.
