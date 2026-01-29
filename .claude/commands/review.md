---
name: review
description: Perform code review on implemented changes
---

# /review Command

Review code changes for a completed implementation.

## Usage
```
/review <ticket-name>
# or
/review  # reviews current branch changes
```

## Process

1. Get list of changed files
2. Review each file against checklist
3. Check for security issues
4. Verify conventions compliance
5. Run tests
6. Provide feedback or approve

## Checklist

### Code Quality
- [ ] Clear naming
- [ ] Single responsibility
- [ ] No dead code
- [ ] Proper error handling

### Security
- [ ] No hardcoded secrets
- [ ] Input validation
- [ ] Auth checks present

### Conventions
- [ ] Follows project standards
- [ ] Consistent patterns

### Tests
- [ ] Tests present
- [ ] Edge cases covered

## Commands

```bash
# View changes
git diff develop...HEAD

# Run tests
./gradlew test
cd admin-web && npm test

# Check build
./gradlew build
```

## Output Format

```
## Review: <ticket-name>

### Summary
APPROVED | CHANGES_REQUESTED

### Files Reviewed
- file1.kt: OK
- file2.tsx: Issues found

### Issues
[SEVERITY] Category: Description
Location: file:line

### Approval
- [ ] Code quality OK
- [ ] Security OK
- [ ] Tests OK
```
