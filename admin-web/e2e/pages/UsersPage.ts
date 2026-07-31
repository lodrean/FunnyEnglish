import { Page, Locator, expect } from '@chromatic-com/playwright';

/**
 * Page Object для страницы пользователей
 */
export class UsersPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly usersTable: Locator;
  readonly searchInput: Locator;
  readonly roleFilter: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.usersTable = page.locator('table, [data-testid="users-table"]').first();
    this.searchInput = page.locator('[data-testid="search-users"] input, input[placeholder*="Search"]').first();
    this.roleFilter = page.locator('select[name="role"], [data-testid="role-filter"]').first();
  }

  /**
   * Переход на страницу пользователей
   */
  async goto() {
    await this.page.goto('/users');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * Проверка загрузки страницы
   */
  async expectPageLoaded() {
    await expect(this.page).toHaveURL(/.*users.*/, { timeout: 10000 });
    await expect(this.pageTitle).toContainText('Users', { timeout: 10000 });
  }

  /**
   * Поиск пользователя
   */
  async searchUser(query: string) {
    await this.searchInput.fill(query);
    await this.page.waitForTimeout(500);
  }

  /**
   * Фильтрация по роли
   */
  async filterByRole(role: 'admin' | 'user' | 'teacher') {
    await this.roleFilter.selectOption(role);
    await this.page.waitForTimeout(500);
  }

  /**
   * Получить количество пользователей в таблице
   */
  async getUsersCount(): Promise<number> {
    const rows = this.page.locator('table tbody tr, [data-testid="user-row"]');
    return await rows.count();
  }

  /**
   * Клик по пользователю для просмотра деталей (открывается drawer)
   */
  async clickViewUser(email: string) {
    // Ищем строку с email и кликаем на неё
    const row = this.page.locator('table tbody tr').filter({ hasText: email }).first();
    await row.click();
    // Ждем появления формы редактирования пользователя (справа drawer)
    // Проверяем по наличию Email поля с значением
    await this.page.locator('input[value="' + email + '"]').first().waitFor({ state: 'visible', timeout: 10000 });
  }
}

/**
 * Page Object для страницы деталей пользователя (drawer справа)
 */
export class UserDetailsPage {
  readonly page: Page;
  readonly updateButton: Locator;

  constructor(page: Page) {
    this.page = page;
    // Кнопка Update в drawer
    this.updateButton = page.locator('button:has-text("Update"), button[type="submit"]').first();
  }

  /**
   * Проверить что страница/дравер загружен
   */
  async expectPageLoaded() {
    // Проверяем что видна кнопка Update в drawer
    await expect(this.updateButton).toBeVisible({ timeout: 10000 });
  }

  /**
   * Получить имя пользователя
   */
  async getUserName(): Promise<string> {
    return await this.userName.textContent() || '';
  }

  /**
   * Получить email пользователя
   */
  async getUserEmail(): Promise<string> {
    return await this.userEmail.textContent() || '';
  }
}
