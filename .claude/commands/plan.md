---
name: plan
description: Create implementation plan for a researched ticket
---

# /plan Command

Create an implementation plan after research is complete.

## Usage
```
/plan <ticket-name>
```

## Prerequisites
- Research document exists at `docs/research/<ticket-name>.md`
- Research status is COMPLETE

## Process

1. Review research document
2. Create plan at `docs/plan/<ticket-name>.md`
3. Define approach and architecture
4. List implementation steps
5. Identify risks and mitigations
6. Request approval

## Template

```markdown
# Plan: <Ticket Name>

## Ticket
<ticket-name>

## Status
DRAFT

## Approach
<high-level approach>

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
| Risk | How to handle |

## Dependencies
- <external dependency>

## Questions for Review
- Question about approach?
```
