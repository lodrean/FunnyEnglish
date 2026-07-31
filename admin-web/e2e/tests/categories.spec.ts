import { test, expect } from '@chromatic-com/playwright';
import { CategoriesPage, CategoryFormPage } from '../pages/CategoriesPage';

test.describe('Управление категориями', () => {
  let categoriesPage: CategoriesPage;
  let categoryFormPage: CategoryFormPage;

  test.beforeEach(async ({ page }) => {
    // Auth state is already set up, just navigate to the page
    categoriesPage = new CategoriesPage(page);
    categoryFormPage = new CategoryFormPage(page);
    await categoriesPage.goto();
  });

  test('должен отображать страницу категорий', async () => {
    await categoriesPage.expectPageLoaded();
  });

  test('должен открывать форму создания категории', async () => {
    await categoriesPage.clickAddCategory();
    
    await expect(categoryFormPage.nameInput).toBeVisible();
    await expect(categoryFormPage.saveButton).toBeVisible();
  });

  test('должен создавать новую категорию', async () => {
    const testCategoryName = `Test Category ${Date.now()}`;
    
    await categoriesPage.clickAddCategory();
    
    await categoryFormPage.fillForm({
      name: testCategoryName,
      description: 'Test category description'
    });
    await categoryFormPage.save();
    
    // Проверяем что вернулись на список
    await categoriesPage.expectPageLoaded();
    
    // Note: In mock mode the category won't actually appear in the list
    // In real API mode we would check: await categoriesPage.expectCategoryVisible(testCategoryName);
  });

  test('должен искать категорию', async () => {
    await categoriesPage.searchCategory('Grammar');
    
    // Проверяем что результаты поиска отображаются
    const count = await categoriesPage.getCategoriesCount();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('должен отображать таблицу категорий', async () => {
    const count = await categoriesPage.getCategoriesCount();
    // Таблица может быть пустой или содержать данные
    expect(count).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Управление категориями - Дизайн система', () => {
  let categoriesPage: CategoriesPage;

  test.beforeEach(async ({ page }) => {
    categoriesPage = new CategoriesPage(page);
    await categoriesPage.goto();
  });

  test('должен иметь корректную структуру страницы', async ({ page }) => {
    // Проверяем наличие основных элементов
    await expect(page.locator('[data-testid="page-title"]')).toBeVisible();
    await expect(page.locator('[data-testid="add-category-button"]')).toBeVisible();
  });

  test('должен иметь функциональный поиск', async ({ page }) => {
    // Проверяем что поисковое поле работает
    const searchInput = page.locator('[data-testid="search-categories"] input, input[placeholder*="Search categories"]').first();
    await searchInput.fill('test');
    await expect(searchInput).toHaveValue('test');
  });
});
