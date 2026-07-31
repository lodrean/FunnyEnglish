import { test, expect } from '@chromatic-com/playwright';

/**
 * E2E тесты для навигации по админ-панели
 */
test.describe('Navigation', () => {
  test.beforeEach(async ({ page }) => {
    // Auth state is already set up
    await page.goto('/');
    await page.waitForLoadState('networkidle');
  });

  test('должен навигировать между разделами', async ({ page }) => {
    const sections = [
      { id: 'dashboard', name: 'Dashboard', url: '/' },
      { id: 'content', name: 'Content', url: '/content/categories' },
      { id: 'users', name: 'Users', url: '/users' },
    ];

    for (const section of sections) {
      // Find and click the nav item using data-testid
      const navItem = page.locator(`[data-testid="nav-${section.id}"]`).first();
      
      if (await navItem.isVisible().catch(() => false)) {
        await navItem.click();
        await page.waitForLoadState('networkidle');
        
        // Verify we're on the right page
        if (section.url === '/') {
          await expect(page.locator('[data-testid="page-title"]')).toContainText('Dashboard');
        } else {
          await expect(page).toHaveURL(new RegExp(section.url.replace(/\//g, '\\/')));
        }
      }
    }
  });

  test('должен поддерживать прямой переход по URL', async ({ page }) => {
    const routes = ['/content/categories', '/content/tests', '/users', '/analytics'];
    
    for (const route of routes) {
      await page.goto(route);
      await page.waitForLoadState('networkidle');
      
      // Check that page loaded (no 404)
      const notFound = page.locator('text=404, text=Not Found, text=Страница не найдена').first();
      const is404 = await notFound.isVisible().catch(() => false);
      
      expect(is404).toBeFalsy();
    }
  });

  test('должен подсвечивать активный раздел', async ({ page }) => {
    await page.goto('/content/categories');
    await page.locator('[data-testid="page-title"]').waitFor({ timeout: 15000 });

    // На mobile/tablet пункты меню в закрытом drawer — открываем его
    const activeNav = page.locator('[data-testid="nav-content"]').first();
    if (!(await activeNav.isVisible().catch(() => false))) {
      await page.getByRole('button', { name: 'toggle sidebar' }).first().click();
      await expect(activeNav).toBeVisible({ timeout: 5000 });
    }

    // Check that the nav item has selected state
    const isSelected = await activeNav.getAttribute('aria-selected');
    
    // The nav item should be selected (MUI ListItemButton uses aria-selected)
    expect(isSelected === 'true' || await activeNav.evaluate(el => 
      el.classList.contains('Mui-selected')
    )).toBeTruthy();
  });
});
