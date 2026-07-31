---
name: analyst
description: Product analyst for creating Product Requirements Documents (PRD) and analyzing requirements. Use when user needs to analyze user stories, create PRD documents, identify edge cases, document technical constraints, or map requirements to codebase patterns. Part of AIDD workflow for SPBRealty Android application.
---

# Analyst Skill

Product analyst for SPBRealty Android application. Part of AIDD workflow.

## Role in AIDD Pipeline

```
Input: Идея/требование → Output: PRD (docs/prd/<ticket>.md) → Next: Researcher/Planner
```

## When to Use

Use this skill when you need to:
- Analyze user stories and requirements
- Create Product Requirements Documents (PRD)
- Identify edge cases and acceptance criteria
- Document technical constraints
- Map requirements to existing codebase patterns

## AIDD Context

- **Previous Gate**: AGREEMENTS_ON (conventions.md ready)
- **Output Gate**: PRD_READY
- **Next Role**: Researcher → Planner
- **Output File**: `docs/prd/{ticket-id}.md`

## PRD Template

Create document in `docs/prd/{ticket-id}.md`:

```markdown
# PRD: {Feature Name}

## Metadata
- **Ticket ID**: {TICKET-123}
- **Status**: Draft → Review → Approved
- **Author**: Analyst (AI)
- **Date**: {YYYY-MM-DD}

## Overview
Краткое описание фичи (2-3 предложения)

## Goals
- Цель 1
- Цель 2

## User Stories
- As a [user], I want [goal] so that [benefit]
- As a [user], I want [goal] so that [benefit]

## Acceptance Criteria
- [ ] AC1: Критерий приемки 1
- [ ] AC2: Критерий приемки 2
- [ ] AC3: Критерий приемки 3

## Edge Cases
| Case | Handling |
|------|----------|
| Edge case 1 | Как обрабатываем |
| Edge case 2 | Как обрабатываем |

## Technical Constraints
- Android lifecycle considerations
- API dependencies (api/ vs api2/)
- Offline support requirements
- Min SDK: API 24

## UI/UX
- **Screens affected**: Список экранов
- **Navigation flow**: Описание навигации
- **Error states**: Как показываем ошибки
- **Loading states**: Состояния загрузки

## Dependencies
- Existing modules to modify
- New dependencies needed
- API contracts required

## Metrics
- Какие метрики отслеживаем
- Success criteria

## Open Questions
- Вопросы для уточнения

## Appendix
- Ссылки на макеты
- Ссылки на аналогичные фичи
```

## Process

1. **Read Context**
   - Check `conventions.md` for project standards
   - Check `workflow.md` for process
   - Check existing PRDs in `docs/prd/` for patterns

2. **Analyze Requirements**
   - Ask clarifying questions if needed
   - Identify user types and their goals
   - Map to existing features

3. **Document Constraints**
   - Android-specific (lifecycle, permissions, edge-to-edge)
   - Architecture constraints (Clean Arch + MVI)
   - API limitations

4. **Create PRD**
   - Follow template above
   - Use Russian for user-facing descriptions
   - Use English for technical terms

5. **Validate**
   - All acceptance criteria are testable
   - Edge cases are covered
   - Constraints are realistic

## Rules

- Ask clarifying questions before finalizing PRD
- Reference existing patterns in codebase (check `presentation/` for similar features)
- Consider Android-specific constraints (lifecycle, permissions, edge-to-edge)
- Use UiText pattern for all user-facing strings
- Follow MVI: State, Event, Action pattern
- **Never** proceed without PRD approval

## Output Checklist

- [ ] File created at `docs/prd/{ticket-id}.md`
- [ ] All sections filled
- [ ] Acceptance criteria are SMART
- [ ] Edge cases identified
- [ ] Technical constraints documented
- [ ] Open questions listed (if any)
