# 02-execute — FunnyEnglish-9bo.3: ADM Settings — реальный save или скрыть экран

## Решение

Выбран вариант **«скрыть экран»** из двух, предложенных в задаче.

**Почему не «реальный save»:** экран Settings был полностью моковым — ни одна из его секций
(General/Email SMTP/Notifications/Theme) не имеет backend-контракта. Единственный реальный
эндпоинт `GET /admin/settings` (`AdminSettingsService`) отдаёт read-only инфра-конфиг
(S3 endpoint/bucket/region, лимиты multipart, CORS origins) и не имеет PUT/POST-аналога.
Реальный save = новый API-контракт + persistence (таблица/миграция Flyway) + обновление спеки
→ требует решения владельца по ADR-007 (human-in-the-loop), самостоятельно не реализуемо.

## Что сделано

Экран Settings полностью удалён из admin-web (мок saveSettings = setTimeout + ложный
«Settings saved successfully!», анти-брендовые опции primaryColor/Roboto/radius 8 ушли вместе с ним):

1. **Удалён** `admin-web/src/screens/Settings.tsx` (747 строк мок-экрана).
2. `admin-web/src/screens/index.ts` — убран экспорт `Settings`.
3. `admin-web/src/App.tsx` — убран импорт `Settings` и роут `/settings` (теперь `/settings`
   попадает в catch-all → редирект на `/login`/`/`).
4. `admin-web/src/components/navigation/navItems.ts` — убран пункт меню `settings`
   (+ неиспользуемый импорт `SettingsIcon`).
5. `admin-web/src/components/layout/Header.tsx` — убран пункт «Settings» из user-меню
   (вести было некуда) + неиспользуемый импорт `SettingsIcon`.
6. `admin-web/src/components/navigation/RouteValidator.tsx` — `/settings` убран из VALID_ROUTES.
7. `admin-web/e2e/tests/navigation/direct-url-access.spec.ts` — `/settings` убран из списка
   защищённых роутов (иначе тест падал бы: роут больше не существует).
8. `admin-web/e2e/overflow-audit.cjs` — `/settings` убран из PAGES аудит-скрипта.

## Осознанно НЕ тронуто

- `admin-web/src/api/client.ts` — `getAdminSettings()` + тип `AdminSettings` оставлены:
  это биндинг к **реальному** backend-эндпоинту `GET /admin/settings` (живой, отдаёт
  инфра-конфиг). Сейчас не используется UI, но удаление не требуется для задачи.
- Backend (`AdminController`, `AdminSettingsService`) — без изменений.
- `e2e/MCP_EXAMPLES.md` — упоминание Settings в примере документации (не тест).
- `PermissionEditor.tsx` (`settings.view`/`settings.edit`) и `GroupManager.tsx` (вкладка
  Settings группы) — относятся к правам/группам, не к удалённому экрану.

## Требуется решение владельца (ADR-007)

Если нужен **реальный** экран настроек: спека/PRD должна описать состав настроек и контракт
(например, `PUT /admin/settings` + таблица `admin_settings`), после согласования — backend
(entity, Flyway-миграция, эндпоинт) и новый экран на react-hook-form + TanStack Query.
До этого решения экран скрыт.

## Как проверить

- `cd admin-web && npm run typecheck` — ошибок быть не должно (гейты прогоняет драйвер).
- `cd admin-web && npx vitest run` — тесты не ссылаются на Settings (проверено grep'ом).
- Ручная проверка: в сайдбаре и user-меню нет пункта Settings; прямой заход на `/settings`
  редиректит на `/` (catch-all).
- E2E: `e2e/tests/navigation/direct-url-access.spec.ts` обновлён под новый список роутов.

## Изменённые/удалённые файлы

- D `admin-web/src/screens/Settings.tsx`
- M `admin-web/src/screens/index.ts`
- M `admin-web/src/App.tsx`
- M `admin-web/src/components/navigation/navItems.ts`
- M `admin-web/src/components/navigation/RouteValidator.tsx`
- M `admin-web/src/components/layout/Header.tsx`
- M `admin-web/e2e/tests/navigation/direct-url-access.spec.ts`
- M `admin-web/e2e/overflow-audit.cjs`
