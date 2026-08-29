---
name: verification-before-completion
description: Use when about to claim work is complete, fixed, or passing, before committing or reporting to the user - requires running verification commands and confirming real output/exit code before making any success claims; evidence before assertions always. Adapted from obra/superpowers (MIT).
---

# Verification Before Completion

**Core principle:** Evidence before claims, always.

**Violating the letter of this rule is violating the spirit of this rule.**

## The Iron Law

```
NO COMPLETION CLAIMS WITHOUT FRESH VERIFICATION EVIDENCE
```

If you haven't run the verification command in this turn, you cannot claim it passes.

## The Gate Function

```
BEFORE claiming any status or expressing satisfaction:

1. IDENTIFY: What command proves this claim?
2. RUN: Execute the FULL command (fresh, complete)
3. READ: Full output, check REAL exit code, count failures
4. VERIFY: Does output confirm the claim?
   - If NO: State actual status with evidence
   - If YES: State claim WITH evidence
5. ONLY THEN: Make the claim

Skip any step = lying, not verifying
```

## Project-Specific Traps (from memory.md грабли)

- **Bash pipes mask exit codes (грабля №30):** `cmd | tail -5` always exits 0 → false
  "PASSED". Check status ONLY via real exit code:
  `cmd > file 2>&1; CODE=$?` then read `$CODE` and the file.
  Same for `./gradlew ... | tail`, `npm run lint | tail`, `maestro test ... | tail`.
- **Masked root causes (CORS case):** UI showed "Invalid email or password", real
  cause was CORS 403. Verifying the symptom is not verifying the fix — reproduce
  the ORIGINAL failing scenario.
- **Linter ≠ build ≠ tests.** `npm run lint` green says nothing about compilation
  or vitest. `./gradlew lint` is NOT configured as a gate in this project at all.

## Verification Commands by Claim

| Claim | Proving command (examples) |
|-------|---------------------------|
| KMP unit tests pass | `./gradlew :composeApp:desktopTest` → exit 0, 0 failures |
| UI tests pass | `./gradlew :composeApp:uiTest` → real exit code |
| Android builds | `./gradlew :app:assembleDebug` → exit 0 |
| Backend works | backend tests + `curl http://localhost:8080/api/actuator/health` |
| Admin unit tests pass | `cd admin-web && npm test` → 0 failed |
| Admin E2E pass | `npx playwright test` (remember грабля №11: port 5173 / SKIP_WEB_SERVER) |
| Maestro flows pass | `maestro test .maestro/` → real exit code (no pipe!) |
| Screenshot gate passes | `./gradlew :composeApp:connectedDebugAndroidTest` |

## Common Failures

| Claim | Requires | Not Sufficient |
|-------|----------|----------------|
| Tests pass | Fresh test output: 0 failures | Previous run, "should pass" |
| Build succeeds | Build command: exit 0 | Linter passing, logs look good |
| Bug fixed | Original symptom re-tested: gone | Code changed, assumed fixed |
| Regression test works | Red-green cycle verified | Test passes once |
| Agent completed | VCS diff shows changes | Agent reports "success" |
| Requirements met | Line-by-line checklist vs spec/plan | Tests passing |

## Red Flags - STOP

- Using "should", "probably", "seems to"
- Expressing satisfaction before verification ("Great!", "Perfect!", "Done!")
- About to commit/push/PR without verification
- Trusting subagent success reports (check the diff independently)
- Relying on partial verification ("ran only the fast tests")
- Thinking "just this once"
- ANY wording implying success without having run verification

## Rationalization Prevention

| Excuse | Reality |
|--------|---------|
| "Should work now" | RUN the verification |
| "I'm confident" | Confidence ≠ evidence |
| "Just this once" | No exceptions |
| "Linter passed" | Linter ≠ compiler ≠ tests |
| "Agent said success" | Verify independently |
| "Partial check is enough" | Partial proves nothing |
| "Exit code was hidden by pipe" | Re-run without pipe (грабля №30) |

## When To Apply

**ALWAYS before:**
- ANY variation of success/completion claims to the user
- Committing, PR creation, closing a bd issue
- Moving to the next task
- Delegating to or accepting results from subagents
