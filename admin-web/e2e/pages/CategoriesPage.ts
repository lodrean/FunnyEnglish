import { Page, Locator, expect } from '@chromatic-com/playwright';

/**
 * Page Object для страницы категорий
 */
export class CategoriesPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly addButton: Locator;
  readonly categoriesTable: Locator;
  readonly searchInput: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.addButton = page.locator('[data-testid="add-category-button"]');
    this.categoriesTable = page.locator('[data-testid="categories-table"], .MuiPaper-root').first();
    this.searchInput = page.locator('[data-testid="search-categories"] input, input[placeholder*="Search categories"]').first();
  }

  /**
   * Переход на страницу категорий
   */
  async goto() {
    await this.page.goto('/content/categories');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * Проверка загрузки страницы
   */
  async expectPageLoaded() {
    await expect(this.page).toHaveURL(/.*categories.*/, { timeout: 10000 });
    await expect(this.pageTitle).toContainText('Categories', { timeout: 10000 });
  }

  /**
   * Нажать кнопку добавления категории
   */
  async clickAddCategory() {
    await this.addButton.click();
    // Wait for dialog to open
    await expect(this.page.locator('[role="dialog"]')).toBeVisible();
  }

  /**
   * Поиск категории
   */
  async searchCategory(query: string) {
    await this.searchInput.fill(query);
    await this.page.waitForTimeout(500); // Дебаунс
  }

  /**
   * Получить количество категорий в таблице
   */
  async getCategoriesCount(): Promise<number> {
    const items = this.page.locator('[role="listitem"], .MuiPaper-root > div');
    return await items.count();
  }

  /**
   * Клик по категории для редактирования
   */
  async clickEditCategory(categoryName: string) {
    const row = this.page.locator(`text=${categoryName}`).first();
    await row.locator('..').locator('button').first().click();
  }

  /**
   * Удалить категорию
   */
  async deleteCategory(categoryName: string) {
    const row = this.page.locator(`text=${categoryName}`).first();
    await row.locator('..').locator('button').nth(1).click();
    
    // Подтверждение удаления
    const confirmButton = this.page.locator('button:has-text("Delete"), button:has-text("Удалить")').first();
    await confirmButton.click();
  }

  /**
   * Проверить что категория видна
   */
  async expectCategoryVisible(categoryName: string) {
    const category = this.page.locator(`text=${categoryName}`).first();
    await expect(category).toBeVisible();
  }
}

/**
 * Page Object для формы создания/редактирования категории
 */
export class CategoryFormPage {
  readonly page: Page;
  readonly nameInput: Locator;
  readonly descriptionInput: Locator;
  readonly saveButton: Locator;
  readonly cancelButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.nameInput = page.locator('input[label="Category Name"], input[placeholder*="Name"], [role="dialog"] input').first();
    this.descriptionInput = page.locator('textarea[name="description"]').first();
    this.saveButton = page.locator('[role="dialog"] button[type="submit"], [role="dialog"] button:has-text("Create"), [role="dialog"] button:has-text("Update")').first();
    this.cancelButton = page.locator('[role="dialog"] button:has-text("Cancel")').first();
  }

  /**
   * Заполнить форму категории
   */
  async fillForm(data: { name: string; description?: string }) {
    await this.nameInput.fill(data.name);
    
    if (data.description && await this.descriptionInput.isVisible().catch(() => false)) {
      await this.descriptionInput.fill(data.description);
    }
  }

  /**
   * Сохранить категорию
   */
  async save() {
    await this.saveButton.click();
    // Wait for dialog to close
    await this.page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
  }

  /**
   * Отменить создание
   */
  async cancel() {
    await this.cancelButton.click();
  }
}
