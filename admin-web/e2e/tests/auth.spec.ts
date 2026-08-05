import { test, expect, BrowserContext, Page } from '@chromatic-com/playwright';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';

/**
 * E2E тесты для авторизации в админ-панели
 * 
 * Эти тесты используют чистый browser context (без storageState)
 * для тестирования процесса логина/логаута
 */
test.describe('Authentication', () => {
  let context: BrowserContext;
  let page: Page;
  let loginPage: LoginPage;
  let dashboardPage: DashboardPage;

  test.beforeEach(async ({ browser }) => {
    // Создаем новый чистый context для каждого теста
    // Это гарантирует что нет сохраненной авторизации
    context = await browser.newContext({
      storageState: undefined, // No auth state
    });
    page = await context.newPage();
    loginPage = new LoginPage(page);
    dashboardPage = new DashboardPage(page);
  });

  test.afterEach(async () => {
    // Закрываем context после каждого теста
    await context.close();
  });

  test('должен отображать страницу логина', async () => {
    await loginPage.goto();
    
    // Проверяем наличие основных элементов
    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.submitButton).toBeVisible();
  });

  test('должен показывать ошибку при неверных учетных данных', async () => {
    await loginPage.goto();
    await loginPage.login('wrong@email.com', 'wrongpassword');
    
    // Проверяем сообщение об ошибке
    await loginPage.expectError();
  });

  test('должен успешно логиниться с правильными учетными данными', async () => {
    // Используем тестовые данные из переменных окружения или дефолтные
    const email = process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com';
    const password = process.env.TEST_ADMIN_PASSWORD || 'admin123';
    
    await loginPage.goto();
    await loginPage.login(email, password);
    
    // Проверяем редирект на дашборд
    await loginPage.expectSuccessfulLogin();
    
    // Проверяем что дашборд загрузился
    await dashboardPage.expectPageLoaded();
  });

  test('должен валидировать обязательные поля', async () => {
    await loginPage.goto();
    
    // Отправляем пустую форму
    await loginPage.clickSubmit();
    
    // Проверяем наличие ошибки валидации (появляется в Alert)
    await loginPage.expectError('Please enter both email and password');
  });

  test('должен выходить из системы', async () => {
    // Сначала логинимся
    const email = process.env.TEST_ADMIN_EMAIL || 'admin@sotospeak.com';
    const password = process.env.TEST_ADMIN_PASSWORD || 'admin123';
    
    await loginPage.goto();
    await loginPage.login(email, password);
    await loginPage.expectSuccessfulLogin();
    
    // Выходим через очистку localStorage и куки
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
    await context.clearCookies();
    
    // Переходим на страницу логина
    await loginPage.goto();
    
    // Проверяем что снова видим форму логина
    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
  });
});

test.describe('Protected Routes', () => {
  let context: BrowserContext;
  let page: Page;

  test.beforeEach(async ({ browser }) => {
    // Создаем новый чистый context без авторизации
    context = await browser.newContext({
      storageState: undefined,
    });
    page = await context.newPage();
  });

  test.afterEach(async () => {
    await context.close();
  });

  test('должен редиректить неавторизованного пользователя на логин', async () => {
    await page.goto('/dashboard');
    
    // Проверяем что произошел редирект на /login
    await expect(page).toHaveURL(/.*login.*/);
  });

  test('должен редиректить неавторизованного пользователя при доступе к пользователям', async () => {
    await page.goto('/users');
    
    await expect(page).toHaveURL(/.*login.*/);
  });
});
