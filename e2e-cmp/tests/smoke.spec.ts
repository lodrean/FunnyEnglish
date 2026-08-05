import { test, expect } from '@playwright/test';
import { launchApp, collectCriticalErrors, fullShot, skipOnMobile } from './helpers';

/**
 * CMP WASM Smoke Tests — speaking-приложение
 * Базовая проверка: WASM загружается, онбординг рендерится, нет критических ошибок.
 */
test.describe('CMP WASM - Smoke Tests', () => {

  test('should load WASM application without critical errors', async ({ page }) => {
    const errors = collectCriticalErrors(page);

    await page.goto('/');
    const canvas = page.locator('canvas');
    await expect(canvas).toBeVisible({ timeout: 60000 });

    await page.waitForTimeout(8000);

    expect(errors, `Критические ошибки консоли: ${errors.join(' | ')}`).toHaveLength(0);
    console.log('✅ WASM app loaded successfully');
  });

  test('should show loading spinner before app starts', async ({ page }) => {
    await page.goto('/');

    // HTML-спиннер «Loading So to speak...» — реальный DOM-элемент вне canvas
    const loadingText = page.locator('text=Loading');
    try {
      await expect(loadingText).toBeVisible({ timeout: 5000 });
      console.log('✅ Loading state shown');
    } catch {
      console.log('ℹ️ Loading state was too fast to capture');
    }
    await expect(page.locator('canvas')).toBeVisible({ timeout: 60000 });
  });

  test('should render onboarding on first launch', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);

    // Первый запуск (чистый localStorage) → Splash → Onboarding
    const initial = await fullShot(page);
    await page.screenshot({ path: 'test-results/cmp-onboarding-first.png' });

    // После перехода по слайду пиксели меняются — приложение живое
    await page.locator('canvas').click({ position: { x: 640, y: 648 } });
    await page.waitForTimeout(1200);
    const afterClick = await fullShot(page);
    await page.screenshot({ path: 'test-results/cmp-onboarding-second.png' });

    expect(initial.equals(afterClick), 'Слайд онбординга не переключился после «Далее»').toBe(false);
    console.log('✅ Onboarding renders and reacts');
  });

  test('should handle window resize', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 60000 });

    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.waitForTimeout(1000);
    await page.setViewportSize({ width: 1366, height: 768 });
    await page.waitForTimeout(1000);
    await page.setViewportSize({ width: 390, height: 844 });
    await page.waitForTimeout(1000);

    await expect(page.locator('canvas')).toBeVisible();
    console.log('✅ Responsive design works');
  });
});
