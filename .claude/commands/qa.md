---
name: qa
description: Run QA testing and generate QA report
---

# /qa Command

Run quality assurance testing and generate QA report.

## Usage
```
/qa <ticket-name>
```

## Prerequisites
- Code implementation is complete
- Code review is done

## Process

1. Read PRD and tasklist
2. Run automated tests
3. Verify acceptance criteria
4. Test edge cases
5. Create QA report

## QA Activities

### Automated Tests
- Run backend unit tests
- Run shared module tests
- Run admin web tests
- Verify build passes

### Manual Verification
- Test each user story
- Verify acceptance criteria
- Check UI/UX if applicable

### Edge Cases
- Empty data scenarios
- Invalid input handling
- Network errors
- Authentication failures

## QA Report

Created at: `reports/qa/<ticket-name>.md`

Contains:
- Test date and summary
- Automated test results
- Acceptance criteria verification
- Edge cases tested
- Issues found with severity
- Sign-off checklist

## Example

```
User: /qa user-profile

Claude:
Running QA for user-profile feature...

Automated Tests:
✅ Backend: 45/45 passed
✅ Mobile: 12/12 passed
✅ Build: SUCCESS

Acceptance Criteria:
✅ User can view profile
✅ User can edit profile
✅ Changes are persisted

Edge Cases:
✅ Empty display name handled
✅ Long text truncated properly
✅ Network error shows message

Issues Found: None

Report: reports/qa/user-profile.md
Status: READY FOR RELEASE
```
