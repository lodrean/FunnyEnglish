# 02-execute — bd FunnyEnglish-9bo.6: ADM конфиг-мины playwright и .env

## Что сделано

1. **Грабля №11 (playwright webServer) исправлена.** `vite.config.ts` слушает порт 3000,
   а `playwright.config.ts` ждал vite на 5173 → `npm run test:e2e` падал по таймауту webServer.
   Выбран вариант «порт 3000» (не `--port 5173 --strictPort`), т.к. 3000 — канонический порт
   admin-web по memory.md и docker-compose:
   - `webServer.url`: `http://localhost:5173` → `http://localhost:3000`;
   - дефолт `use.baseURL`: `http://localhost:5173` → `http://localhost:3000`
     (та же мина: иначе тесты навигировали бы на 5173, где ничего не слушает;
     `ADMIN_URL` env по-прежнему переопределяет).
2. **Закоммиченный `.env`**: `VITE_API_URL=http://localhost:8080` → `VITE_API_URL=/api`.
   Vite dev-прокси (`vite.config.ts` server.proxy `/api` → `http://localhost:8080`) обеспечивает
   префикс `/api` (context-path backend, грабля №9) — свежий клон больше не получает 404.
   Значение совпадает с `.env.local` и дефолтом `src/api/client.ts`.
3. **`.env.example`**: `ADMIN_URL` и комментарий 5173 → 3000 (документирует тот же дефолт baseURL).
4. **memory.md**: грабля №11 помечена исправленной (2026-08-29, bd 9bo.6) с описанием фикса.

## Изменённые файлы

- `admin-web/playwright.config.ts` (baseURL дефолт + webServer.url → 3000)
- `admin-web/.env` (VITE_API_URL=/api)
- `admin-web/.env.example` (ADMIN_URL → 3000)
- `memory.md` (грабля №11 — отметка об исправлении)

## Как проверить

```bash
cd admin-web
npm run test:e2e            # webServer поднимается на 3000, без таймаута
npm run dev                 # открыть http://localhost:3000 — API-запросы идут на /api (vite proxy)
grep -c 5173 playwright.config.ts   # → 0
```

Гейты (`npm run typecheck`, `npx vitest run`) прогоняет драйвер — сам не запускал.
Спеки/PRD не затронуты. Git-коммитов/пушей не делал.
