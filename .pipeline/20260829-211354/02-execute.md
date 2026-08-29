# 02-execute — bd FunnyEnglish-9bo.5: ADM: подключить ErrorBoundary в App.tsx

## Что сделано

ErrorBoundary (`src/components/feedback/ErrorBoundary.tsx`, уже написан и логирует в `utils/logger` через `componentDidCatch` → `logger.error('ErrorBoundary', ...)`) смонтирован на двух уровнях:

1. **App-уровень (App.tsx)** — `<ErrorBoundary>` оборачивает `RouteValidator` + `AppInitializer` + `Routes` (внутри `ThemeProvider`/`ToastProvider`, чтобы fallback рендерился в теме). Краш роутов/инициализации больше не даёт белый экран (memory №42) — показывается fallback с кнопкой «Try Again».
2. **Уровень роута (AdminLayout.tsx)** — `<ErrorBoundary key={location.pathname}>` оборачивает `<Outlet />`. Краш одной страницы не роняет header/sidebar; `key` по pathname автоматически сбрасывает boundary при переходе на другой роут.
3. **Баррел feedback/index.ts** — добавлены экспорты `ErrorBoundary`, `ErrorFallback`, `withErrorBoundary`, тип `ErrorFallbackProps` (раньше компонент был недостижим через barrel и использовался только в одном story).

Логирование дополнительно не потребовалось: `componentDidCatch` уже пишет в `logger.error` (in-memory буфер → `POST /api/public/logs`, OpenSpec add-client-logging) + console.error со stack.

## Изменённые файлы

- `admin-web/src/App.tsx` — импорт + top-level ErrorBoundary вокруг роутов
- `admin-web/src/components/layout/AdminLayout.tsx` — импорт `useLocation` + route-level ErrorBoundary вокруг `<Outlet/>` с key по pathname
- `admin-web/src/components/feedback/index.ts` — экспорты ErrorBoundary/ErrorFallback/withErrorBoundary

Созданных файлов нет.

## Примечание про ErrorDisplay.tsx

`src/components/ErrorDisplay.tsx` — это НЕ crash-fallback, а презентационный компонент для отображения API/validation-ошибок (Alert с fieldErrors) в формах. Глобально «смонтировать» его невозможно — нужны точки вызова в экранах/формах. Подключение к конкретным формам = массовая правка экранов, вне рамок quick win; при необходимости — отдельная задача bd.

## Как проверить (гейты драйвера)

```bash
cd admin-web
npm run typecheck      # tsc --noEmit
npx vitest run
```

Ручная проверка: временно бросить `throw new Error('test')` в рендере любой страницы (например, Dashboard) → вместо белого экрана fallback с иконкой ошибки и «Try Again»; header/sidebar остаются; переход по меню на другой роут сбрасывает ошибку. В консоли/на backend (`GET` client-логов, страница /logs) — запись с тегом `ErrorBoundary`.

## Сборки/тесты

Не запускались (гейты прогоняет драйвер).
