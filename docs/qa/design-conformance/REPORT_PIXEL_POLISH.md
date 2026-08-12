# Pixel-polish conformance audit report

**Issue:** `FunnyEnglish-2jo` — pixel polish: buttons, paddings, recording-state inconsistencies  
**Date:** 2026-08-10  
**Auditor:** Kimi Code CLI  
**Source of truth:** `.docs/design-system/mockups.html` v2.0, `tokens.json` v1.3.0  
**Platform audited:** Android debug build (`Pixel_7_API_34`, 1080×2400). WASM captured where possible; recording states are blocked on WASM because the web stub reports `MicPermissionState.Denied`.

---

## 1. Methodology

1. Built and installed Android debug APK (`:app:assembleDebug`).
2. Captured canonical mockup frames from `e2e-cmp/test-results/pixel-report/mockups-light-phone/` (360×800).
3. Captured app frames on Android via Maestro for Training states (idle/recording/recorded).
4. Captured WASM app frames for the screens reachable in the web stub (Library, Topics, Video, Questions, Register/Login, Profile, MySubmissions, Onboarding).
5. Generated scaled diff overlays with `e2e-cmp/compare-wasm-mockups.py` (resizes frames to mockup width, crops/pads to height, computes per-pixel difference ratio).
5. Reviewed diffs visually against tokens and DSM-7 decisions.

---

## 2. Diff summary (WASM light phone)

| Screen | Mockup | App frame | Diff ratio | Notes |
|--------|--------|-----------|------------|-------|
| Library (guest) | `frame-library.png` | `library.png` | 16.4 % | Content/status chips differ; structural match OK. |
| Library (auth) | `frame-library.png` | `library-auth.png` | 16.4 % | Re-captured after fixing the Register → Login link coordinates (see §4.2). |
| Topics | `frame-topics.png` | `topics.png` | 6.9 % | Good match. |
| Video player | `frame-video.png` | `video.png` | 44.9 % | First frame black + native ExoPlayer vs mockup overlay; not a polish issue. |
| Questions + CTA | `frame-questions.png` | `questions.png` | 29.4 % | **After fix:** hero card + compact rows implemented; remaining diff is CTA copy (DSM-7) and guest gate. |
| Register | `frame-register.png` | `register.png` | 14.2 % | **After fix:** input backgrounds transparent; main remaining diff is disabled button in app vs enabled in mockup. |
| Login | `frame-login.png` | `login.png` | 16.9 % | Real Login screen now (previously Register was captured by mistake). Residual diff: mockup shows filled fields + password error state, app shows empty state + disabled CTA (P4). |
| Profile auth | `frame-profile.png` | `profile.png` | 8.8 % | Re-captured with working auth flow. Good match; residual diff is mockup persona data (Анна Смирнова) vs admin account. |
| Profile guest | `frame-profile-guest.png` | N/A | — | Not captured in this WASM flow. |
| MySubmissions | `frame-submissions.png` | `submissions.png` | 7.6 % | Re-captured with working auth flow. Good match. |
| Onboarding 1/2/3 | `frame-onboarding.png` | `onboarding-*.png` | ~9.7 % | Good match; dots/paging align. |
| Training idle | `frame-training.png` | `maestro/android-training-idle.png` | 22.2 % | **After fix:** timer digits now `speaking.text` (mockup `.tnum` inherits body color, not arc color). Residual diff is data (3 vs 5 questions → rec-zone sits higher), status/nav bars. |

Diff images are in `e2e-cmp/test-results/pixel-report/diffs/`.

---

## 3. Issues found

### 3.1 Questions screen — question-card hierarchy ✅ FIXED

**Severity:** Medium  
**Frames:** `frame-questions.png` vs `questions.png`

- Mockup shows the **active/current question** as a large hero card with a `ВОПРОС 1 ИЗ N` eyebrow label and bigger text; remaining questions are compact rows.
- App previously rendered all question cards at the same compact size.

**Fix applied (2026-08-10):**
- `QuestionsScreen.kt`: first item renders `ActiveQuestionCard` (26 dp radius, 24/16 dp padding, eyebrow `speaking.primary`, `QuestionText` 25 sp).
- Remaining items render `CompactQuestionCard` (16 dp radius, 10/12 dp padding, 14 sp body, primary number).
- Diff ratio dropped from ~39 % to **29.4 %**; residual diff is CTA copy (DSM-7 approved) and guest gate.

**Recommendation:** Keep current implementation.

### 3.2 Questions screen — CTA labels

**Severity:** Low (DSM-7 decision)  
**Frames:** `frame-questions.png`

- Mockup CTAs: `Начать Training · 80 сек` / `Сразу Practice · 30 сек`.
- App CTAs: `Тренировка · 3 попытки` / `Практика · 30 сек`.
- Per DSM-7 / `M3_PIXEL_REPORT.md`, the app labels are the approved copy. **No fix required.** Documented here for traceability.

### 3.3 Practice screen — recorder hint ✅ FIXED

**Severity:** Low  
**Frames:** `frame-practice.png` vs `PracticeScreen.kt`

- Mockup has a short label under the recorder (`Ответь на все вопросы подряд одной записью`).
- App previously placed the same guidance only in the bottom info card.

**Fix applied (2026-08-10):**
- `PracticeScreen.kt` `ReadyPhase`: added the recorder hint under `SpeakingRecordButton`.
- Bottom `.practice-note` info card about auto-send is retained (it exists in the mockup too).

**Verification (Maestro, 2026-08-10):** all three Practice states captured on Android via new flow `.maestro/flows/speaking_practice_screenshots.yaml` (requires `DELETE FROM grades; DELETE FROM practice_submissions;` before re-run — backend 409 gate):
- `practice-idle.png`: ring + record button + hint under the button (the fix) + bottom auto-send note card — matches `frame-practice.png`; timer digits are dark (§3.9 fix).
- `practice-recording.png`: REC indicator, orange arc with progress, live waveform, stop button, «Закончить и отправить» hint — all present and correctly colored.
- `practice-sent.png`: success card (`successContainer`), status chip `статус NEW · ждёт проверки`, primary CTA «Вернуться в библиотеку» — matches tokens.
- Minor: chip «1 ЗАПИСЬ НА ВСЕ ВОПРОСЫ» wraps to 2 lines on 360dp width (mockup shows 1 line on 375px frame) — cosmetic, no action.

**Recommendation:** Keep current implementation.

### 3.4 Training screen — level chip vs progress bar ✅ FIXED

**Severity:** Low  
**Frames:** `frame-training.png` vs `docs/qa/design-conformance/maestro/android-training-idle.png`

- Mockup uses a neutral level chip plus a horizontal segmented progress bar for attempts.
- App previously used three dots and showed the attempts block even with 0 recordings.

**Fix applied (2026-08-10):**
- `TrainingScreen.kt`: replaced dot indicators with `AttemptProgressBar` (22×6 dp segments, 4 dp gap).
- Attempts title/list are now hidden when there are no recordings; visible only after the first attempt.
- `TrainingScreenTest.kt` updated to assert the new visibility rule.

**Verification (Maestro, 2026-08-10):**
- Idle: level chip `Уровень 1 · 80 сек` + segmented progress bar with the first segment active matches the mockup.
- Recording: REC dot/label, timer ring, waveform and stop button all present and correctly colored.
- Recorded: attempts title `Попытки · 1 из 3` and `AttemptCard` appear; progress bar shows one completed segment and the current-level segment in the level color.

**Recommendation:** Keep current implementation.

### 3.5 Training recording state — REC indicator and waveform

**Severity:** Low  
**Frames:** `android-training-recording.png`, `android-training-l3-recording.png`

- REC dot + label appear correctly.
- Live waveform renders, but bar heights look somewhat uniform compared to the more dynamic mockup waveform; this may be acceptable because it is driven by real/placeholder amplitude data.
- Timer ring color changes per level (blue → orange → green), matching tokens.

**Recommendation:** No action unless design wants a more pronounced waveform amplitude range.

### 3.6 Training finished / final CTA card

**Severity:** Low  
**Frames:** `android-training-finished.png`

- The final card uses `record` (#FF9F6B) for the primary CTA `Перейти к практике`. In the mockup for Practice this button is also record-colored, so this is consistent.
- Card padding and spacing look adequate.

### 3.7 Practice sent state

**Severity:** Low  
**Frames:** `android-practice-sent2.png`

- Success card uses `successContainer` green and a status chip `статус NEW · ждёт проверки`. Aligns with tokens.
- The bottom `Вернуться в библиотеку` button is primary-strong; matches expected final-CTA pattern.

### 3.8 Login / Register / Profile / MySubmissions

**Severity:** Low  
**Frames:** `frame-login.png` / `frame-register.png` vs `login.png` / `register.png`

- Mockup inputs have transparent background; app inputs previously had `surface` fill, creating a visible box.
- App primary buttons are disabled while required fields are empty; mockups show the enabled/rest state.

**Fix applied (2026-08-10):**
- `SpeakingAuth.kt` (`SpeakingField`): `focusedContainerColor` / `unfocusedContainerColor` set to `Color.Transparent`.
- Label and placeholder colors set to `speaking.textMuted` (unfocused) / `speaking.primary` (focused) to match mockup.

**Remaining:** ~~disabled CTA state~~ — **RESOLVED (2026-08-10, owner decision):** keep current behaviour (buttons disabled until fields are valid). Mockup enabled-rest state is illustrative, not authoritative for validation logic. Documented as by-design; no code or mockup changes.

### 3.9 Training/Practice timer ring — digit color ✅ FIXED

**Severity:** Low  
**Frames:** `frame-training.png` vs `training.png` (Android, Maestro)

- Mockup `.timer-label .tnum` has no color override → digits inherit the body text color (dark navy), only the arc uses the level color.
- App previously colored the digits with `ringColor` (level blue/orange/green).

**Fix applied (2026-08-10):**
- `SpeakingRecording.kt` (`SpeakingTimerRing`): digit `Text` now uses `speaking.text`.
- Verified on-device: digit pixels are #2D3561 (was #4A7FE8). Diff ratio for Training idle is unchanged (22.2 %) because it is dominated by data (3 vs 5 questions) and system bars, not the digits.

**Recommendation:** Keep current implementation.

---

## 4. Platform-specific notes

### Android
- Record button uses system mic icon and a 3-D shadow; consistent with `SpeakingTokens.radius.recorder = 22 dp`.
- Status-bar icons from the device are visible in screenshots and are not part of the app UI.

### WASM
- Recording states cannot be reached because `rememberMicrophonePermissionState` on WASM returns `Denied` by design (`MicPermission.wasmJs.kt`).
- Video control bar is rendered below the player on WASM due to DOM `<video>` always sitting above the Compose canvas — this is documented as by-design.

### Auth-gate login-link capture issue ✅ FIXED (2026-08-10)
- Root cause: `shoot-app-pixel-polish.js` clicked the «Уже есть аккаунт? Войти» link at (180,650), but the actual position of the primary-colored «Войти» text on 360×800 is ~(242,512) — detected via pixel scan of `register.png`.
- Fix: click coordinates changed to (242,512); the full guest → register → login → admin-auth flow now completes (verified by authorized `/api/users/me/profile` and `/api/speaking/submissions/my` requests in the run log).
- Gotcha: `gradlew ... | tail -N` masks the Gradle exit code (`tail` returns 0), so a failed `installDebug`/`buildWasmDist` can look successful — check `BUILD SUCCESSFUL/FAILED` explicitly.

---

## 5. Recommended fix priority

| Priority | Issue | Files likely involved | Status |
|----------|-------|----------------------|--------|
| P1 | Questions active-question hero card + compact rows | `QuestionsScreen.kt` | ✅ Fixed |
| P2 | Training attempt indicator: segmented progress bar | `TrainingScreen.kt` | ✅ Fixed |
| P3 | Practice recorder hint under button | `PracticeScreen.kt` | ✅ Fixed |
| P4 | Auth CTA disabled vs rest state | `LoginScreen.kt`, `RegisterScreen.kt` | ✅ Resolved — owner: keep disabled (by design) |
| P5 | Timer digits color: arc color → `speaking.text` | `SpeakingRecording.kt` (`SpeakingTimerRing`) | ✅ Fixed |
| — | CTA label copy | — | **No fix** (DSM-7 approved) |
| — | Login/Profile/MySubmissions structural/content diffs | — | **No fix** |

---

## 6. Artifacts

- Mockups: `e2e-cmp/test-results/pixel-report/mockups-{light,dark,light-phone}/`
- WASM app frames: `e2e-cmp/test-results/pixel-report/app/`
- WASM diff overlays: `e2e-cmp/test-results/pixel-report/diffs/`
- Android app frames: `docs/qa/design-conformance/android-*.png` (manual) and `docs/qa/design-conformance/maestro/android-training-*.png` (Maestro)
- Android diff overlays: `docs/qa/design-conformance/diffs/`
- Diff generator: `e2e-cmp/compare-wasm-mockups.py`
- Raw summary: `e2e-cmp/test-results/pixel-report/diffs/summary.json`

---

## 7. Next steps

1. ~~Decide on P4 auth CTA disabled vs rest state~~ ✅ Owner decision (2026-08-10): keep disabled-until-valid, mockups unchanged.
2. ~~Fix the WASM/Playwright login-link interaction~~ ✅ Done (2026-08-10).
3. ~~Re-capture Practice idle/recording/sent frames on Android~~ ✅ Done (2026-08-10, `.maestro/flows/speaking_practice_screenshots.yaml`, frames in `e2e-cmp/test-results/pixel-report/app/practice-*.png`).
4. Quality gates after this round: `:composeApp:desktopTest` ✅, `:composeApp:compileKotlinWasmJs` ✅, `buildWasmDist` ✅ (requires `--no-configuration-cache`: Kotlin JS config-cache serialization crash on JDK 21), `:app:installDebug` ✅, Maestro training ✅ (3/3) and practice ✅ (3/3) screenshot flows.
