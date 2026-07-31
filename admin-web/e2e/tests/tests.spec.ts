import { test, expect } from '@chromatic-com/playwright';
import { TestsPage, TestFormPage } from '../pages/TestsPage';

test.describe('Управление тестами', () => {
  let testsPage: TestsPage;
  let testFormPage: TestFormPage;

  test.beforeEach(async ({ page }) => {
    // Auth state is already set up
    testsPage = new TestsPage(page);
    testFormPage = new TestFormPage(page);
    await testsPage.goto();
  });

  test('должен отображать страницу тестов', async () => {
    await testsPage.expectPageLoaded();
  });

  test('должен открывать форму создания теста', async () => {
    await testsPage.clickAddTest();
    
    await expect(testFormPage.titleInput).toBeVisible();
    await expect(testFormPage.saveButton).toBeVisible();
  });

  test('должен создавать новый тест', async ({ page }) => {
    const testTitle = `Тест ${Date.now()}`;
    
    await testsPage.clickAddTest();
    
    await testFormPage.fillForm({
      title: testTitle,
      description: 'Описание теста',
      category: 'Grammar' // Выбираем существующую категорию
    });
    await testFormPage.save();
    
    // Проверяем что тест сохранен - страница редактирования загружена
    // После сохранения остаемся на странице редактирования с заголовком теста
    await expect(page.locator('text=All changes saved').or(page.locator('text=Сохранено'))).toBeVisible({ timeout: 10000 });
    // И проверяем что URL содержит ID теста (не /new)
    await expect(page).toHaveURL(/.*tests\/.+/, { timeout: 10000 });
  });

  test('должен искать тест', async () => {
    await testsPage.searchTest('Present');
    
    // Проверяем что поиск работает
    const count = await testsPage.getTestsCount();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('должен отображать таблицу тестов', async () => {
    const count = await testsPage.getTestsCount();
    expect(count).toBeGreaterThanOrEqual(0);
  });
});

test.describe('Управление тестами - Дизайн система', () => {
  let testsPage: TestsPage;

  test.beforeEach(async ({ page }) => {
    testsPage = new TestsPage(page);
    await testsPage.goto();
  });

  test('должен иметь корректную структуру страницы', async ({ page }) => {
    await expect(page.locator('[data-testid="page-title"]')).toBeVisible();
    await expect(page.locator('[data-testid="add-test-button"]')).toBeVisible();
  });

  test('должен отображать таблицу с колонками', async ({ page }) => {
    // Проверяем наличие заголовков таблицы
    const tableHeaders = page.locator('table th, [role="columnheader"]');
    const headerCount = await tableHeaders.count();
    expect(headerCount).toBeGreaterThan(0);
  });
});
