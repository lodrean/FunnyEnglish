# 02-execute: INF — архивировать openspec add-video-transcript-highlight (bd FunnyEnglish-qbq.4)

## Что сделано

Change `add-video-transcript-highlight` был реализован, но не заархивирован
(источник: PROJECT_AUDIT_2026-08-29 F-4). Архивация выполнена стандартной командой
OpenSpec CLI:

```
npx -y @fission-ai/openspec validate add-video-transcript-highlight --strict  # валидно
npx -y @fission-ai/openspec archive add-video-transcript-highlight -y          # exit 0
```

Результат:
- Change перемещён в `openspec/changes/archive/2026-08-30-add-video-transcript-highlight/`.
- Дельта-спеки применены к основным спекам:
  - `openspec/specs/speaking-content/spec.md` — 1 требование обновлено (транскрипт не хранится отдельно, админка показывает превью текста из .vtt);
  - `openspec/specs/video-transcript/spec.md` — создана новая спека (6 требований).
- `openspec list` → «No active changes found» (активных changes не осталось).

## Изменённые/созданные файлы

- Удалены (перемещены): `openspec/changes/add-video-transcript-highlight/{proposal.md,tasks.md,specs/**}` → `openspec/changes/archive/2026-08-30-add-video-transcript-highlight/` (всё сделано CLI `openspec archive`).
- Изменён: `openspec/specs/speaking-content/spec.md` (CLI применил дельту).
- Создан: `openspec/specs/video-transcript/spec.md` (CLI применил дельту).

## Проверка

- `npx -y @fission-ai/openspec archive ...` — exit 0, вывод «archived as '2026-08-30-add-video-transcript-highlight'».
- `npx -y @fission-ai/openspec list` — «No active changes found».
- `npx -y @fission-ai/openspec validate --specs --strict`: затронутые спеки **speaking-content ✓** и **video-transcript ✓** валидны. 3 ошибки валидации (`admin-login`, `theme-toggle`, `wasm-onboarding`) — **предсуществующие**, из архивов 2026-08-05, к данному change отношения не имеют; не исправлялись (вне скоупа, правка спек = ADR-007).

## Заметки

- Спеки в `openspec/specs/` обновлял сам `openspec archive` — это штатная механика воркфлоу (применение дельт при архивации), а не ручная правка спеки; правило ADR-007 не нарушено.
- Незакрытые пункты tasks.md (1.8 push CI, 4.5 диффы спек владельцу, 4.6 bd close) — исторические пометки `[~]`; памятка и решения уже внесены в memory.md ранее (грабли №87–89 и др.), bd-задача самого change ранее закрыта — поэтому change реализован и готов к архиву.
- Git-коммитов/пушей не делалось, gradle не запускался.
