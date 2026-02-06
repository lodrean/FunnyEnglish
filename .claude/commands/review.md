---
name: review
description: Review code changes for a ticket
---

# /review Command

Perform code review for a ticket's implementation.

## Usage
```
/review <ticket-name>
```

## Prerequisites
- Implementation is complete
- Code is committed or in PR

## Process

1. Read PRD and plan
2. Review git diff
3. Check each changed file
4. Verify conventions compliance
5. Run verification commands
6. Provide review feedback

## Review Checklist

### Code Quality
- [ ] Clear, descriptive naming
- [ ] Single responsibility principle
- [ ] Proper error handling
- [ ] No dead code or TODOs

### Conventions
- [ ] Follows conventions.md
- [ ] Consistent with existing code
- [ ] Proper file structure

### Security
- [ ] No hardcoded secrets
- [ ] Input validation present
- [ ] Proper auth checks

### Tests
- [ ] Unit tests for business logic
- [ ] Edge cases covered
- [ ] Tests are deterministic

## Review Comment Format

```
[SEVERITY] Category: Description

SEVERITY levels:
- [BLOCKER] - Must fix before merge
- [CRITICAL] - Security/data issue
- [MAJOR] - Significant issue
- [MINOR] - Style/convention issue
- [SUGGESTION] - Improvement idea
- [QUESTION] - Need clarification
```
