import { test, expect } from '@chromatic-com/playwright';

/**
 * E2E: переключатель светлой/тёмной темы в Header.
 * Проект использует storageState (setup-login), поэтому начинаем с /dashboard.
 */
test.describe('theme toggle', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page.locator('[data-testid="page-title"], h1')).toContainText('Dashboard', { timeout: 15000 });
  });

  test('toggles light/dark mode and persists to localStorage', async ({ page }) => {
    const toggle = page.locator('[data-testid="theme-toggle-button"]');
    await expect(toggle).toBeVisible();

    // Read initial mode from localStorage (default: system preference or previous value)
    const initialMode = await page.evaluate(() => localStorage.getItem('sotospeak-theme-mode'));
    const expectedAfterClick = initialMode === 'dark' ? 'light' : 'dark';
    const initialAriaLabel = await toggle.getAttribute('aria-label');

    await toggle.click();

    await expect.poll(async () =>
      page.evaluate(() => localStorage.getItem('sotospeak-theme-mode'))
    ).toBe(expectedAfterClick);

    // Tooltip aria-label should flip to the opposite mode
    await expect.poll(async () => toggle.getAttribute('aria-label')).not.toBe(initialAriaLabel);

    // Toggle back
    await toggle.click();
    await expect.poll(async () =>
      page.evaluate(() => localStorage.getItem('sotospeak-theme-mode'))
    ).toBe(initialMode);
  });
});
