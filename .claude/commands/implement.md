---
name: implement
description: Implement tasks from a tasklist for a ticket
---

# /implement Command

Implement remaining tasks from a ticket's tasklist.

## Usage
```
/implement <ticket-name>
```

## Prerequisites
- Tasklist exists at `docs/tasklist/<ticket-name>.md`
- PRD exists and is READY
- Plan exists and is APPROVED

## Process

1. Read the tasklist
2. Find next incomplete task
3. Implement the task
4. Verify acceptance criteria
5. Mark task complete
6. Repeat until all tasks done

## Guidelines

- Work on one task at a time
- Follow existing code patterns
- Run tests after each task
- Update documentation if needed
- Use conventional commits

## Example

```
User: /implement auth-refresh

Claude:
Implementing auth-refresh feature...

Tasklist status:
- [x] Add refresh token entity
- [x] Create refresh token repository
- [ ] Implement refresh endpoint ← Working on this
- [ ] Add token rotation logic
- [ ] Update tests

Implementing refresh endpoint...
[code changes]

✅ Task complete. Verification passed.

Updated docs/tasklist/auth-refresh.md
```
