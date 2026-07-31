import { test, expect } from '@chromatic-com/playwright';

/**
 * TC-E2E-001: Create Image Word Match Test
 * Priority: P0 (Critical)
 * Status: ✅ WORKING
 *
 * Auth: storageState из setup-проекта (admin уже залогинен) — ручной логин убран
 * (2026-07-21: он редиректил с /login на / и падал по timeout на input[type=email]).
 */
test.describe('Image Word Match - Create Test ✅', () => {

  test('should navigate to Tests page and see tests list', async ({ page }) => {
    await page.goto('/content/tests');
    await page.locator('[data-testid="page-title"]').waitFor({ timeout: 15000 });

    // Verify page loaded successfully
    await expect(page.locator('[data-testid="page-title"]')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('[data-testid="add-test-button"], button:has-text("Add Test")').first()).toBeVisible();

    // Verify no error message
    const errorVisible = await page.locator('text=Failed to load').isVisible().catch(() => false);
    expect(errorVisible).toBe(false);

    console.log('✅ Tests page loaded successfully');
  });

  test('should click Add Test button and navigate to creation page', async ({ page }) => {
    await page.goto('/content/tests');
    await page.locator('[data-testid="page-title"]').waitFor({ timeout: 15000 });

    // Click Add Test button
    await page.locator('[data-testid="add-test-button"], button:has-text("Add Test")').first().click();

    // Wait for navigation
    await page.waitForURL(/.*new|.*create/, { timeout: 10000 });

    // Verify we're on creation page
    const url = page.url();
    expect(url).toContain('new');

    console.log('✅ Navigated to test creation page:', url);
  });

  test('should see Create Test form with all fields', async ({ page }) => {
    await page.goto('/content/tests/new');

    // Verify title input exists
    const titleInput = page.locator('[data-testid="test-title-input"] input, input[name="title"]').first();
    await expect(titleInput).toBeVisible({ timeout: 15000 });

    // Verify category select and tabs exist
    await expect(page.locator('[role="combobox"]').first()).toBeVisible();
    await expect(page.locator('[data-testid="questions-tab"]')).toBeVisible();
    await expect(page.locator('[data-testid="save-test-button"]')).toBeVisible();

    console.log('✅ Create Test form is visible');
  });
});
