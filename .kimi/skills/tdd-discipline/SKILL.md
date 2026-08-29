---
name: tdd-discipline
description: Use when implementing any feature or bugfix in domain/data/backend logic, before writing implementation code - RED-GREEN-REFACTOR cycle adapted for this project (KMP/backend/admin-web), with explicit exceptions for Compose UI, screenshot tests and prototypes. Adapted from obra/superpowers (MIT).
---

# TDD Discipline (project-adapted)

Write the test first. Watch it fail. Write minimal code to pass.

**Core principle:** If you didn't watch the test fail, you don't know if it tests
the right thing.

## Scope: Where TDD Applies in This Project

**APPLY (strict):**
- `backend/` — services, validation, API contract logic (Spring/Kotlin)
- `shared/`, `core/`, `core:domain`, `core:data` — repositories, mappers,
  use-cases, Result/DataError handling
- `composeApp` ViewModels and non-UI logic (MVI state reducers, `onAction`)
- `admin-web` — api clients, utils, store/query logic (vitest)

**EXCEPTIONS (different mechanism, ask user if unsure):**
- Compose UI rendering — covered by uiTest (`./gradlew :composeApp:uiTest`) and
  Dropshots screenshot gates, not classic TDD. Write UI first, then lock it with
  tags + screenshot/UI tests.
- Throwaway prototypes and exploration spikes — allowed, but DELETE before the
  real implementation starts; re-implement via TDD.
- Configuration files, Gradle scripts, generated code.
- Visual/design work in `design/`.

## The Cycle

```
RED → verify RED (watch it fail) → GREEN (minimal code) →
verify GREEN (watch it pass) → REFACTOR (stay green) → next test
```

### RED — Write Failing Test

- One behavior per test, clear name describing the behavior.
- Test real code; fakes/mocks only when unavoidable (see `android-testing`
  skill for fake repositories, Turbine, UnconfinedTestDispatcher).
- Project test stacks: kotest (KMP commonTest → `desktopTest`), JUnit + Spring
  (backend), vitest (admin-web).

### Verify RED — MANDATORY

Run ONLY the new test first and watch it fail:
- KMP: `./gradlew :composeApp:desktopTest --tests "*ClassName*"`
- Backend: `./gradlew :backend:test --tests "*ClassName*"`
- Admin: `cd admin-web && npx vitest run path/to/test`

Confirm: it FAILS (not errors), failure message is the expected one, it fails
because the feature is missing (not a typo/import error).

- **Test passes immediately?** You're testing existing behavior — fix the test.
- **Test errors?** Fix the error until it fails correctly.

### GREEN — Minimal Code

Write the simplest code that passes. No extra options, no "will be needed"
(YAGNI). Don't refactor other code or bundle improvements.

### Verify GREEN — MANDATORY

Run the test — it passes. Then run the wider suite for regressions
(`:composeApp:desktopTest`, `:backend:test`, `npm test`). Output pristine: no
errors, no new warnings. Follow `verification-before-completion` for claims.

### REFACTOR

Only after green: remove duplication, improve names, extract helpers. Keep
tests green. Don't add behavior.

## Bug Fixes

Bug found → write a failing test reproducing it → fix → verify. Never fix bugs
without a regression test. Verify the red-green cycle: the new test MUST fail
with the fix reverted (this is how грабли like "any answer is correct" get
caught — the test must be able to fail).

## Common Rationalizations

| Excuse | Reality |
|--------|---------|
| "I'll test after" | Tests-after answer "what does this do?"; tests-first answer "what should this do?" You never watched it fail, so you never proved it catches the bug. |
| "Too simple to test" | Simple code breaks. Test takes 30 seconds. |
| "Already manually tested" | No record, no re-run, forgotten edge cases. |
| "Keep written code as reference" | You'll adapt it = testing after. Delete means delete. |
| "Existing code has no tests" | You're improving it. Add tests for what you touch. |
| "TDD slows me down" | Debugging in production is slower. |

## Red Flags - STOP and Start Over

- Code before test
- Test passes immediately
- Can't explain why the test failed
- "Just this once"

## Verification Checklist (before marking done)

- [ ] Every new function/method in TDD scope has a test
- [ ] Watched each test fail before implementing
- [ ] Each test failed for the expected reason
- [ ] Minimal code written per test
- [ ] Full relevant suite passes (real exit code, no pipes!)
- [ ] Regression test for each bugfix fails with fix reverted

## When Stuck

| Problem | Solution |
|---------|----------|
| Don't know how to test | Write the wished-for API, assertion first |
| Test too complicated | Design too complicated — simplify the interface |
| Must mock everything | Too coupled — use Koin DI / constructor injection |
| Huge setup | Extract test helpers (see TestMocks.kt pattern in composeApp) |
