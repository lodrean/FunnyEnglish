<#
.SYNOPSIS
    Автономный прогон открытых задач bd через kimi-code CLI (драйвер-цикл).

.DESCRIPTION
    Читает .beads/issues.jsonl (passive export; CLI bd на этой машине нет),
    сортирует открытые задачи (priority -> created_at), для каждой кодовой
    задачи: собирает промпт -> запускает kimi (headless --print) ->
    прогоняет гейты (gradle) -> обновляет статус в .beads/issues.jsonl ->
    пишет артефакты в .pipeline/<stamp>/ и сводку в kimi-runs/<stamp>.md.

    Задачи-«решения владельца» (ADR-007) и блокированные окружением
    пропускаются с указанием причины (таблица $SkipReasons).

    Тикет закрывается ТОЛЬКО если: kimi завершился с кодом 0, лог непустой,
    ВСЕ гейты зелёные. Иначе остаётся in_progress с updated_at.

.PARAMETER Only
    Запустить только указанные id (например -Only FunnyEnglish-j8r).

.PARAMETER Epic
    Запустить подзадачи эпика (например -Epic FunnyEnglish-9bo). Гейты
    выбираются по типу эпика (admin/backend/client/none).

.PARAMETER Model
    Модель kimi (дефолт kimi-code/k3).

.PARAMETER DryRun
    Показать очередь/скипы и ничего не запускать.

.PARAMETER SkipGates
    Не прогонять gradle-гейты (только kimi + статус).

.EXAMPLE
    .\scripts\bd-kimi-loop.ps1 -DryRun
    .\scripts\bd-kimi-loop.ps1
    .\scripts\bd-kimi-loop.ps1 -Only FunnyEnglish-j8r -SkipGates
#>
param(
    [string[]]$Only = @(),
    [string]$Epic = '',
    [string]$Model = 'kimi-code/k3',
    [switch]$DryRun,
    [switch]$SkipGates,
    [switch]$NoCommit,
    [switch]$WaitQuota,
    [int]$KimiTimeoutSec = 1200,
    [int]$GateTimeoutSec = 1800
)

# EAP=Stop делал фатальным ЛЮБОЙ stderr нативных команд (git «Switched to a new
# branch», kimi/gradlew логи) — драйвер не должен умирать на этом. Собственные
# ошибки обрабатываются try/catch и проверками exit-кодов.
$ErrorActionPreference = 'Continue'
$PSNativeCommandUseErrorActionPreference = $false
$IssuesPath = Join-Path (Get-Location) '.beads/issues.jsonl'
$KimiMcpFile = Join-Path (Get-Location) 'scripts/kimi-mcp-empty.json'

# --- Причины пропуска (динамически: владелец-решения / эпики / окружение-блокеры) ---
# 4d1: код follow-up готов, блокер — живой Android-гейт (нет эмулятора); тикет НЕ закрывать.
$HardcodedSkips = @{
    'FunnyEnglish-4d1' = 'Код follow-up готов; блокер — живой Android-гейт (нет эмулятора); тикет НЕ закрывать'
}

# --- Дополнительный контекст задачи в промпт ---
$TaskExtras = @{
    'FunnyEnglish-j8r' = @'
Контекст задачи (cleanup):
- libs.androidx.media3.session подключён в shared и feature-tests, но MediaSession нигде не создаётся.
  Либо удалить зависимость, либо задействовать под фоновое аудио/медиаконтролы — выбери удаление,
  если нет явных признаков использования (проверь grep по MediaSession/Player.Listener и пр.).
- media3-ui (PlayerView) остался в gradle/libs.versions.toml после миграции на ui-compose (bd FunnyEnglish-did).
  Удали алиас/версию media3-ui, если на него нет ссылок (проверь все build.gradle.kts и исходники).
- Затронутые места: gradle/libs.versions.toml, shared/build.gradle.kts, feature-tests/build.gradle.kts
  (и feature-*/build.gradle.kts, где media3-ui/session упоминаются).
- Гейты драйвера: :composeApp:desktopTest, :composeApp:compileDebugKotlinAndroid,
  :composeApp:compileKotlinWasmJs (--no-configuration-cache). Сборки сам не запускай.
'@
    'FunnyEnglish-xic' = @'
Контекст задачи (дизайн-конформити, MS1-MS3):
- Аудит: docs/qa/design-conformance/REPORT_ANDROID_2026-08-10.md, строка MySubmissions (❌ MS1-MS3).
- Мокап: .docs/design-system/mockups.html, frame frame-submissions; скриншот-эталон docs/qa/design-conformance/mockup-submissions.png.
- App сейчас: «← Мои записи», «На проверке», 1-строчная карточка, explainer отсутствует.
- Привести экран MySubmissions (composeApp, app/screens/*, MySubmissionsViewModel) к мокапу:
  1) заголовок «Отправки» + подзаголовок (например «N записей · оценка учителя» по мокапу);
  2) бейдж NEW для новых записей;
  3) карточка 2-строчная: тема/дата + grade-chip (цвета из SpeakingColors, статусы НОВАЯ/ПРОВЕРЕНО и пр. по мокапу);
  4) explainer о запрете повторной отправки.
- Тексты не ломай те, на которые завязаны тесты/Maestro (проверь desktopTest и .maestro).
- ВАЖНО: это изменение поведения UI, но спеки/PRD не трогай (мокап — источник). Если без правки спеки
  не обойтись — остановись и опиши, что нужно (ADR-007).
'@
    'FunnyEnglish-c47' = @'
Контекст задачи (дизайн-конформити, V1/V2):
- Аудит: docs/qa/design-conformance/REPORT_ANDROID_2026-08-10.md, строка Video (⚠️ V1-V3).
- Мокап: .docs/design-system/mockups.html, frame frame-video; эталон docs/qa/design-conformance/mockup-video.png.
- Мокап: реплика (активные субтитры) в БЕЛОЙ карточке под плеером, CTA (переход к вопросам) сразу после карточки.
- App сейчас: текст субтитров plain (без карточки), CTA прижата к низу.
- Привести VideoScreen (composeApp) к мокапу: субтитры в карточке под плеером, CTA сразу после карточки.
  Полноэкранный overlay-режим (video_subtitle_overlay, memory.md §5 решение 2026-08-12) НЕ ломать.
- ВАЖНО: изменение UI-композиции; спеки/PRD не трогай. Если без правки спеки не обойтись — остановись
  и опиши (ADR-007).
'@
}

# --- Гейты по типу работы (client = KMP-приложение, admin = admin-web, backend = Spring Boot, none) ---
$GatesByKind = @{
    'client' = @(
        @{ Name = 'desktopTest';    Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':composeApp:desktopTest') },
        @{ Name = 'androidCompile'; Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':composeApp:compileDebugKotlinAndroid') },
        @{ Name = 'wasmCompile';    Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':composeApp:compileKotlinWasmJs', '--no-configuration-cache') }
    )
    'admin' = @(
        @{ Name = 'adminTypecheck'; Dir = 'admin-web'; Cmd = 'npm'; Args = @('run', 'typecheck') },
        @{ Name = 'adminVitest';    Dir = 'admin-web'; Cmd = 'npx'; Args = @('vitest', 'run') }
    )
    'backend' = @(
        @{ Name = 'backendTest';    Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':backend:test') }
    )
    'mixed' = @(
        # Продуктовые фичи (h3l) трогают И клиент, И backend — проверяем обе части
        # (h3l.3 слил сломанную backend-компиляцию, гоняя только клиентские гейты).
        @{ Name = 'desktopTest';    Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':composeApp:desktopTest') },
        @{ Name = 'androidCompile'; Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':composeApp:compileDebugKotlinAndroid') },
        @{ Name = 'wasmCompile';    Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':composeApp:compileKotlinWasmJs', '--no-configuration-cache') },
        @{ Name = 'backendTest';    Dir = '.'; Cmd = '.\gradlew.bat'; Args = @(':backend:test') }
    )
    'none' = @(
        # INF-задачи (CI/detekt/Kover/чистка): правки могут затрагивать сборку —
        # обязательная проверка конфигурации Gradle (qbq.5 сломал koverVerify вслепую).
        @{ Name = 'gradleConfig'; Dir = '.'; Cmd = '.\gradlew.bat'; Args = @('help', '--no-configuration-cache') }
    )
}
# Тип гейтов по эпику (префикс id); дефолт — client.
$EpicGateConfig = @{
    'FunnyEnglish-9bo' = 'admin'
    'FunnyEnglish-b85' = 'admin'
    'FunnyEnglish-nj2' = 'backend'
    'FunnyEnglish-wy7' = 'backend'
    'FunnyEnglish-0w3' = 'backend'
    'FunnyEnglish-h3l' = 'mixed'
    'FunnyEnglish-qbq' = 'none'
}

# Дополнительный контекст в промпт по типу работы
$KindExtras = @{
    'admin' = @'
Стек admin-web: React 18 + TS strict + MUI 6 + TanStack Query 5 + axios (src/api/client.ts, токен в localStorage) + vite 5.
- Страницы — src/pages/, API-клиент — src/api/client.ts, E2E — e2e/ (Playwright, Page Object e2e/pages/).
- Гейты драйвера: npm run typecheck (tsc --noEmit) и npx vitest run в admin-web. Сборки/тесты сам НЕ запускай.
- Конвенции: MUI компоненты, theme из src/theme (палитра speaking), формы на react-hook-form, данные через TanStack Query.
'@
    'backend' = @'
Стек backend: Spring Boot 3.4.1 + Kotlin + PostgreSQL + Flyway + JWT.
- Контекст-путь /api; контроллеры БЕЗ /api в маппингах; сущности backend/.../entity, миграции backend/src/main/resources/db/migration.
- Гейт драйвера: .\gradlew.bat :backend:test (тесты на H2 test-profile). Сборки/тесты сам НЕ запускай.
- Известные грабли: JSONB workaround (TestService), jackson-module-kotlin обязателен, миграции писать с IF NOT EXISTS.
'@
    'client' = @'
Стек клиента: Kotlin Multiplatform + Compose (монолит composeApp, app/screens/*, app/viewmodel/*, app/di/*, design/ + composeApp/designsystem токены Playful Coach).
- MVI: XxxState/Action/Event + StateFlow; DI — Koin (AppModule.kt); навигация — sealed AppScreen без NavHost.
- Гейты драйвера: :composeApp:desktopTest, :composeApp:compileDebugKotlinAndroid, :composeApp:compileKotlinWasmJs (--no-configuration-cache). Сборки/тесты сам НЕ запускай.
'@
}

function Get-UtcNowIso { (Get-Date).ToUniversalTime().ToString('yyyy-MM-ddTHH:mm:ssZ') }

function Update-IssueJsonl {
    param([string]$Path, [string]$Id, [hashtable]$Changes)
    $lines = [System.Collections.Generic.List[string]]::new()
    Get-Content $Path -Encoding utf8 | ForEach-Object { $lines.Add($_) }
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -notmatch '^\s*\{\s*$' -and $line -match ('"id"\s*:\s*"' + [regex]::Escape($Id) + '"')) {
            $obj = $line | ConvertFrom-Json
            foreach ($k in $Changes.Keys) { $obj | Add-Member -NotePropertyName $k -NotePropertyValue $Changes[$k] -Force }
            $lines[$i] = ($obj | ConvertTo-Json -Compress -Depth 10)
            Set-Content -Path $Path -Value $lines -Encoding utf8
            return $true
        }
    }
    return $false
}

function Invoke-Native {
    # Запуск нативной команды с таймаутом через фоновый job.
    # Возвращает exit code нативной команды; -2 = таймаут; -1 = нет вывода.
    # ВАЖНО: scriptblock'и через Start-Job -ArgumentList НЕ сериализуются
    # (превращаются в строку-текст), поэтому передаём имя команды + аргументы.
    param(
        [string]$CmdName,
        [object[]]$Arguments = @(),
        [int]$TimeoutSec = 1800,
        [string]$LogPath = ''
    )
    $workdir = (Get-Location).Path
    $sb = {
        param($c, $a, $l, $w)
        Set-Location $w
        if ($l) { & $c @a *> $l } else { & $c @a }
        $LASTEXITCODE
    }
    $job = Start-Job -ScriptBlock $sb -ArgumentList $CmdName, $Arguments, $LogPath, $workdir
    if (Wait-Job $job -Timeout $TimeoutSec) {
        $out = @(Receive-Job $job -Keep)
        Remove-Job $job -Force
        if ($out.Count -gt 0) { return [int]$out[-1] }
        return -1
    }
    Stop-Job $job -ErrorAction SilentlyContinue
    Remove-Job $job -Force
    return -2
}

function Invoke-Gate {
    param([hashtable]$Gate, [string]$LogDir)
    $log = Join-Path $LogDir ("gate-{0}.log" -f $Gate.Name)
    $dir = if ($Gate.Dir) { $Gate.Dir } else { '.' }
    Push-Location $dir
    $code = Invoke-Native -CmdName $Gate.Cmd -Arguments $Gate.Args -TimeoutSec $GateTimeoutSec -LogPath $log
    Pop-Location
    [pscustomobject]@{ Name = $Gate.Name; Ok = ($code -eq 0); ExitCode = $code; Log = $log }
}

# --- Загрузка задач ---
if (-not (Test-Path $IssuesPath)) { throw "issues.jsonl не найден: $IssuesPath" }
$issues = Get-Content $IssuesPath -Encoding utf8 | Where-Object { $_ -match '"id"' } | ForEach-Object { $_ | ConvertFrom-Json }
$open = @($issues | Where-Object { $_.status -notin @('closed', 'done', 'deferred') })
if ($Only.Count) { $open = @($open | Where-Object { $_.id -in $Only }) }
if ($Epic) {
    $epicChildren = @($issues | Where-Object {
        $_.dependencies -and (@($_.dependencies | Where-Object { $_.depends_on_id -eq $Epic }).Count -gt 0)
    } | ForEach-Object { $_.id })
    $open = @($open | Where-Object { $_.id -in $epicChildren })
    Write-Host ("[bd-kimi-loop] эпик {0}: открытых детей {1}" -f $Epic, $open.Count)
}

# --- Динамические скипы: DECISION-задачи, эпики с открытыми детьми, блокеры окружения ---
$openIds = @($open | ForEach-Object { $_.id })
$childCount = @{}
foreach ($i in $issues) {
    if ($i.dependencies) {
        foreach ($d in $i.dependencies) {
            if ($d.depends_on_id -and $d.depends_on_id -ne $i.id -and $d.depends_on_id -in $openIds) {
                if (-not $childCount.ContainsKey($d.depends_on_id)) { $childCount[$d.depends_on_id] = 0 }
                $childCount[$d.depends_on_id]++
            }
        }
    }
}
$SkipReasons = @{}
foreach ($h in $HardcodedSkips.Keys) { $SkipReasons[$h] = $HardcodedSkips[$h] }
foreach ($t in $open) {
    $reason = $null
    if ($t.title -match 'DECISION|РЕШЕНИЕ|решение владельца' -or $t.description -match 'Решение владельца|решение владельца') {
        $reason = 'Решение владельца (DECISION) — не задача на код'
    } elseif ($childCount[$t.id] -gt 0) {
        $reason = "Эпик-контейнер ($($childCount[$t.id]) открытых детей) — выполняется через подзадачи"
    } elseif ($t.description -match 'НЕ закрывать до живого|блокирован.*эмулятор|ждёт живого') {
        $reason = 'Блокер окружения (живой гейт) — тикет НЕ закрывать'
    }
    if ($reason -and -not $SkipReasons.ContainsKey($t.id)) { $SkipReasons[$t.id] = $reason }
}

$queue = @($open | Sort-Object @{ e = { try { [int]$_.priority } catch { 99 } } }, @{ e = { $_.created_at } })
$effectiveSkips = @($SkipReasons.Keys | Where-Object { $_ -in $openIds })
Write-Host ("[bd-kimi-loop] открытых задач: {0}; в очереди: {1}; скипов: {2}" -f $open.Count, $queue.Count, $effectiveSkips.Count)

$reportLines = @()
foreach ($task in $queue) {
    $id = $task.id
    $skip = $SkipReasons[$id]
    if ($skip) {
        $msg = "SKIP  {0} [p{1}] {2}  -> {3}" -f $id, $task.priority, $task.title, $skip
        Write-Host $msg; $reportLines += $msg
        continue
    }
    $msg = "RUN   {0} [p{1}] {2}" -f $id, $task.priority, $task.title
    Write-Host $msg; $reportLines += $msg
    if ($DryRun) { continue }

    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $dir = Join-Path (Get-Location) (".pipeline/$stamp")
    New-Item -ItemType Directory -Path $dir -Force | Out-Null

    # статус -> in_progress
    Update-IssueJsonl -Path $IssuesPath -Id $id -Changes @{ status = 'in_progress'; updated_at = (Get-UtcNowIso) } | Out-Null

    # --- git: ветка под задачу (коммит и merge в develop — в конце обработки) ---
    $branch = "kimi/$id-$stamp"
    if (-not $NoCommit) {
        git checkout -b $branch 2>&1 | Out-Null
    }

    # --- промпт ---
    $epicPrefix = ($id -split '\.')[0]
    $kind = $EpicGateConfig[$epicPrefix]
    if (-not $kind) { $kind = 'client' }
    $gates = $GatesByKind[$kind]
    if (-not $gates) { $gates = @() }
    $extra = ''
    if ($TaskExtras.ContainsKey($id)) { $extra = "`n`n$($TaskExtras[$id])" }
    $kindExtra = ''
    if ($KindExtras.ContainsKey($kind)) { $kindExtra = "`n`n$($KindExtras[$kind])" }
    $prompt = @"
Проект: So to Speak (FunnyEnglish) — Kotlin Multiplatform (Compose Multiplatform).
Рабочий каталог — корень репозитория (ветка develop). Ты выполняешь задачу bd $id.

Задача: $($task.title)

Описание задачи (из bd):
$($task.description)
$extra$kindExtra

Требования и ограничения:
- ПЕРЕД правками прочитай memory.md (архитектура, конвенции, известные грабли) и AGENTS.md (правила проекта).
- Для навигации по символам используй grep/read/glob; для UI-правок сверяйся с дизайн-системой Playful Coach (tokens в design/ и composeApp/designsystem).
- Меняй ТОЛЬКО файлы, необходимые для этой задачи; ничего лишнего не «улучшай», не удаляй и не переписывай.
- НЕ запускай gradle-сборки/тесты/линт (гейты прогоняет драйвер), НЕ делай git-коммитов и пушей.
- Спеки/PRD (docs/, openspec/) НЕ правишь: если для задачи нужна правка спеки или решение владельца — ОСТАНОВИСЬ и напиши в отчёте, что именно требуется (ADR-007, human-in-the-loop).
- После правок запиши краткий отчёт в $dir/02-execute.md: что сделано, список изменённых/созданных файлов, как проверить.
- В финальном ответе ПЕРВОЙ строкой верни маркер: `STATUS: DONE` (задача выполнена, можно закрывать) | `STATUS: NEEDS_OWNER` (нужно решение владельца или правка спеки, ADR-007) | `STATUS: BLOCKED` (непреодолимое препятствие). Далее — сводка до 10 строк.
"@
    $promptFile = Join-Path $dir 'kimi-prompt.txt'
    Set-Content -Path $promptFile -Value $prompt -Encoding utf8

    # --- kimi (с таймаутом и одним ретраем; БЕЗ внешних MCP-серверов: serena
    # через uvx периодически вешал сессию — навигация через grep/read самого kimi;
    # пустой конфиг через файл: встроенные кавычки JSON в args pwsh режет) ---
    $kimiLog = Join-Path $dir 'kimi-run.log'
    $kimiCode = -1
    $kimiTimedOut = $false
    $quotaHit = $false
    $kimiArgs = @('-p', $prompt, '-m', $Model, '--print', '--mcp-config-file', $KimiMcpFile)
    for ($attempt = 1; $attempt -le 2; $attempt++) {
        Write-Host ("  [{0}] kimi: {1} ({2}) — попытка {3}" -f $stamp, $id, $Model, $attempt)
        $started = Get-Date
        $kimiCode = Invoke-Native -CmdName 'kimi' -Arguments $kimiArgs -TimeoutSec $KimiTimeoutSec -LogPath $kimiLog
        if ($kimiCode -eq -2) {
            $kimiTimedOut = $true
            Write-Host ("  kimi TIMEOUT (> {0} с) на попытке {1} — чистка зависших процессов" -f $KimiTimeoutSec, $attempt)
            Get-Process kimi -ErrorAction SilentlyContinue | Where-Object { $_.StartTime -ge $started.AddMinutes(-1) } | Stop-Process -Force -ErrorAction SilentlyContinue
            foreach ($pn in @('serena', 'uv', 'uvx', 'python', 'python3.13', 'node')) {
                Get-Process $pn -ErrorAction SilentlyContinue | Where-Object { $_.StartTime -ge $started.AddMinutes(-1) } | Stop-Process -Force -ErrorAction SilentlyContinue
            }
            if ($attempt -eq 1) { Write-Host '  повторная попытка...'; continue }
        }
        # --- квота kimi: ждём сброса окна (только с -WaitQuota) и ретраим задачу ---
        if ($kimiCode -ne 0) {
            $qTail = ((Get-Content $kimiLog -Tail 50 -ErrorAction SilentlyContinue) -join "`n")
            if ($qTail -match "You've reached your 5-hour usage limit|access_terminated_error") {
                $quotaHit = $true
                if ($WaitQuota) {
                    Write-Host 'QUOTA: квота kimi исчерпана — жду сброса окна (проверка каждые 5 мин, до 6 ч)...'
                    $recovered = $false
                    for ($w = 0; $w -lt 72 -and -not $recovered; $w++) {
                        Start-Sleep -Seconds 300
                        # Тест строгий: только реальный ответ (маркер <choice>) без ошибок.
                        # Мелкий запрос мог проходить в обход квоты — не доверяем голому exit 0.
                        $to = (& kimi --quiet -p "Ответь одним словом: OK" -m $Model --mcp-config-file $KimiMcpFile 2>&1 | Out-String)
                        if ($LASTEXITCODE -eq 0 -and $to -match '<choice>' -and $to -notmatch 'usage limit|access_terminated|Error code') { $recovered = $true }
                    }
                    if ($recovered) {
                        Write-Host '  квота восстановилась — задача перезапускается с чистого листа'
                        $attempt = 0
                        continue
                    }
                    Write-Host 'QUOTA: 6 часов ожидания истекли — марафон остановлен'
                }
                # без -WaitQuota или после истёкшего ожидания: дальше бессмысленно
            }
        }
        break
    }
    $logBytes = if (Test-Path $kimiLog) { (Get-Item $kimiLog).Length } else { 0 }
    # Закрытие ТОЛЬКО при реальном успехе: exit 0 (403-квота и прочие падения дают exit 1)
    $kimiOk = ($kimiCode -eq 0) -and ($logBytes -gt 0) -and (-not $kimiTimedOut)
    Write-Host ("  kimi exit: {0}; timedout: {1}; log bytes: {2}" -f $kimiCode, $kimiTimedOut, $logBytes)

    # --- детект исчерпания квоты kimi (403 usage limit) — ставится и в цикле попыток.
    # ТОЛЬКО при ненулевом exit (сессия убита ошибкой) И точной строке в ХВОСТЕ лога:
    # сам текст грабли в memory.md тоже содержит «usage limit» (kimi читает память). ---
    if (-not $quotaHit -and (Test-Path $kimiLog)) {
        $tailLog = ((Get-Content $kimiLog -Tail 50 -ErrorAction SilentlyContinue) -join "`n")
        if ($kimiCode -ne 0 -and $tailLog -match "You've reached your 5-hour usage limit|access_terminated_error") { $quotaHit = $true }
    }

    # --- гейты ---
    $gateResults = @()
    if ($kimiOk -and -not $SkipGates -and -not $quotaHit) {
        foreach ($g in $gates) {
            $r = Invoke-Gate -Gate $g -LogDir $dir
            $gateResults += $r
            Write-Host ("  gate {0}: {1} (exit {2})" -f $r.Name, $(if ($r.Ok) { 'OK' } else { 'FAIL' }), $r.ExitCode)
        }
    }
    $gatesOk = ($gateResults.Count -gt 0) -and -not ($gateResults | Where-Object { -not $_.Ok })

    # --- маркер статуса от kimi (DONE | NEEDS_OWNER | BLOCKED) — ТОЛЬКО из хвоста лога,
    # чтобы эхо промпта в начале не давало ложных совпадений ---
    $statusMarker = ''
    if (Test-Path $kimiLog) {
        $m = (Get-Content $kimiLog -Tail 300 | Select-String -Pattern 'STATUS:\s*(DONE|NEEDS_OWNER|BLOCKED)' | Select-Object -Last 1)
        if ($m) { $statusMarker = $m.Matches[0].Groups[1].Value }
    }
    $ownerStopped = $statusMarker -in @('NEEDS_OWNER', 'BLOCKED')

    # --- статус ---
    $changed = (& git status --short 2>$null | Out-String).Trim()
    $closeable = $kimiOk -and $gatesOk -and -not $ownerStopped
    if ($closeable) {
        Update-IssueJsonl -Path $IssuesPath -Id $id -Changes @{
            status      = 'closed'
            closed_at   = (Get-UtcNowIso)
            close_reason = ("Прогон kimi {0}: exit {1}, гейты OK ({2})" -f $stamp, $kimiCode, (($gateResults | ForEach-Object { $_.Name }) -join ','))
            updated_at  = (Get-UtcNowIso)
        } | Out-Null
        $verdict = 'CLOSED (kimi+гейты OK)'
    } else {
        Update-IssueJsonl -Path $IssuesPath -Id $id -Changes @{ updated_at = (Get-UtcNowIso) } | Out-Null
        $why = if ($quotaHit) { 'квота kimi исчерпана (403 usage limit)' } elseif ($kimiTimedOut) { "kimi TIMEOUT (2x$($KimiTimeoutSec/60) мин)" } elseif ($ownerStopped) { "kimi остановился: $statusMarker" } else { "kimi=$kimiCode gates=" + ($(if ($gatesOk) { 'ok' } else { 'FAIL' })) }
        $verdict = "IN_PROGRESS ($why)"
    }
    Write-Host ("  verdict: {0}" -f $verdict)
    $reportLines += ("  -> {0}" -f $verdict)

    # --- отчёт прогона ---
    $report = @"
# Прогон kimi: $id — $($task.title)

- Стамп: $stamp · Модель: $Model · Задача: [$id] $($task.title)
- kimi exit code: $kimiCode · Лог: kimi-run.log · Промпт: kimi-prompt.txt
- Гейты: $(if ($gateResults.Count) { ($gateResults | ForEach-Object { "$($_.Name)=$(if ($_.Ok) {'OK'} else {"FAIL($($_.ExitCode))"})" }) -join ', ' } else { 'не запускались' })
- Вердикт: $verdict

## Изменённые файлы (git status --short)
$changed

## Сводка kimi (хвост kimi-run.log)
$(if (Test-Path $kimiLog) { (Get-Content $kimiLog -Tail 120 | Out-String) } else { '(лог отсутствует)' })
"@
    Set-Content -Path (Join-Path $dir '00-report.md') -Value $report -Encoding utf8
    $runLog = Join-Path (Get-Location) ("kimi-runs/{0}-{1}.md" -f $stamp, $id)
    Set-Content -Path $runLog -Value $report -Encoding utf8

    # --- git: коммит на ветке + merge в develop (или WIP в stash при незакрытой задаче) ---
    if (-not $NoCommit) {
        if ($closeable) {
            git add -A 2>&1 | Out-Null
            $type = 'chore'
            if ($task.title -match '^(SEC|ADM|BUG|FIX)') { $type = 'fix' }
            elseif ($task.title -match '^(LC|BE|ADT|DS|KMP)') { $type = 'refactor' }
            elseif ($task.title -match '^PR') { $type = 'feat' }
            elseif ($task.title -match '^INF') { $type = 'chore' }
            $scope = switch ($kind) { 'admin' { 'admin' } 'backend' { 'backend' } 'none' { 'infra' } default { 'composeApp' } }
            $msg = "$type($scope): $($task.title) (bd $id)"
            git commit -m $msg 2>&1 | Out-Null
            git checkout develop 2>&1 | Out-Null
            $mg = (& git merge --no-ff $branch -m "merge: $msg" 2>&1 | Out-String)
            if ($LASTEXITCODE -ne 0) { Write-Host ("  git WARN merge: " + $mg.Trim()) }
            git branch -D $branch 2>&1 | Out-Null
            Write-Host ("  git: committed+merged -> develop ($msg)")
        } else {
            git stash push -u -m "bd $id not closed (kimi=$kimiCode, $verdict)" 2>&1 | Out-Null
            git checkout develop 2>&1 | Out-Null
            git branch -D $branch 2>&1 | Out-Null
            Update-IssueJsonl -Path $IssuesPath -Id $id -Changes @{ status = 'in_progress'; updated_at = (Get-UtcNowIso) } | Out-Null
            Write-Host ("  git: WIP застешен (stash), ветка удалена, статус in_progress")
        }
    }

    # --- квота kimi исчерпана: дальше бессмысленно, останавливаем марафон ---
    if ($quotaHit) {
        Write-Host 'QUOTA: квота kimi (5-hour usage limit) исчерпана — марафон остановлен. Перезапустить после сброса окна.'
        break
    }
}

# --- Авто-закрытие эпиков, у которых не осталось открытых детей ---
if (-not $Epic) {
    $allOpenNow = @($issues | Where-Object { $_.status -notin @('closed', 'done', 'deferred') })
    $openNowIds = @($allOpenNow | ForEach-Object { $_.id })
    foreach ($t in $allOpenNow) {
        if ($childCount[$t.id] -le 0) { continue }
        $kidsOpen = @($issues | Where-Object {
            $_.dependencies -and (@($_.dependencies | Where-Object { $_.depends_on_id -eq $t.id }).Count -gt 0)
        } | Where-Object { $_.id -in $openNowIds })
        if ($kidsOpen.Count -eq 0) {
            Update-IssueJsonl -Path $IssuesPath -Id $t.id -Changes @{
                status       = 'closed'
                closed_at    = (Get-UtcNowIso)
                close_reason = 'Эпик закрыт автоматически: все открытые дети выполнены'
                updated_at   = (Get-UtcNowIso)
            } | Out-Null
            Write-Host ("EPIC-CLOSED {0} ({1})" -f $t.id, $t.title)
        }
    }
}

# --- Финальный коммит статусов эпиков (если что-то осталось незакоммиченным) ---
if (-not $NoCommit) {
    $dirty = (& git status --short | Measure-Object -Line).Lines
    if ($dirty -gt 0) {
        git add -A 2>&1 | Out-Null
        git commit -m "chore(bd): авто-закрытие эпиков и финальные статусы" 2>&1 | Out-Null
        Write-Host 'git: финальный chore-коммит (эпики/статусы)'
    }
}

Write-Host '---'
$reportLines | ForEach-Object { Write-Host $_ }
Write-Host '[bd-kimi-loop] завершено'
