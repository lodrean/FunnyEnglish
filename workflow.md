# So to Speak Development Workflow (AIDD Pipeline)

## Overview

This document describes the AI-Driven Development (AIDD) pipeline for the So to Speak project. Each feature follows a structured workflow with quality gates.

## Pipeline Stages

```
┌─────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐    ┌─────────┐
│  IDEA   │───►│ RESEARCH │───►│  PLAN   │───►│   PRD    │───►│ TASKLIST│
└─────────┘    └──────────┘    └─────────┘    └──────────┘    └─────────┘
                                                                    │
┌─────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐         │
│  DOCS   │◄───│    QA    │◄───│ REVIEW  │◄───│IMPLEMENT │◄────────┘
└─────────┘    └──────────┘    └─────────┘    └──────────┘
```

## Stage Descriptions

### 1. IDEA (Ideation)
**Input**: User request, feature idea, bug report
**Output**: Clear problem statement
**Gate**: `IDEA_CAPTURED`

### 2. RESEARCH (Analysis)
**Input**: Problem statement
**Output**: `docs/research/<ticket>.md`
**Gate**: `RESEARCH_COMPLETE`

Activities:
- Analyze existing codebase
- Identify affected files
- Find similar patterns
- Assess complexity

### 3. PLAN (Architecture)
**Input**: Research findings
**Output**: `docs/plan/<ticket>.md`
**Gate**: `PLAN_APPROVED`

Contents:
- Proposed approach
- Architectural decisions
- Technology choices
- Risks and mitigations

### 4. PRD (Requirements)
**Input**: Approved plan
**Output**: `docs/prd/<ticket>.prd.md`
**Gate**: `PRD_READY`

Contents:
- Feature description
- User stories
- Acceptance criteria
- Success metrics

### 5. TASKLIST (Decomposition)
**Input**: PRD document
**Output**: `docs/tasklist/<ticket>.md`
**Gate**: `TASKLIST_READY`

Format:
```markdown
- [ ] Task 1 description
  - Acceptance: test X passes
  - Acceptance: scenario Y works
- [ ] Task 2 description
  - Acceptance: API returns correct data
```

### 6. IMPLEMENT (Development)
**Input**: Tasklist with acceptance criteria
**Output**: Code changes
**Gate**: `IMPLEMENT_STEP_OK` (per task)

Process:
1. Pick one task from tasklist
2. Implement changes
3. Verify acceptance criteria locally
4. Mark task complete
5. Repeat until all tasks done

### 7. REVIEW (Code Review)
**Input**: Implemented code
**Output**: Review comments / approval
**Gate**: `REVIEW_OK`

Checklist:
- [ ] Code follows conventions
- [ ] No security vulnerabilities
- [ ] Tests included
- [ ] Documentation updated

### 8. QA (Quality Assurance)
**Input**: Reviewed code
**Output**: `reports/qa/<ticket>.md`
**Gate**: `RELEASE_READY`

Activities:
- Run all tests
- Manual verification
- Edge case testing
- Regression check

### 9. DOCS (Documentation)
**Input**: Released feature
**Output**: Updated documentation
**Gate**: `DOCS_UPDATED`

Update:
- API documentation
- Architecture docs
- README if needed
- User guides

## Document Templates

### Research Template (`docs/research/<ticket>.md`)
```markdown
# Research: <Ticket Title>

## Ticket
<ticket-id>

## Objective
<what we're investigating>

## Affected Areas
- File 1: reason
- File 2: reason

## Existing Patterns
<how similar features are implemented>

## Complexity Assessment
- Estimated scope: Low/Medium/High
- Risk areas: <list>

## Open Questions
- [ ] Question 1 (Status: OPEN/RESOLVED)
- [ ] Question 2

## Recommendation
<proposed approach>
```

### Plan Template (`docs/plan/<ticket>.md`)
```markdown
# Plan: <Ticket Title>

## Ticket
<ticket-id>

## Status
DRAFT | REVIEW | APPROVED

## Approach
<high-level description>

## Architecture Decisions
### Decision 1
- **Context**: <situation>
- **Decision**: <what we decided>
- **Consequences**: <tradeoffs>

## Implementation Steps
1. Step 1
2. Step 2
3. Step 3

## Risks
| Risk | Mitigation |
|------|------------|
| Risk 1 | How to handle |

## Dependencies
- <external dependency>
```

### PRD Template (`docs/prd/<ticket>.prd.md`)
```markdown
# PRD: <Feature Name>

## Ticket
<ticket-id>

## Status
DRAFT | REVIEW | READY

## Context
<why this feature is needed>

## Goals
1. Goal 1
2. Goal 2

## Success Metrics
- Metric 1: target value
- Metric 2: target value

## User Stories
### Story 1
As a <user type>, I want to <action> so that <benefit>

**Acceptance Criteria:**
- [ ] Criterion 1
- [ ] Criterion 2

## Out of Scope
- Feature X
- Feature Y

## Open Questions
- [ ] Question 1 (Status: OPEN)
```

### Tasklist Template (`docs/tasklist/<ticket>.md`)
```markdown
# Tasklist: <Ticket Title>

## Ticket
<ticket-id>

## Status
IN_PROGRESS | BLOCKED | COMPLETE

## Tasks

### Backend
- [ ] Task 1
  - AC: Test passes
  - AC: Endpoint returns 200
- [ ] Task 2

### Mobile
- [ ] Task 3
  - AC: Screen renders correctly
- [ ] Task 4

### Admin Web
- [ ] Task 5

## Blockers
<any blocking issues>

## Notes
<additional context>
```

### QA Report Template (`reports/qa/<ticket>.md`)
```markdown
# QA Report: <Ticket Title>

## Ticket
<ticket-id>

## Test Date
<YYYY-MM-DD>

## Summary
PASS | FAIL | PARTIAL

## Test Results

### Automated Tests
- Backend: PASS/FAIL (X/Y passed)
- Mobile: PASS/FAIL
- Admin: PASS/FAIL

### Manual Testing
| Scenario | Expected | Actual | Status |
|----------|----------|--------|--------|
| Scenario 1 | X | X | PASS |

### Edge Cases
- [ ] Edge case 1: PASS/FAIL
- [ ] Edge case 2: PASS/FAIL

## Issues Found
1. Issue description (Severity: Low/Medium/High)

## Sign-off
- [ ] All acceptance criteria verified
- [ ] No critical issues
- [ ] Ready for release
```

## Status Transitions

```
IDEA_CAPTURED → RESEARCH_COMPLETE → PLAN_APPROVED → PRD_READY
                                                        ↓
DOCS_UPDATED ← RELEASE_READY ← REVIEW_OK ← TASKLIST_READY
                                              ↓
                                    (loop: IMPLEMENT_STEP_OK)
```

## Working with Claude Code

### Starting a New Feature
```
User: "Add feature X"
Claude:
1. Create docs/research/feature-x.md
2. Analyze codebase
3. Create docs/plan/feature-x.md
4. Wait for approval
5. Create docs/prd/feature-x.prd.md
6. Create docs/tasklist/feature-x.md
7. Implement tasks one by one
8. Create reports/qa/feature-x.md
```

### Continuing Work
```
User: "Continue on feature X"
Claude:
1. Check docs/tasklist/feature-x.md
2. Find next uncompleted task
3. Implement and verify
4. Update tasklist status
```

### Handling Blockers
```
User: "I'm blocked on task Y"
Claude:
1. Add blocker to tasklist
2. Propose alternatives
3. Update plan if needed
```

## Best Practices

1. **One task at a time**: Complete and verify each task before moving on
2. **Clear acceptance criteria**: Every task should have testable criteria
3. **Status tracking**: Keep document statuses up to date
4. **Incremental commits**: Commit after each completed task
5. **Documentation as source of truth**: Reference docs, not chat history
