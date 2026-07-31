# CLAUDE.md - FunnyEnglish Project Instructions

## AI-Driven Development (AIDD) Process

This project follows the AIDD methodology where LLM acts as a team of specialized roles, not a single "magic brain".

### Quality Gates Pipeline

```
┌─────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐    ┌─────────┐
│  IDEA   │───►│ RESEARCH │───►│  PLAN   │───►│   PRD    │───►│ TASKLIST│
└────┬────┘    └────┬─────┘    └────┬────┘    └────┬─────┘    └────┬────┘
     │              │               │              │               │
   GATE 1        GATE 2          GATE 3         GATE 4          GATE 5
 IDEA_        RESEARCH_       PLAN_          PRD_           TASKLIST_
 CAPTURED     COMPLETE        APPROVED       READY          READY
     │              │               │              │               │
     └──────────────┴───────────────┴──────────────┴───────────────┘
                                     │
                                     ▼
┌─────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐    ┌─────────┐
│  DOCS   │◄───│    QA    │◄───│ REVIEW  │◄───│IMPLEMENT │◄───│  DEV    │
└────┬────┘    └────┬─────┘    └────┬────┘    └────┬─────┘    └────┬────┘
     │              │               │              │               │
   GATE 9        GATE 8          GATE 7         GATE 6          WORK
 DOCS_          QA_            REVIEW_        IMPLEMENT_
 UPDATED        PASS           OK             STEP_OK
```

### Team Roles (Subagents)

| Role | Responsibility | Output |
|------|---------------|--------|
| **Analyst** | Requirements gathering, Q&A | `docs/research/<ticket>.md` |
| **Researcher** | Codebase analysis | Research findings |
| **Architect** | System design, ADRs | `docs/plan/<ticket>.md`, `docs/adr/*.md` |
| **Developer** | Implementation | Code + tests |
| **Reviewer** | Code review | Review comments |
| **QA** | Testing | `reports/qa/<ticket>.md` |
| **Tech Writer** | Documentation | Updated docs, CHANGELOG |
| **Validator** | Quality gate verification | Validation report |

## Project Overview

FunnyEnglish — Speaking-тренажёр (пивот 2026-07-30): видео-топики + голосовые ответы. Актуальный контекст — `memory.md` и `AGENTS.md`.

### Current Status
- **Status:** Пивот в Speaking-тренажёр, эпик bd `FunnyEnglish-8tg` (реализация не начата, фазы в `docs/plan/SPEAKING-TRAINER-001.md`)
- **Спеки (источник истины, SDD):** `docs/prd/SPEAKING-TRAINER-001.prd.md`, `docs/SPEAKING_TRAINER_SPEC_PART{1,2,3}.md`; версионируются и ревьюятся (AGENTS.md правило 5), изменения — через OpenSpec (`openspec/`)
- **Дизайн-система:** Playful Coach v1.1 (`.docs/design-system/tokens.json`)
- Устаревшие отчёты и статусы удалены 2026-07-31; история тестовых прогонов — в `memory.md` §5

### Tech Stack
- **Backend**: Spring Boot 3 + Kotlin + PostgreSQL
- **Mobile/Desktop**: Kotlin Multiplatform + Compose Multiplatform
- **Admin Panel**: React 18 + TypeScript + Material UI

## Quick Start

```bash
# Backend (requires PostgreSQL running on port 5432)
cd backend && ./gradlew bootRun

# Admin Web
cd admin-web && npm install && npm run dev

# Mobile/Desktop
./gradlew :composeApp:run
```

## Project Structure

### Legacy Structure (Monolithic)
```
FunnyEnglish/
├── backend/                 # Spring Boot API
├── admin-web/               # React Admin Panel
├── composeApp/              # Compose Multiplatform UI (legacy)
├── shared/                  # KMP Shared Module (legacy, migrating to core/)
├── docs/                    # Documentation
└── reports/                 # QA Reports
```

### New Modular Architecture (Recommended)
```
FunnyEnglish/
├── backend/                 # Spring Boot API
├── admin-web/               # React Admin Panel
│
├── core/                    # Core infrastructure (KMP)
│   ├── toggle/              # Feature toggle system
│   ├── network/             # HTTP clients
│   ├── settings/            # App settings
│   └── di/                  # Core DI module
│
├── feature-api/             # API for feature modules (KMP)
│   ├── navigation/          # Inter-feature navigation
│   └── api/                 # FeatureEntry interfaces
│
├── feature-home/            # Feature: Home screen
├── feature-auth/            # Feature: Authentication
├── feature-tests/           # Feature: Tests/Quizzes
├── feature-groups/          # Feature: Student Groups
├── feature-gamification/    # Feature: Streaks/Achievements
├── feature-profile/         # Feature: User Profile
│   └── Can be toggled on/off via Feature Toggle system
│
├── app/                     # Application assembly module
│   └── Registers features, initializes app
│
├── docs/                    # Documentation
│   ├── prd/                 # Product Requirements
│   ├── plan/                # Implementation Plans
│   ├── tasklist/            # Task Lists
│   ├── adr/                 # Architecture Decision Records
│   ├── API.md               # REST API docs
│   └── MODULAR_ARCHITECTURE.md  # Modular architecture guide
│
└── reports/                 # QA Reports
```

## Feature Toggle System

FunnyEnglish uses a comprehensive **Feature Toggle** system for dynamic feature management.

### Quick Usage

```kotlin
// Check if feature is enabled
val toggleManager: FeatureToggleManager = get()

if (toggleManager.isEnabled(Feature.GROUPS)) {
    // Show groups UI
}

// Conditional Composable
@Composable
fun HomeScreen() {
    if (LocalFeatureToggle.current.isEnabled(Feature.STREAKS)) {
        StreakWidget()
    }
}
```

### Available Features

See `core/src/.../toggle/Feature.kt` for full list:
- `GROUPS` - Student groups/classes
- `ADAPTIVE_LESSONS` - ML-based adaptive learning
- `DAILY_QUESTS` - Daily quest system
- `FRIENDS` - Social friends system
- And more...

### Full Documentation

See [docs/MODULAR_ARCHITECTURE.md](docs/MODULAR_ARCHITECTURE.md) for:
- Creating new feature modules
- Feature toggle best practices
- A/B testing with toggles
- Migration guide from monolith

### Workflow: Adding New Feature (Modular)

```
1. Add Feature to enum in core/.../toggle/Feature.kt
   - Set defaultValue = false (for new features)
   - Set requiresRestart = true (if applicable)

2. Create feature-[name] module
   - Copy feature-home/build.gradle.kts as template
   - Add to settings.gradle.kts

3. Implement FeatureEntry interface
   - Create [Name]FeatureEntry class
   - Implement Content() composable

4. Register in app/ module
   - Add to feature registry
   - Add navigation routes

5. Add conditional UI in other modules
   - Use toggleManager.isEnabled(Feature.NAME)

6. Update backend toggle endpoint (if needed)
   - Add to FeatureToggleController

7. QA: Test with feature on/off
   - Test both states
   - Verify graceful degradation
```

## Development Workflow

### Starting a New Feature

```
1. User: "Add feature X"
2. Analyst: Create docs/research/feature-x.md
3. Architect: Create docs/plan/feature-x.md (with ADRs if needed)
4. Wait for approval
5. Analyst: Create docs/prd/feature-x.prd.md
6. Developer: Create docs/tasklist/feature-x.md
7. Developer: Implement tasks one by one
8. Reviewer: Code review
9. QA: Create reports/qa/feature-x.md
10. Validator: Verify all gates passed
```

### Continuing Work

```
User: "Continue on feature X"
↓
Check docs/tasklist/feature-x.md
↓
Find next uncompleted task
↓
Implement and verify
↓
Update tasklist status
```

## Quality Gates Details

### Gate 1: IDEA_CAPTURED ✅
- Clear problem statement
- Ticket ID assigned
- Initial context documented

### Gate 2: RESEARCH_COMPLETE ✅
- `docs/research/<ticket>.md` exists
- Affected areas identified
- Existing patterns documented
- Complexity assessed

### Gate 3: PLAN_APPROVED ✅
- `docs/plan/<ticket>.md` exists
- Status: APPROVED
- Architecture decisions documented
- Risks identified with mitigations

### Gate 4: PRD_READY ✅
- `docs/prd/<ticket>.prd.md` exists
- Status: READY
- User stories defined
- Acceptance criteria clear
- Success metrics defined

### Gate 5: TASKLIST_READY ✅
- `docs/tasklist/<ticket>.md` exists
- Tasks are small and incremental
- Each task has acceptance criteria
- No task > 1 day of work

### Gate 6: IMPLEMENT_STEP_OK ✅ (per task)
- Code compiles
- Tests pass
- Task acceptance criteria met
- No TODOs left

### Gate 7: REVIEW_OK ✅
- Code review completed
- No BLOCKER/CRITICAL issues
- Conventions followed
- Reviewer sign-off

### Gate 8: QA_PASS ✅
- `reports/qa/<ticket>.md` exists
- All acceptance criteria verified
- Automated tests passing
- Edge cases tested
- No critical issues

### Gate 9: DOCS_UPDATED ✅
- API docs updated (if changed)
- Architecture docs updated
- CHANGELOG updated
- README updated (if needed)

## Before Modifying Code

1. **Check active tasklist**: `docs/tasklist/<ticket>.md`
2. **Review PRD**: `docs/prd/<ticket>.prd.md`
3. **Review Plan**: `docs/plan/<ticket>.md`
4. **Check conventions**: `conventions.md`
5. **Understand patterns**: Read similar existing code

## After Modifying Code

1. **Verify compilation**:
   ```bash
   # Backend
   cd backend && ./gradlew build
   
   # Mobile
   ./gradlew :composeApp:build
   
   # Admin
   cd admin-web && npm run build
   ```

2. **Run tests**:
   ```bash
   ./gradlew :backend:test
   ./gradlew :shared:allTests
   ```

3. **Update documentation** if API changed:
   - Update `docs/API.md`
   - Update DTOs in `shared/model/`
   - Update `admin-web/src/api/client.ts`

4. **Update tasklist**: Mark completed tasks

5. **Commit with conventional message**:
   ```
   feat(scope): description
   
   Types: feat, fix, refactor, docs, test, chore
   Scopes: backend, mobile, admin, shared, docs
   ```

## UX Guidelines

See full guidelines in `docs/UX_GUIDELINES.md`.

### Key Patterns

**Dialogs:**
- Всегда добавлять кнопку отмены/закрытия
- Показывать состояние загрузки
- Обрабатывать клавиатуру (Done → submit)
- Иерархия кнопок: Primary (Filled) + Secondary (Text)

**Navigation:**
- TopAppBar с кнопкой "Назад" для вложенных экранов
- BottomNavigation: 3-5 иконок, активная подсвечена
- Скелетоны вместо спиннеров для загрузки

**Buttons:**
```kotlin
// Primary (main action)
Button(onClick = { }) { Text("Подтвердить") }

// Secondary (alternative)
OutlinedButton(onClick = { }) { Text("Отмена") }

// Tertiary (dismiss)
TextButton(onClick = { }) { Text("Пропустить") }

// With loading
Button(enabled = !isLoading) {
    if (isLoading) CircularProgressIndicator(...) 
    else Text("Отправить")
}
```

## Coding Conventions

See full conventions in `conventions.md`.

### Key Points
- **Kotlin**: camelCase for functions/variables, PascalCase for classes
- **Compose**: Composables start with uppercase
- **TypeScript**: Strict mode, functional components with hooks
- **Git**: Conventional commits, feature branches

## Available Commands

### Slash Commands
- `/plan <ticket>` - Create implementation plan
- `/research <ticket>` - Research codebase
- `/implement <ticket>` - Implement from tasklist
- `/review <ticket>` - Code review
- `/qa <ticket>` - Run QA checks
- `/techdebt` - Find technical debt
- `/prove` - Verify changes work
- `/elegant` - Refactor elegantly
- `/quiz` - Test understanding
- `/explain-visual` - Visual explanation

### Using Subagents

Spawn subagents for specific tasks to keep main context clean:

```
Task(subagent_name="analyst", description="Analyze requirements", prompt="...")
Task(subagent_name="architect", description="Design architecture", prompt="...")
Task(subagent_name="developer", description="Implement feature", prompt="...")
Task(subagent_name="reviewer", description="Review code", prompt="...")
Task(subagent_name="qa", description="Test implementation", prompt="...")
```

## Testing

### E2E Backend Tests (Primary)
```powershell
.\api-tests\e2e-backend-tests.ps1
# Results: 15/15 tests passing (100%)
```

### Unit Tests
```bash
./gradlew :backend:test
./gradlew :shared:allTests
```

### Maestro E2E
```bash
maestro test .maestro/
```

## Troubleshooting

### Backend won't start
- Check PostgreSQL: `docker ps` or `pg_isready`
- Check environment variables
- Check port 8080 is free

### Mobile app can't connect
- Backend running on correct port?
- Emulator: use `10.0.2.2` instead of `localhost`
- Physical device: use computer's local IP

### Admin build fails
- Delete `node_modules` and reinstall
- Check Node.js version (requires 18+)

## Session Notes

Folder `docs/notes/` contains task-specific notes.
- Update after each PR
- Reference in context: "see docs/notes/<task>.md"

## Claude Code Best Practices

### Plan Mode Strategy
- Use plan mode for non-trivial tasks
- After writing plan, spawn reviewer for feedback
- Return to plan mode if issues arise

### Useful Prompts
- "Докажи мне, что это работает" - Compare main vs feature
- "Погоняй меня по этим изменениям" - Understanding check
- "Зная всё что знаешь, переделай элегантно" - Refactor
- "Обнови CLAUDE.md чтобы не повторять эту ошибку" - Learn

### Visualization
- Request ASCII diagrams for architecture
- Create HTML presentations for complex code
- Use `/explain-visual` for auto-generation


<!-- BEGIN BEADS INTEGRATION v:1 profile:minimal hash:6cd5cc61 -->
## Beads Issue Tracker

This project uses **bd (beads)** for issue tracking. Run `bd prime` to see full workflow context and commands.

### Quick Reference

```bash
bd ready              # Find available work
bd show <id>          # View issue details
bd update <id> --claim  # Claim work
bd close <id>         # Complete work
```

### Rules

- Use `bd` for ALL task tracking — do NOT use TodoWrite, TaskCreate, or markdown TODO lists
- Run `bd prime` for detailed command reference and session close protocol
- Use `bd remember` for persistent knowledge — do NOT use MEMORY.md files

**Architecture in one line:** issues live in a local Dolt DB; sync uses `refs/dolt/data` on your git remote; `.beads/issues.jsonl` is a passive export. See https://github.com/gastownhall/beads/blob/main/docs/SYNC_CONCEPTS.md for details and anti-patterns.

## Agent Context Profiles

The managed Beads block is task-tracking guidance, not permission to override repository, user, or orchestrator instructions.

- **Conservative (default)**: Use `bd` for task tracking. Do not run git commits, git pushes, or Dolt remote sync unless explicitly asked. At handoff, report changed files, validation, and suggested next commands.
- **Minimal**: Keep tool instruction files as pointers to `bd prime`; use the same conservative git policy unless active instructions say otherwise.
- **Team-maintainer**: Only when the repository explicitly opts in, agents may close beads, run quality gates, commit, and push as part of session close. A current "do not commit" or "do not push" instruction still wins.

## Session Completion

This protocol applies when ending a Beads implementation workflow. It is subordinate to explicit user, repository, and orchestrator instructions.

1. **File issues for remaining work** - Create beads for anything that needs follow-up
2. **Run quality gates** (if code changed) - Tests, linters, builds
3. **Update issue status** - Close finished work, update in-progress items
4. **Handle git/sync by active profile**:
   ```bash
   # Conservative/minimal/default: report status and proposed commands; wait for approval.
   git status

   # Team-maintainer opt-in only, unless current instructions forbid it:
   git pull --rebase
   git push
   git status
   ```
5. **Hand off** - Summarize changes, validation, issue status, and any blocked sync/commit/push step

**Critical rules:**
- Explicit user or orchestrator instructions override this Beads block.
- Do not commit or push without clear authority from the active profile or the current user request.
- If a required sync or push is blocked, stop and report the exact command and error.
<!-- END BEADS INTEGRATION -->
