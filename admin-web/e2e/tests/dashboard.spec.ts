import { test, expect } from '@chromatic-com/playwright';
import { DashboardPage } from '../pages/DashboardPage';

/**
 * E2E тесты для Dashboard страницы
 */
test.describe('Dashboard', () => {
  let dashboardPage: DashboardPage;

  test.beforeEach(async ({ page }) => {
    // Auth state is already set up, just create page object
    dashboardPage = new DashboardPage(page);
    await dashboardPage.goto();
  });

  test('должен отображать заголовок дашборда', async () => {
    await dashboardPage.expectPageLoaded();
    
    const title = await dashboardPage.getPageTitle();
    expect(title.toLowerCase()).toContain('dashboard');
  });

  test('должен отображать боковое меню навигации', async () => {
    await dashboardPage.ensureSidebarOpen();
    await expect(dashboardPage.sidebar).toBeVisible();
  });

  test('должен отображать статистические карточки', async () => {
    const count = await dashboardPage.getStatsCardsCount();
    expect(count).toBeGreaterThan(0);
  });

  test('должен иметь ссылки навигации', async () => {
    await dashboardPage.ensureSidebarOpen();
    const links = await dashboardPage.navigationLinks.count();
    expect(links).toBeGreaterThan(0);
  });

  test('должен содержать элементы меню', async () => {
    await dashboardPage.ensureSidebarOpen();
    // Проверяем наличие основных разделов в меню
    const hasDashboard = await dashboardPage.hasNavigationItem('Dashboard');
    const hasUsers = await dashboardPage.hasNavigationItem('Users');
    const hasContent = await dashboardPage.hasNavigationItem('Content');
    
    // Должен быть виден хотя бы Dashboard и один из разделов
    expect(hasDashboard && (hasUsers || hasContent)).toBeTruthy();
  });

  test('должен открывать меню пользователя', async ({ page }) => {
    // Click on user menu button
    await page.click('[data-testid="user-menu-button"]');
    
    // Check that the menu is visible using the logout item
    await expect(page.locator('[data-testid="logout-menu-item"]')).toBeVisible();
  });
});

test.describe('Dashboard Responsive', () => {
  test('должен корректно отображаться на мобильном', async ({ page }) => {
    // Устанавливаем мобильный viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Navigate to dashboard (auth state is already set up)
    await page.goto('/');
    
    // Check that the page loaded
    await expect(page.locator('[data-testid="page-title"]')).toBeVisible();
    
    // Проверяем что контент адаптирован
    await expect(page.locator('main, [role="main"]').first()).toBeVisible();
  });
});
