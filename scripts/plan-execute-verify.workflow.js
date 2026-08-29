/**
 * plan-execute-verify.workflow.js — координатор этапов конвейера /pipeline
 * (плагин @deepseek-ai/dsh-command-pipeline).
 *
 * Хост запускает этот скрипт по одному разу на этап через
 *   workflowEngine.start({ script, meta, args, parent, signal })
 * args = { task, projectName, stamp, stage, clarifications?, ...config },
 * где stage ∈ plan | execute | verify | report, а ...config — поля
 * scripts/plan-execute-verify.config.json.
 *
 * Скрипт обязан вернуть plain-JSON объект { stage, taskDir, stamp, summary }:
 *   - taskDir — папка артефактов относительно рабочей папки сессии;
 *   - summary — итоги этапа для вопроса человеку на согласовании.
 *
 * Доступны только хуки agent()/parallel()/pipeline()/phase()/log() и args;
 * Node-API нет, поэтому файлы создают агенты своими инструментами:
 *   .pipeline/<stamp>/01-plan.md    — план (этап plan)
 *   .pipeline/<stamp>/02-execute.md — отчёт разработчика (этап execute)
 *   .pipeline/<stamp>/03-verify.md  — вердикт верификации (этап verify)
 *   .pipeline/<stamp>/00-report.md  — итоговый отчёт (этап report)
 * Решения человека в 05-gate-<stage>.md пишет сам хост.
 */

const a = args ?? {}
const stage = String(a.stage ?? '').trim()
const task = String(a.task ?? '').trim()
const projectName = String(a.projectName ?? '').trim() || 'workspace'
const stamp = String(a.stamp ?? Date.now())
const clarifications = String(a.clarifications ?? '').trim()
const taskDir = `.pipeline/${stamp}`

/** Переопределение модели из config.json (planModel/verifyModel), если задано. */
function modelOpts(model) {
  return typeof model === 'string' && model.length > 0 ? { model } : {}
}

/** Сводка для вопроса человеку; null от агента превращается в сообщение о сбое. */
function summarize(text, fallback) {
  if (text === null || text === undefined) return fallback
  const s = String(text).trim()
  return s.length > 4000 ? `${s.slice(0, 4000)}\n…` : s
}

const kimiCommand = typeof a.kimiCommand === 'string' && a.kimiCommand.trim()
  ? a.kimiCommand.trim()
  : 'kimi'

if (stage === 'plan') {
  phase('Plan')
  const plan = await agent([
    `Ты — планировщик конвейера. Задача: ${task}`,
    `Проект: ${projectName}`,
    clarifications ? `Уточнения пользователя:\n${clarifications}` : null,
    'Изучи рабочую папку read-only инструментами (структура, ключевые файлы, существующие паттерны, состояние тестов). Ничего не меняй и не создавай.',
    'Составь decision-complete план: цель и критерии приёмки; изменения по подсистемам; затронутые API/схемы/конфиги; крайние случаи и риски; тесты; порядок шагов.',
    `Запиши план в ${taskDir}/01-plan.md (создай папку).`,
    'Верни краткую сводку плана (до 10 строк) — она уйдёт человеку на согласование.',
  ].filter(Boolean).join('\n\n'), { label: 'plan', phase: 'Plan', ...modelOpts(a.planModel) })
  return {
    stage,
    taskDir,
    stamp,
    summary: summarize(plan, `Этап «План» не выполнен: агент не вернул результат (см. ${taskDir}).`),
  }
}

if (stage === 'execute') {
  phase('Execute')
  const executed = await agent([
    `Ты — разработчик конвейера. Задача: ${task}`,
    `Проект: ${projectName}`,
    `План: прочитай ${taskDir}/01-plan.md; если его нет — составь рабочий план сам и придерживайся его.`,
    clarifications ? `Уточнения/замечания пользователя:\n${clarifications}` : null,
    'Реализуй задачу в рабочей папке, следуя плану и паттернам проекта: правь код, создавай файлы, обновляй или добавляй тесты.',
    `Код генерируй через CLI «${kimiCommand}» (запускай shell-командой), если он доступен, затем проверь и поправь результат своими инструментами; если CLI недоступен — пиши код напрямую.`,
    'Проверь результат релевантными командами (сборка/тесты/линт) и исправь найденное.',
    `Запиши краткий отчёт в ${taskDir}/02-execute.md: что сделано, какие файлы изменены, как проверить.`,
    'Верни краткую сводку изменений (до 10 строк) — она уйдёт человеку на согласование.',
  ].filter(Boolean).join('\n\n'), { label: 'execute', phase: 'Execute' })
  return {
    stage,
    taskDir,
    stamp,
    summary: summarize(executed, `Этап «Разработка» не выполнен: агент не вернул результат (см. ${taskDir}).`),
  }
}

if (stage === 'verify') {
  phase('Verify')
  const verdict = await agent([
    `Ты — верификатор конвейера. Задача: ${task}`,
    `Проект: ${projectName}`,
    `Материалы: план — ${taskDir}/01-plan.md, отчёт разработчика — ${taskDir}/02-execute.md.`,
    clarifications ? `Уточнения/замечания пользователя:\n${clarifications}` : null,
    'Сверь реализацию с планом и задачей: что выполнено, что нет, где отклонения и риски. Запусти релевантные проверки (тесты/сборка/линт), если это безопасно.',
    `Запиши вывод в ${taskDir}/03-verify.md: статус (ОК / частично / не выполнено), найденные проблемы, рекомендации.`,
    'Верни краткую сводку верификации (до 10 строк) — она уйдёт человеку на согласование.',
  ].filter(Boolean).join('\n\n'), { label: 'verify', phase: 'Verify', ...modelOpts(a.verifyModel) })
  return {
    stage,
    taskDir,
    stamp,
    summary: summarize(verdict, `Этап «Верификация» не выполнен: агент не вернул результат (см. ${taskDir}).`),
  }
}

if (stage === 'report') {
  phase('Report')
  const report = await agent([
    `Ты — автор итогового отчёта конвейера. Задача: ${projectName}: ${task}`,
    `Собери материалы: план — ${taskDir}/01-plan.md, отчёт разработчика — ${taskDir}/02-execute.md, верификация — ${taskDir}/03-verify.md, решения человека — ${taskDir}/05-gate-*.md (если есть).`,
    `Напиши итоговый отчёт в ${taskDir}/00-report.md: цель, что сделано, результаты проверок, отклонения, как проверить результат, что осталось.`,
    'Верни краткую сводку отчёта (до 10 строк).',
  ].filter(Boolean).join('\n\n'), { label: 'report', phase: 'Report' })
  return {
    stage,
    taskDir,
    stamp,
    summary: summarize(report, `Этап «Отчёт» не выполнен: агент не вернул результат (см. ${taskDir}).`),
  }
}

throw new Error(`plan-execute-verify: неизвестный этап «${stage}» (ожидались plan/execute/verify/report)`)
