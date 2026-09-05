import { test, expect } from '@chromatic-com/playwright';
import { LoginPage } from '../../pages/LoginPage';
import { DashboardPage } from '../../pages/DashboardPage';

/**
 * Визуальные регрессионные тесты с Chromatic
 * 
 * Эти тесты создают скриншоты страниц и сравнивают их с baseline
 * для обнаружения визуальных изменений.
 */

test.describe('Visual Regression - Public Pages', () => {
  test('Login page matches baseline', async ({ browser }) => {
    // Создаем новый контекст без авторизации
    const context = await browser.newContext({ storageState: undefined });
    const page = await context.newPage();
    
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    
    // Ждем загрузки всех элементов
    await expect(page.locator('[data-testid="login-email"]')).toBeVisible();
    await expect(page.locator('[data-testid="login-password"]')).toBeVisible();
    
    // Создаем визуальный снапшот
    await expect(page).toHaveScreenshot('login-page.png', {
      fullPage: true,
    });
    
    await context.close();
  });

  test('Login page with error matches baseline', async ({ browser }) => {
    // Создаем новый контекст без авторизации
    const context = await browser.newContext({ storageState: undefined });
    const page = await context.newPage();
    
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    
    // Вводим неверные данные
    await loginPage.login('wrong@email.com', 'wrongpassword');
    
    // Ждем появления ошибки
    await loginPage.expectError();
    
    // Снапшот страницы с ошибкой
    await expect(page).toHaveScreenshot('login-page-error.png', {
      fullPage: true,
    });
    
    await context.close();
  });
});

test.describe('Visual Regression - Authenticated Pages', () => {
  test.use({
    storageState: 'e2e/.auth/admin.json',
  });

  test('Dashboard page matches baseline', async ({ page }) => {
    const dashboardPage = new DashboardPage(page);
    await dashboardPage.goto();
    await dashboardPage.expectPageLoaded();
    
    await expect(page).toHaveScreenshot('dashboard-page.png', {
      fullPage: false,
      mask: [
        // Статистика, графики, лента активности и таблицы зависят от данных
        // (в прогоне другие тесты создают данные)
        page.locator('[data-testid="stat-card"], .MuiCard-root, .recharts-wrapper, [role="application"], table'),
      ],
    });
  });
});

test.describe('Visual Regression - Responsive', () => {
  test.use({
    storageState: 'e2e/.auth/admin.json',
  });

  test('Dashboard on mobile matches baseline', async ({ page }) => {
    // Устанавливаем мобильный viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    const dashboardPage = new DashboardPage(page);
    await dashboardPage.goto();
    await dashboardPage.expectPageLoaded();
    
    await expect(page).toHaveScreenshot('dashboard-mobile.png', {
      fullPage: false,
      mask: [
        // Данные дашборда меняются по ходу прогона (другие тесты создают тесты/юзеров):
        // стат-карточки, графики, лента активности, таблицы
        page.locator('[data-testid="stat-card"], .MuiCard-root, .recharts-wrapper, [role="application"], table'),
      ],
    });
  });

  test('Dashboard on tablet matches baseline', async ({ page }) => {
    // Устанавливаем tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    
    const dashboardPage = new DashboardPage(page);
    await dashboardPage.goto();
    await dashboardPage.expectPageLoaded();
    
    await expect(page).toHaveScreenshot('dashboard-tablet.png', {
      fullPage: false,
      mask: [
        // Данные дашборда меняются по ходу прогона (другие тесты создают тесты/юзеров):
        // стат-карточки, графики, лента активности, таблицы
        page.locator('[data-testid="stat-card"], .MuiCard-root, .recharts-wrapper, [role="application"], table'),
      ],
    });
  });
});
