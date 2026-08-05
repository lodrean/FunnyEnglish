# Tasks: fix-mvp-acceptance

## Spec / docs
- [x] 1. Approve delta specs (admin-login, theme-toggle, wasm-onboarding).
- [x] 2. Bump versions in main specs and add changelog entries.
- [x] 3. Update `memory.md` §5 с решениями.

## Backend
- [x] 4. Change `application.yml` default admin email to `admin@sotospeak.com`.
- [x] 5. Update `AdminUserInitializer` to upsert admin by role fallback and sync email.
- [x] 6. Add backend test for admin login credentials (`AuthControllerIntegrationTest`).

## Admin-web
- [x] 7. Verify `ThemeProvider` wraps app and `Header` toggle works; add `data-testid="theme-toggle-button"`.
- [x] 8. Add Playwright E2E test for theme toggle (`e2e/tests/theme-toggle.spec.ts`).

## App
- [x] 9. `SettingsViewModel.themeMode` → `App.kt` theme resolution (already wired; default changed to `SYSTEM`).
- [x] 10. `MainActivity` splash background uses `splash_background_light`/`splash_background_dark` per `theme_mode`.
- [x] 11. Use existing vector icons `SpeakingIcons.Play/Mic/Send` in `OnboardingScreen` for WASM compatibility.
- [x] 12. Update debug-menu text about baseUrl override (applied immediately).
- [x] 13. Existing desktopTest/UI tests cover onboarding; E2E-cmp smoke test verifies WASM onboarding renders.

## Quality gates
- [x] 14. Run backend tests, desktopTest, wasm compile, app assembleDebug, admin-web vitest + playwright, e2e-cmp.
- [x] 15. Manual smoke test: admin login (`auth.spec.ts` + `auth.setup.ts`), theme toggles (`theme-toggle.spec.ts`), WASM onboarding (`e2e-cmp smoke`).
