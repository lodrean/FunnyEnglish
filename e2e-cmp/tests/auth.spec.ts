import { test, expect } from '@playwright/test';
import {
  launchApp, clickCanvas, clipShot, fullShot, expectScreenChanged,
  collectCriticalErrors, collectServerErrors, skipOnMobile,
  passOnboarding, continueAsGuest, openLoginScreen, loginAsAdmin,
  POS,
} from './helpers';

/**
 * CMP WASM Auth Tests — speaking-приложение (guest-first, 2026-08-01)
 * Онбординг (3 слайда) → Library (гость); Register/Login — из гостевого профиля.
 * Координатные клики (canvas-only), assertion'ы по смене пикселей + HTTP.
 */
test.describe('CMP WASM - Authentication', () => {

  test('onboarding: 3 слайда «Далее»/«Начать» ведут в Library (гость)', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);

    // Слайды онбординга центрированы — сравниваем полные скриншоты
    const slide1 = await fullShot(page);
    await clickCanvas(page, POS.onboardingNext, 1200);
    const slide2 = await fullShot(page);
    expectScreenChanged(slide1, slide2, 'слайд 1 → слайд 2');

    await clickCanvas(page, POS.onboardingNext, 1200);
    const slide3 = await fullShot(page);
    expectScreenChanged(slide2, slide3, 'слайд 2 → слайд 3');

    await clickCanvas(page, POS.onboardingNext, 5000); // «Начать»
    const library = await fullShot(page);
    expectScreenChanged(slide3, library, 'слайд 3 → Library');

    await page.screenshot({ path: 'test-results/cmp-auth-library-guest.png' });
    console.log('✅ Onboarding → Library (guest)');
  });

  test('гостевой профиль: «Зарегистрироваться» → Register, оттуда «Войти» → Login', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);
    await passOnboarding(page);

    // Гостевой профиль → Register
    await clickCanvas(page, POS.bottomNavProfile, 3000);
    const guestProfile = await fullShot(page);
    await clickCanvas(page, POS.guestProfileRegister, 2500);
    const register = await fullShot(page);
    expectScreenChanged(guestProfile, register, 'гостевой профиль → Register');
    await page.screenshot({ path: 'test-results/cmp-auth-register.png' });

    // Register → Login («Уже есть аккаунт? Войти»)
    await clickCanvas(page, POS.registerToLoginLink, 2500);
    const login = await fullShot(page);
    expectScreenChanged(register, login, 'Register → Login');
    await page.screenshot({ path: 'test-results/cmp-auth-login.png' });
    console.log('✅ Guest profile → Register ↔ Login navigation works');
  });

  test('успешный логин администратором ведёт в Library', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    const errors = collectCriticalErrors(page);
    await launchApp(page);
    await openLoginScreen(page);

    const loginScreen = await clipShot(page);
    const loginResponse = page.waitForResponse(
      r => r.url().includes('/api/auth/login') && r.request().method() === 'POST',
      { timeout: 15000 },
    );
    await loginAsAdmin(page);
    const response = await loginResponse;
    expect(response.status(), 'POST /api/auth/login').toBe(200);

    await page.waitForTimeout(5000);
    const library = await clipShot(page);
    expectScreenChanged(loginScreen, library, 'Login → Library');
    await page.screenshot({ path: 'test-results/cmp-auth-library-after-login.png' });

    expect(errors, `Критические ошибки консоли: ${errors.join(' | ')}`).toHaveLength(0);
    console.log('✅ Login → Library');
  });

  test('гостевая сессия: онбординг сразу ведёт в Library', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    const serverErrors = collectServerErrors(page);
    await launchApp(page);

    const onboarding = await clipShot(page, { x: 0, y: 0, width: 400, height: 120 });
    await continueAsGuest(page);
    const library = await clipShot(page, { x: 0, y: 0, width: 400, height: 120 });
    expectScreenChanged(onboarding, library, 'Onboarding → Library (guest)');
    await page.screenshot({ path: 'test-results/cmp-auth-library-guest-session.png' });

    // SESSION_STARTED уходит на /api/public/guest-events — 5xx быть не должно
    expect(serverErrors, `HTTP 5xx к backend: ${serverErrors.join(' | ')}`).toHaveLength(0);
    console.log('✅ Guest → Library');
  });

  test('гостевая сессия переживает reload (без повторного онбординга)', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);
    await continueAsGuest(page);
    const libraryBefore = await clipShot(page);

    // Гостевая сессия лежит в localStorage
    const hasGuestKeys = await page.evaluate(() =>
      Object.keys(localStorage).some(k => k.toLowerCase().includes('guest') || k.toLowerCase().includes('session')));
    expect(hasGuestKeys, 'В localStorage нет гостевой сессии').toBe(true);

    await page.reload();
    await expect(page.locator('canvas')).toBeVisible({ timeout: 60000 });
    await page.waitForTimeout(10000);

    // После reload → Splash → сразу Library (не онбординг): клип совпадает с Library до reload
    const libraryAfter = await clipShot(page);
    await page.screenshot({ path: 'test-results/cmp-auth-library-after-reload.png' });
    expect(libraryAfter.equals(libraryBefore), 'После reload открылся не Library (сессия потеряна?)').toBe(true);
    console.log('✅ Guest session persists across reload');
  });
});
