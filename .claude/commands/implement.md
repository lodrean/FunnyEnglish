---
name: implement
description: Start or continue implementation of a planned ticket
---

# /implement Command

Start or continue implementing a ticket with approved plan.

## Usage
```
/implement <ticket-name>
```

## Prerequisites
- Plan approved at `docs/plan/<ticket-name>.md`
- Tasklist exists at `docs/tasklist/<ticket-name>.md`

## Process

1. Read tasklist to find next incomplete task
2. Review acceptance criteria
3. Implement the task
4. Verify criteria are met
5. Mark task complete
6. Commit changes
7. Move to next task or finish

## Workflow

```
1. Read docs/tasklist/<ticket>.md
2. Find first unchecked task
3. Implement changes
4. Run: ./gradlew build (or npm run build)
5. Mark task [x] in tasklist
6. Commit: git commit -m "feat(<scope>): <task>"
7. Repeat until all tasks done
```

## Completion Criteria

Task is done when:
- [ ] Code compiles without errors
- [ ] All acceptance criteria verified
- [ ] Tasklist updated with [x]
- [ ] Changes committed

All tasks done when:
- [ ] All tasks marked complete
- [ ] All tests pass
- [ ] Ready for review
