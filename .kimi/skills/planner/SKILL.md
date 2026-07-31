---
name: planner
description: Implementation planner for creating detailed plans and tasklists from PRD and research. Use when user needs to create implementation plans, break features into tasks, define dependencies, estimate effort, or create tasklists for developers. Part of AIDD workflow for SPBRealty Android application.
---

# Planner Skill

Implementation planner for SPBRealty Android application. Part of AIDD workflow.

## Role in AIDD Pipeline

```
Input: PRD + Research → Output: Plan + Tasklist → Next: Implementer
```

## When to Use

Use this skill when you need to:
- Create detailed implementation plan
- Break down feature into tasks
- Define task dependencies
- Estimate effort (relative)
- Create tasklist for implementer

## AIDD Context

- **Previous Gate**: PRD_READY, RESEARCH_DONE
- **Input**: `docs/prd/{ticket-id}.md`, `docs/research/{ticket-id}.md`
- **Output Gate**: PLAN_APPROVED, TASKLIST_READY
- **Next Role**: Implementer
- **Output Files**: `docs/plan/{ticket-id}.md`, `docs/tasklist/{ticket-id}.md`

## Implementation Plan Template

```markdown
# Implementation Plan: {Feature Name}

## Metadata
- **Ticket ID**: {TICKET-123}
- **PRD**: docs/prd/{ticket-id}.md
- **Research**: docs/research/{ticket-id}.md
- **Status**: Draft → Approved
- **Date**: {YYYY-MM-DD}

## Architecture Decision
### Approach
Краткое описание выбранного подхода

### Alternatives Considered
| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| Option 1 | ... | ... | Not chosen |
| Option 2 | ... | ... | **Chosen** |

## Component Design
### Data Layer
- Repository: `data/repository/{Feature}Repository`
- API: `data/api/{Feature}Api`
- Database: `data/database/entity/{Feature}Entity`

### Domain Layer
- UseCase: `domain/usecase/{Feature}UseCase`
- Model: `domain/model/{Feature}Model`

### Presentation Layer
- Screen: `presentation/{feature}/{Feature}Screen`
- ViewModel: `presentation/{feature}/{Feature}ViewModel`
- State: `presentation/{feature}/{Feature}State`
- Event: `presentation/{feature}/{Feature}Event`
- Action: `presentation/{feature}/{Feature}Action`
- Navigation: `presentation/{feature}/{Feature}Navigation`

## Task Dependencies
```
Task 1 (API)
  → Task 2 (Repository)
    → Task 3 (UseCase)
      → Task 4 (ViewModel)
        → Task 5 (Screen)
          → Task 6 (Navigation)
```

## Testing Strategy
- Unit tests for: UseCase, ViewModel
- UI tests for: Screen interactions
- Integration tests for: API + Repository

## Risks and Mitigations
| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Risk 1 | Low/Med/High | Low/Med/High | Стратегия |
```

## Tasklist Template

```markdown
# Tasklist: {Feature Name}

## Metadata
- **Ticket ID**: {TICKET-123}
- **Plan**: docs/plan/{ticket-id}.md
- **Status**: In Progress
- **Progress**: 0/X tasks

## Tasks

### Data Layer
- [ ] Task 1: Create API interface
  - **File**: `data/api/{Feature}Api.kt`
  - **Depends**: None
  - **AC**: Interface with endpoints defined

- [ ] Task 2: Create DTOs
  - **File**: `data/dto/{Feature}Dto.kt`
  - **Depends**: Task 1
  - **AC**: Request/Response DTOs with serialization

- [ ] Task 3: Create Repository
  - **File**: `data/repository/{Feature}RepositoryImpl.kt`
  - **Depends**: Task 2
  - **AC**: Repository implements domain interface

### Domain Layer
- [ ] Task 4: Create domain model
  - **File**: `domain/model/{Feature}Model.kt`
  - **Depends**: None
  - **AC**: Data class with all fields

- [ ] Task 5: Create repository interface
  - **File**: `domain/repository/{Feature}Repository.kt`
  - **Depends**: Task 4
  - **AC**: Interface with suspend functions

- [ ] Task 6: Create UseCase
  - **File**: `domain/usecase/{Feature}UseCase.kt`
  - **Depends**: Task 5
  - **AC**: UseCase with operator fun invoke

### Presentation Layer
- [ ] Task 7: Create State
  - **File**: `presentation/{feature}/{Feature}State.kt`
  - **Depends**: Task 4
  - **AC**: Data class with all UI states

- [ ] Task 8: Create Event
  - **File**: `presentation/{feature}/{Feature}Event.kt`
  - **Depends**: Task 7
  - **AC**: Sealed interface with all events

- [ ] Task 9: Create Action
  - **File**: `presentation/{feature}/{Feature}Action.kt`
  - **Depends**: Task 7
  - **AC**: Sealed interface with all actions

- [ ] Task 10: Create ViewModel
  - **File**: `presentation/{feature}/{Feature}ViewModel.kt`
  - **Depends**: Task 6, Task 7, Task 8, Task 9
  - **AC**: ViewModel extends BaseViewModel

- [ ] Task 11: Create Screen
  - **File**: `presentation/{feature}/{Feature}Screen.kt`
  - **Depends**: Task 10
  - **AC**: Composable screen with all states

- [ ] Task 12: Create Navigation
  - **File**: `presentation/{feature}/{Feature}Navigation.kt`
  - **Depends**: Task 11
  - **AC**: Route and navigation functions

### DI and Integration
- [ ] Task 13: Register in Koin
  - **File**: `di/ViewModelModule.kt`, `di/RepositoryModule.kt`
  - **Depends**: Task 3, Task 10
  - **AC**: All components registered

- [ ] Task 14: Add to navigation graph
  - **File**: `presentation/navigation/BottomNavigationScreen.kt`
  - **Depends**: Task 12
  - **AC**: Route added to NavHost

### Testing
- [ ] Task 15: Unit tests for UseCase
  - **File**: `test/.../{Feature}UseCaseTest.kt`
  - **Depends**: Task 6
  - **AC**: Tests with MockK

- [ ] Task 16: Unit tests for ViewModel
  - **File**: `test/.../{Feature}ViewModelTest.kt`
  - **Depends**: Task 10
  - **AC**: Tests for all event handlers

### QA
- [ ] Task 17: QA verification
  - **Depends**: All above
  - **AC**: QA report in reports/qa/

## Progress Log
- [YYYY-MM-DD HH:MM] Tasklist created
- [YYYY-MM-DD HH:MM] Task X started
- [YYYY-MM-DD HH:MM] Task X completed
```

## Planning Rules

- Tasks should be small (1-2 hours max)
- Each task must have clear AC (Acceptance Criteria)
- Dependencies must be explicitly stated
- Follow Clean Architecture order: Data → Domain → Presentation
- Include testing tasks
- Include DI registration tasks
- Include navigation integration tasks

## Output Checklist

- [ ] Plan created at `docs/plan/{ticket-id}.md`
- [ ] Tasklist created at `docs/tasklist/{ticket-id}.md`
- [ ] All components identified
- [ ] Dependencies mapped
- [ ] Testing strategy defined
- [ ] Tasks are small and actionable
