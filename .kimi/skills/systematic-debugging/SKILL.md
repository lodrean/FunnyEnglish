---
name: systematic-debugging
description: Use when encountering any bug, test failure, build failure, or unexpected behavior, before proposing fixes - 4-phase root cause process; no fixes without root cause investigation; 3+ failed fixes means question the architecture. Adapted from obra/superpowers (MIT).
---

# Systematic Debugging

**Core principle:** ALWAYS find root cause before attempting fixes. Symptom fixes are failure.

## The Iron Law

```
NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST
```

If you haven't completed Phase 1, you cannot propose fixes.

## When to Use

Use for ANY technical issue: test failures, bugs, unexpected behavior, performance
problems, build failures, integration issues.

**Use ESPECIALLY when:** under time pressure, "just one quick fix" seems obvious,
you already tried a fix that didn't work, or you don't fully understand the issue.

**First step in this project:** check `memory.md` раздел «Известные грабли» — many
recurring classes of bugs (CORS masking, proxy/VPN on emulator, Jackson Kotlin
module, Maestro exact matching, pipes masking exit codes, ...) are already
documented there with diagnosis recipes. If the fix reveals a NEW грабля — add it
to `memory.md` (and/or `bd remember`).

## The Four Phases

You MUST complete each phase before proceeding to the next.

### Phase 1: Root Cause Investigation

**BEFORE attempting ANY fix:**

1. **Read Error Messages Carefully** — full stack traces, line numbers, error
   codes. Don't skip warnings.
2. **Reproduce Consistently** — exact steps, every time? If not reproducible →
   gather more data, don't guess.
3. **Check Recent Changes** — git diff/log, new dependencies, config/env changes.
4. **Gather Evidence in Multi-Component Systems** — this project IS one
   (app → backend → PostgreSQL/MinIO; admin → nginx → backend; emulator →
   proxy → host). For EACH component boundary: log what enters, what exits,
   verify env/config propagation. Run once to see WHERE it breaks, then
   investigate that component. (Example from our history: UI "Invalid email" →
   network tab → CORS 403 → Spring CORS config, not auth logic.)
5. **Trace Data Flow** — when error is deep in the call stack: where does the
   bad value originate? What called this with bad value? Keep tracing up to the
   source. Fix at source, not at symptom. (Example: `is_correct=false` for all
   answers → trace to missing `jackson-module-kotlin`, not the scoring code.)

### Phase 2: Pattern Analysis

1. **Find Working Examples** — similar working code in this codebase
   (e.g. working Repository vs broken one).
2. **Compare Against References** — read reference implementation COMPLETELY,
   don't skim.
3. **Identify Differences** — list every difference, however small.
4. **Understand Dependencies** — required config, env, feature flags
   (`SOTOSPEAK_ENABLE_*`, `SOTOSPEAK_API_BASE_URL`), platform differences
   (Android/Desktop/WASM behave differently — Napier, kotest, audio).

### Phase 3: Hypothesis and Testing

1. **Form Single Hypothesis** — "I think X is the root cause because Y". Write
   it down. Be specific.
2. **Test Minimally** — smallest possible change, one variable at a time.
3. **Verify Before Continuing** — worked? → Phase 4. Didn't? NEW hypothesis.
   DON'T stack fixes on top of each other.
4. **When You Don't Know** — say "I don't understand X", research more, ask the
   user. Don't pretend.

### Phase 4: Implementation

1. **Create Failing Test Case** — simplest reproduction; automated test if
   possible (see `tdd-discipline` skill).
2. **Implement Single Fix** — root cause only. ONE change. No "while I'm here".
3. **Verify Fix** — test passes, no other tests broken, original symptom gone.
   Use `verification-before-completion` skill before claiming success.
4. **If Fix Doesn't Work** — STOP. Count attempts:
   - < 3: return to Phase 1 with new information.
   - **≥ 3: STOP and question the architecture (see below).** No Fix #4 without
     architectural discussion with the user.

### If 3+ Fixes Failed: Question Architecture

Pattern indicating architectural problem: each fix reveals new shared
state/coupling elsewhere; fixes require "massive refactoring"; each fix creates
new symptoms.

STOP and ask: Is this pattern fundamentally sound? Refactor vs. continue fixing
symptoms? **Discuss with the user before more attempts.**

This is not a failed hypothesis — this is a wrong architecture.

## Supporting Techniques

### Root-Cause Tracing (backward through the stack)

When a bad value surfaces deep in the stack, don't fix where it surfaces:
1. Identify the exact bad value/state at the failure point.
2. Ask "where did this come from?" — one frame up.
3. Repeat until you find where correct data becomes incorrect.
4. Fix THERE. Add validation at the source boundary.

### Defense in Depth (after finding root cause)

One fix at the source is necessary but often not sufficient for fragile paths:
- Add validation at each layer the bad data crosses (DTO → service → entity).
- Make the failure LOUD at the earliest layer (clear error, not silent default).
- Example: Jackson Kotlin module fix + DTO validation + test on the API contract.

### Condition-Based Waiting (replace arbitrary timeouts)

Never `sleep(5)` / `waitForTimeout` and hope. Poll for the actual condition:
- E2E: wait for `[data-testid="page-title"]`, not `networkidle` (грабля №22).
- Scripts: poll health endpoint `until curl -sf .../actuator/health; do sleep 1; done`.
- Emulators: wait for `adb shell getprop sys.boot_completed`.

## Red Flags - STOP and Return to Phase 1

- "Quick fix for now, investigate later"
- "Just try changing X and see"
- "Add multiple changes, run tests"
- "Skip the test, I'll manually verify"
- "It's probably X"
- Proposing solutions before tracing data flow
- "One more fix attempt" (when already tried 2+)

## Common Rationalizations

| Excuse | Reality |
|--------|---------|
| "Issue is simple" | Simple issues have root causes too. Process is fast for simple bugs. |
| "Emergency, no time" | Systematic is FASTER than guess-and-check thrashing. |
| "Multiple fixes at once saves time" | Can't isolate what worked. Causes new bugs. |
| "I see the problem" | Seeing symptoms ≠ understanding root cause. |
| "One more fix" (after 2+ failures) | 3+ failures = architectural problem. |

## Quick Reference

| Phase | Key Activities | Success Criteria |
|-------|---------------|------------------|
| 1. Root Cause | Read errors, reproduce, recent changes, boundary evidence, trace data | Understand WHAT and WHY |
| 2. Pattern | Working examples, compare, differences | Identify the delta |
| 3. Hypothesis | Single theory, minimal test | Confirmed or new hypothesis |
| 4. Implementation | Failing test, single fix, verify | Bug resolved, tests green |

## When Process Reveals "No Root Cause"

If investigation shows the issue is truly environmental/timing/external:
document what you investigated, implement appropriate handling (retry, timeout,
clear error), add logging for future investigation. But 95% of "no root cause"
cases are incomplete investigation.
