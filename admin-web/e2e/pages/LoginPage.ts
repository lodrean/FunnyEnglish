import { Page, Locator, expect } from '@chromatic-com/playwright';

/**
 * Page Object для страницы логина
 * 
 * Пример использования:
 * ```typescript
 * const loginPage = new LoginPage(page);
 * await loginPage.goto();
 * await loginPage.login('admin@example.com', 'password');
 * ```
 */
export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    // Используем разные стратегии поиска для надежности
    this.emailInput = page.locator('input[type="email"], input[name="email"], input[inputmode="email"]').first();
    this.passwordInput = page.locator('input[type="password"], input[name="password"]').first();
    this.submitButton = page.locator('button[type="submit"], button:has-text("Sign In"), button:has-text("Login")').first();
    this.errorMessage = page.locator('.MuiAlert-root, .error, [role="alert"]').first();
  }

  /**
   * Переход на страницу логина
   */
  async goto() {
    await this.page.goto('/login');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * Заполнение email
   */
  async fillEmail(email: string) {
    await this.emailInput.fill(email);
  }

  /**
   * Заполнение пароля
   */
  async fillPassword(password: string) {
    await this.passwordInput.fill(password);
  }

  /**
   * Клик по кнопке входа
   */
  async clickSubmit() {
    await this.submitButton.click();
  }

  /**
   * Полный процесс логина
   */
  async login(email: string, password: string) {
    await this.fillEmail(email);
    await this.fillPassword(password);
    await this.clickSubmit();
  }

  /**
   * Проверка наличия ошибки
   */
  async expectError(message?: string) {
    await expect(this.errorMessage).toBeVisible();
    if (message) {
      await expect(this.errorMessage).toContainText(message);
    }
  }

  /**
   * Проверка что страница логина загружена
   */
  async expectPageLoaded() {
    await expect(this.emailInput).toBeVisible({ timeout: 10000 });
    await expect(this.passwordInput).toBeVisible({ timeout: 10000 });
    await expect(this.submitButton).toBeVisible({ timeout: 10000 });
  }

  /**
   * Проверка успешного редиректа после логина
   */
  async expectSuccessfulLogin() {
    // Wait for dashboard page to load
    await expect(this.page.locator('[data-testid="page-title"]')).toBeVisible({ timeout: 10000 });
    // Verify we're not on login page
    await expect(this.emailInput).not.toBeVisible({ timeout: 5000 });
  }

  /**
   * Получение ошибок валидации формы
   */
  async getValidationErrors(): Promise<string[]> {
    // Ищем элементы с aria-invalid или текстом ошибки
    const errors: string[] = [];
    
    // Проверяем поле email
    const emailError = this.page.locator('[data-testid="login-email"] ~ p[class*="error"], [data-testid="login-email"] + p[class*="error"], [data-testid="login-email"] ~ [class*="error"]');
    if (await emailError.isVisible().catch(() => false)) {
      errors.push(await emailError.textContent() || 'Email error');
    }
    
    // Проверяем поле password
    const passwordError = this.page.locator('[data-testid="login-password"] ~ p[class*="error"], [data-testid="login-password"] + p[class*="error"], [data-testid="login-password"] ~ [class*="error"]');
    if (await passwordError.isVisible().catch(() => false)) {
      errors.push(await passwordError.textContent() || 'Password error');
    }
    
    return errors;
  }
}
