import { test, expect } from '@chromatic-com/playwright';
import { LoginPage } from '../../pages/LoginPage';
import { DashboardPage } from '../../pages/DashboardPage';
import { CategoriesPage } from '../../pages/CategoriesPage';
import { TestsPage } from '../../pages/TestsPage';

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
      maxDiffPixels: 500, // Допускаем небольшие различия
      mask: [
        // Статистика, графики, лента активности и таблицы зависят от данных
        // (в прогоне другие тесты создают данные)
        page.locator('[data-testid="stat-card"], .MuiCard-root, .recharts-wrapper, [role="application"], table'),
      ],
    });
  });

  test('Categories page matches baseline', async ({ page }) => {
    const categoriesPage = new CategoriesPage(page);
    await categoriesPage.goto();
    await categoriesPage.expectPageLoaded();
    
    await expect(page).toHaveScreenshot('categories-page.png', {
      fullPage: true,
    });
  });

  test('Tests page matches baseline', async ({ page }) => {
    const testsPage = new TestsPage(page);
    await testsPage.goto();
    await testsPage.expectPageLoaded();
    
    await expect(page).toHaveScreenshot('tests-page.png', {
      fullPage: false, // Только видимая часть, без скролла
      maxDiffPixels: 1000, // Допускаем различия до 1000 пикселей
    });
  });
});

test.describe('Visual Regression - Component States', () => {
  test.use({
    storageState: 'e2e/.auth/admin.json',
  });

  test('Test editor form matches baseline', async ({ page }) => {
    // Переходим к созданию нового теста
    await page.goto('/content/tests/new');
    await page.waitForLoadState('networkidle');
    
    // Ждем загрузки формы - ищем по тексту "Create Test"
    await expect(page.locator('text=Create Test')).toBeVisible();
    
    // Снапшот всей страницы создания теста
    await expect(page).toHaveScreenshot('test-editor-form.png', {
      fullPage: true,
      mask: [
        // Маскируем поле с динамическим id
        page.locator('input').first(),
      ],
    });
  });

  test('Question card matches baseline', async ({ page }) => {
    // Детерминированность: «первый тест в таблице» меняется от прогона к прогону
    // (другие тесты создают свои) — создаём свой тест с фиксированным именем через API
    // и открываем его через поиск.
    const VISUAL_TEST_TITLE = 'Visual Baseline Test';
    const loginResp = await page.request.post('/api/auth/login', {
      data: { email: 'admin@funnyenglish.com', password: 'admin123' },
    });
    const { token } = await loginResp.json();
    const headers = { Authorization: `Bearer ${token}` };

    const testsResp = await page.request.get('/api/admin/tests', { headers });
    const tests = await testsResp.json();
    let visualTest = tests.find((t: any) => t.title === VISUAL_TEST_TITLE);
    if (!visualTest) {
      const categoriesResp = await page.request.get('/api/categories', { headers });
      const categories = await categoriesResp.json();
      const createResp = await page.request.post('/api/admin/tests', {
        headers,
        data: {
          categoryId: categories[0].id,
          title: VISUAL_TEST_TITLE,
          description: 'Deterministic test for visual baseline',
          difficulty: 'EASY',
          pointsReward: 10,
          isPublished: false,
          questions: [{
            type: 'TEXT_SELECT', text: 'Visual question?', displayOrder: 0, points: 1,
            answers: [
              { text: 'yes', isCorrect: true, displayOrder: 0 },
              { text: 'no', isCorrect: false, displayOrder: 1 },
            ],
          }],
        },
      });
      visualTest = await createResp.json();
    }

    await page.goto(`/content/tests/${visualTest.id}`);
    await page.locator('[data-testid="questions-tab"]').waitFor({ timeout: 15000 });
    await page.waitForTimeout(1000);

    // Делаем снапшот страницы редактирования
    await expect(page).toHaveScreenshot('test-edit-page.png', {
      fullPage: false,
      maxDiffPixels: 100, // Небольшие различия допустимы
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
      maxDiffPixels: 500,
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
      maxDiffPixels: 500,
      mask: [
        // Данные дашборда меняются по ходу прогона (другие тесты создают тесты/юзеров):
        // стат-карточки, графики, лента активности, таблицы
        page.locator('[data-testid="stat-card"], .MuiCard-root, .recharts-wrapper, [role="application"], table'),
      ],
    });
  });
});
