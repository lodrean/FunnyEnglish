---
name: qa
description: Run QA verification for implemented ticket
---

# /qa Command

Run quality assurance tests and create QA report.

## Usage
```
/qa <ticket-name>
```

## Prerequisites
- Implementation complete
- Review approved

## Process

1. Read PRD and tasklist for acceptance criteria
2. Run all automated tests
3. Verify each acceptance criterion
4. Test edge cases
5. Create QA report
6. Sign off or request fixes

## Commands

```bash
# Run all tests
./gradlew test
./gradlew :shared:allTests
cd admin-web && npm test

# Build verification
./gradlew build
cd admin-web && npm run build
```

## QA Report Location
`reports/qa/<ticket-name>.md`

## Report Template

```markdown
# QA Report: <Ticket>

## Date
YYYY-MM-DD

## Summary
PASS | FAIL

## Automated Tests
- Backend: PASS/FAIL
- Shared: PASS/FAIL
- Admin: PASS/FAIL

## Acceptance Criteria
- [ ] Criterion 1: PASS/FAIL
- [ ] Criterion 2: PASS/FAIL

## Edge Cases
- [ ] Empty data
- [ ] Invalid input
- [ ] Error scenarios

## Issues
1. Issue (Severity)

## Sign-off
- [ ] All criteria verified
- [ ] Ready for release
```
