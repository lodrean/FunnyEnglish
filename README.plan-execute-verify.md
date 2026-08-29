# README.plan-execute-verify.md

Шаблон конвейера для команды `/pipeline` (плагин `@deepseek-ai/dsh-command-pipeline`).

## Что это

Конвейер «план → разработка → верификация → отчёт» с согласованием человека на каждом этапе. Каждый этап выполняется свежим workflow-прогоном (новый субагент, передача только через артефакты). Между этапами команда спрашивает человека: Принять / Исправить / Стоп.

## Файлы (в рабочей папке сессии)

- `scripts/plan-execute-verify.workflow.js` — тело workflow-скрипта.
- `scripts/plan-execute-verify.config.json` — опциональные настройки (см. ниже).
- `README.plan-execute-verify.md` — этот шаблон.

При первом `/pipeline` в проекте без этих файлов плагин сам копирует шаблон из `packages/workflow/command-pipeline/template/` в `scripts/` проекта; существующие локальные `config.json` и README не перезаписываются. Скрипт общий для всех проектов — проектные отличия задаются конфигом.

## Контракт workflow-скрипта

Хост запускает скрипт по одному разу на этап: `workflowEngine.start({ script, meta, args, parent, signal })`.

- `args`: `{ task, projectName, stamp, stage, clarifications?, ...config }`; `stage` ∈ `plan | execute | verify | report`.
- Скрипт обязан вернуть plain JSON: `{ stage, taskDir, stamp, summary }`. `taskDir` — папка артефактов относительно рабочей папки, `summary` — итоги этапа для вопроса человеку.
- Доступны хуки: `agent(prompt, opts)`, `parallel(thunks)`, `pipeline(items, ...stages)`, `phase(title)`, `log(message)`, `args`. Node-API нет — файлы пишут агенты своими инструментами.
- `agent()` opts: `label`, `phase`, `schema`, `provider`, `model`.

## Артефакты

Каждый прогон задачи получает штамп `stamp` (формат YYYYMMDDHHmm) и папку `.pipeline/<stamp>/`:

- `00-report.md` — итоговый отчёт (этап report)
- `01-plan.md` — план (этап plan)
- `02-execute.md` — отчёт разработчика (этап execute)
- `03-verify.md` — вердикт верификации (этап verify)
- `05-gate-<stage>.md` — решения человека, пишет сам хост

## Конфиг (scripts/plan-execute-verify.config.json)

| Ключ | По умолчанию | Назначение |
| --- | --- | --- |
| `projectName` | имя рабочей папки | название проекта в промптах и отчёте |
| `maxRevisions` | `2` | лимит доработок этапа по решению «Исправить» |
| `kimiCommand` | `kimi` | CLI для генерации кода на этапе execute; если недоступен — агент пишет код сам |
| `planModel` | модель по умолчанию | модель агента планирования, например `deepseek-v4-flash` |
| `verifyModel` | модель по умолчанию | модель агента верификации, например `deepseek-v4-flash` |

## Запуск

В сессии с рабочей папкой: `/pipeline <описание задачи>`
