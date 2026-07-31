import { Page, Locator, expect } from '@chromatic-com/playwright';

/**
 * Page Object для страницы тестов
 */
export class TestsPage {
  readonly page: Page;
  readonly pageTitle: Locator;
  readonly addButton: Locator;
  readonly testsTable: Locator;
  readonly searchInput: Locator;

  constructor(page: Page) {
    this.page = page;
    this.pageTitle = page.locator('[data-testid="page-title"]');
    this.addButton = page.locator('[data-testid="add-test-button"]');
    this.testsTable = page.locator('table, [data-testid="tests-table"]').first();
    this.searchInput = page.locator('[data-testid="search-tests"] input, input[placeholder*="Search"]').first();
  }

  /**
   * Переход на страницу тестов
   */
  async goto() {
    await this.page.goto('/content/tests');
    // networkidle нестабилен (TanStack Query фоновые запросы) — ждём конкретный элемент
    await this.pageTitle.waitFor({ timeout: 15000 });
  }

  /**
   * Проверка загрузки страницы
   */
  async expectPageLoaded() {
    await expect(this.page).toHaveURL(/.*tests.*/, { timeout: 10000 });
    await expect(this.pageTitle).toContainText('Tests', { timeout: 10000 });
  }

  /**
   * Нажать кнопку добавления теста
   */
  async clickAddTest() {
    await this.addButton.click();
    await this.page.waitForURL(/.*tests\/(new|add).*/);
  }

  /**
   * Поиск теста
   */
  async searchTest(query: string) {
    await this.searchInput.fill(query);
    await this.page.waitForTimeout(500);
  }

  /**
   * Получить количество тестов в таблице
   */
  async getTestsCount(): Promise<number> {
    const rows = this.page.locator('table tbody tr, [data-testid="test-row"]');
    return await rows.count();
  }
}

/**
 * Page Object для формы создания/редактирования теста
 */
export class TestFormPage {
  readonly page: Page;
  readonly titleInput: Locator;
  readonly descriptionInput: Locator;
  readonly saveButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.titleInput = page.locator('input[placeholder*="Test Title"], input[name="title"], [data-testid="test-title-input"] input').first();
    this.descriptionInput = page.locator('textarea[placeholder*="Description"], textarea[name="description"]').first();
    this.saveButton = page.locator('[data-testid="save-test-button"], button[type="submit"]').first();
  }

  /**
   * Заполнить форму теста
   */
  async fillForm(data: {
    title: string;
    description?: string;
    category?: string;
  }) {
    await this.titleInput.fill(data.title);
    
    if (data.description && await this.descriptionInput.isVisible().catch(() => false)) {
      await this.descriptionInput.fill(data.description);
    }
    
    // Выбираем категорию если нужно
    if (data.category) {
      // MUI Select: открываем combobox в блоке Category
      const categoryDropdown = this.page.locator('[role="combobox"]').first();
      await categoryDropdown.scrollIntoViewIfNeeded().catch(() => {});
      await categoryDropdown.click();
      // Ждём стабилизации MUI Menu (анимация) — иначе click по опции флакует «element is not stable»
      await this.page.locator('[role="option"]').first().waitFor({ timeout: 5000 });
      await this.page.waitForTimeout(400);
      
      // Выбираем категорию по тексту, иначе первую реальную опцию КЛАВИАТУРОЙ —
      // click по option на mobile перехватывается MUI Backdrop (element is not stable).
      const option = this.page.locator('[role="option"]', { hasText: data.category }).first();
      if (await option.isVisible({ timeout: 3000 }).catch(() => false)) {
        await option.click();
      } else {
        // Меню открыто, фокус на текущей опции: ArrowDown -> первая enabled, Enter -> выбор
        await this.page.keyboard.press('ArrowDown');
        await this.page.keyboard.press('Enter');
      }
      await this.page.waitForTimeout(300);
    }
  }

  /**
   * Сохранить тест
   */
  async save() {
    await this.saveButton.click();
    // Ждём завершения сохранения: навигация на detail (новый тест) или snackbar
    await Promise.race([
      this.page.waitForURL(/\/content\/tests\/[0-9a-f-]{36}/, { timeout: 15000 }),
      this.page.locator('text=Test saved successfully').waitFor({ timeout: 15000 }),
    ]).catch(() => {});
  }
}
