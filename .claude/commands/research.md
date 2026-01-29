---
name: research
description: Start research phase for a new feature or bug fix
---

# /research Command

Start the research phase for a new ticket.

## Usage
```
/research <ticket-name> <description>
```

## Process

1. Create research document at `docs/research/<ticket-name>.md`
2. Analyze the codebase for affected areas
3. Identify existing patterns to follow
4. Assess complexity and risks
5. Document open questions

## Template

```markdown
# Research: <Ticket Name>

## Ticket
<ticket-name>

## Status
IN_PROGRESS

## Objective
<description from user>

## Affected Areas
- File: reason

## Existing Patterns
<how similar things are done>

## Complexity Assessment
- Scope: Low/Medium/High
- Risks: <list>

## Open Questions
- [ ] Question (Status: OPEN)

## Recommendation
<proposed approach>
```
