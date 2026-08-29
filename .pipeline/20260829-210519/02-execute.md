# 02-execute — bd FunnyEnglish-9bo.4: ADM Users — убрать тихий ложный успех

## Что сделано

Backend поддерживает только `GET /admin/users` (параметры `q`, `role`) и `GET /admin/users/{id}` — create/update/delete отсутствуют (`AdminUserController.kt`). Поэтому выбран вариант из описания задачи: **disable действий + серверный поиск/фильтр** (новых backend-эндпоинтов не добавлял — это потребовало бы решения владельца/спеки, ADR-007).

1. **Удалены мёртвые мутации-заглушки** `createUser`/`_updateUser` (бросали "not implemented") и `deleteUser`/`bulkDeleteUsers` (только `console.log`, но резолвились → ложный onSuccess, диалог закрывался «успешно»). Вместе с ними удалены `createMutation`/`deleteMutation`/`bulkDeleteMutation` и связанное состояние (`formData`, `editingUser`, `deleteDialogOpen`, `userToDelete`, `selectedUsers`, `bulkDeleteDialogOpen`, `UserFormData`).
2. **Убраны действия из UI**: кнопка «Add User», row-actions Edit/Delete, чекбокс-выбор (`selectable`/`onSelectionChange`) и тулбар «Delete Selected», диалоги подтверждения удаления (одиночного и bulk).
3. **Drawer переведён в read-only «User Details»**: поля disabled, плашка `Alert info` «Editing users is not supported by the API yet», единственная кнопка — Close. Добавлен `data-testid="user-details-title"`.
4. **Серверный поиск и фильтр по роли**: `fetchUsers(search, role)` передаёт `query`/`role` в существующий `getAdminUsers({query, role})` (параметры `q`/`role` уже поддерживаются backend'ом); маппинг UI-ролей → API (`student→USER`, `instructor→TEACHER`, `admin→ADMIN`); debounce 300 мс (`useDebouncedValue`, паттерн как в `GradingInbox.tsx`); queryKey `['users', debouncedSearch, filters.role]`. Фильтр статуса остался клиентским (у API нет поля status).
5. **E2E Page Object синхронизирован**: `UserDetailsPage` больше не ждёт кнопку «Update» (её нет), а ждёт заголовок read-only drawer `[data-testid="user-details-title"]`.

Рабочие функции страницы не тронуты: список/колонки, Export CSV (реальные данные), диалог «Message to Student» (реальный API сообщений), фильтр статуса.

## Изменённые файлы

- `admin-web/src/screens/Users.tsx` — основной фикс (867 → ~650 строк).
- `admin-web/e2e/pages/UsersPage.ts` — `UserDetailsPage` под read-only drawer.

Созданных файлов нет. Спеки/PRD не тронуты. Backend не тронут.

## Как проверить

Гейты (прогоняет драйвер): `cd admin-web && npm run typecheck` и `npx vitest run`.

Ручная проверка (dev-стек, admin@sotospeak.com):
1. Открыть `/users` — кнопки «Add User» нет, в таблице нет чекбоксов и row-actions Edit/Delete, осталась только колонка Message.
2. Ввести текст в «Search users...» — запрос уходит на `GET /api/admin/users?q=<текст>` (видно в Network), список фильтруется сервером.
3. Выбрать Role=Admin — запрос `GET /api/admin/users?role=ADMIN`.
4. Клик по строке — открывается drawer «User Details» с disabled-полями и плашкой «Editing users is not supported by the API yet», без кнопок сохранения.
5. E2E: `SKIP_WEB_SERVER=1 ADMIN_URL=http://localhost:3000 npx playwright test e2e/tests/users.spec.ts`.

## Out of scope (отмечено в аудите К4, но не входит в эту задачу)

- Колонки Avg Score / Time Spent / Status пока показывают хардкод (`0` / `0m` / `active`) — у API нет этих полей; нужно решение владельца (скрыть колонки или добавить поля на backend).
- Серверная пагинация: backend `GET /admin/users` возвращает полный список без page/size — требует backend-изменения (отдельная задача/решение владельца).
