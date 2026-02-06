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

FunnyEnglish is a cross-platform English learning application with gamification.

### Current Status
- **Status:** MVP Complete ✅ | Ready for Next Phase
- **E2E Tests:** 15/15 passing (100%) - [Details](docs/testing/TESTING_STATUS.md)
- **Integration Tests:** 6/7 passing (85.7%)
- **Full Report:** [PROJECT_STATUS_REPORT.md](docs/PROJECT_STATUS_REPORT.md)

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

```
FunnyEnglish/
├── backend/                 # Spring Boot API
├── admin-web/               # React Admin Panel
├── composeApp/              # Compose Multiplatform UI
├── shared/                  # KMP Shared Module
├── docs/                    # Documentation
│   ├── prd/                 # Product Requirements
│   ├── plan/                # Implementation Plans
│   ├── tasklist/            # Task Lists
│   ├── research/            # Research Documents
│   ├── adr/                 # Architecture Decision Records
│   └── API.md               # REST API docs
├── reports/                 # QA Reports
│   └── qa/                  # QA test reports
├── .claude/                 # Claude Code configuration
│   ├── agents/              # Subagent definitions
│   ├── commands/            # Slash commands
│   └── hooks/               # CI hooks
└── .claude-shared/          # Shared AIDD pipeline (optional)
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
