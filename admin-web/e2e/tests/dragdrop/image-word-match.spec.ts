import { test, expect } from '@chromatic-com/playwright';
import { TestsPage, TestFormPage } from '../../pages/TestsPage';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * E2E тесты для Image-Word-Match вопросов
 * 
 * Проверяют что редактор открывается и все элементы UI присутствуют.
 * Полная проверка с загрузкой файлов требует настроенного S3/MinIO.
 */

// Клик по шагу индикатора: на mobile step перекрывается чипом типа вопроса,
// поэтому вместо click — focus + Enter (на шаге есть onKeyDown).
async function activateStep(page: any, index: number) {
  await page.locator('.step-indicator .step').nth(index).focus();
  await page.keyboard.press('Enter');
  await page.waitForTimeout(300);
}

test.describe('Image-Word-Match Editor', () => {
  let testsPage: TestsPage;
  let testFormPage: TestFormPage;

  test.beforeEach(async ({ page }) => {
    testsPage = new TestsPage(page);
    testFormPage = new TestFormPage(page);
  });

  test('должен открывать редактор Image Word Match (TC-IWM-001)', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `IWM Test ${Date.now()}`,
      description: 'Test for image word match editor',
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    
    // Открываем редактор IMAGE_WORD_MATCH
    await page.locator('[data-testid="add-image-word-match-button"]').click();

    // Проверяем что редактор открылся (скроллим к нему)
    await page.locator('h2:has-text("Create Image-Word Match Question")').scrollIntoViewIfNeeded();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Проверяем шаг загрузки изображения
    await expect(page.locator('[data-testid="image-step"]')).toBeVisible();
    
    // Проверяем что есть поле для загрузки
    await expect(page.locator('[data-testid="image-upload-input"]')).toBeAttached();
    
    // Проверяем текст инструкции
    await expect(page.locator('h3:has-text("Upload Image")')).toBeVisible();
    await expect(page.locator('span:has-text("Click to upload image")')).toBeVisible();
  });

  test('должен показывать все шаги редактора (TC-IWM-002)', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `IWM Steps Test ${Date.now()}`,
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    
    // Открываем редактор
    await page.locator('[data-testid="add-image-word-match-button"]').click();

    // Проверяем что редактор открылся (скроллим к нему)
    await page.locator('h2:has-text("Create Image-Word Match Question")').scrollIntoViewIfNeeded();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Проверяем индикатор шагов
    await expect(page.locator('.step-indicator .step').nth(0)).toBeVisible();
    await expect(page.locator('.step-indicator .step').nth(1)).toBeVisible();
    await expect(page.locator('.step-indicator .step').nth(2)).toBeVisible();
    await expect(page.locator('.step-indicator .step').nth(3)).toBeVisible();
    
    // Проверяем что активен первый шаг
    await expect(page.locator('[data-testid="image-step"]')).toBeVisible();
  });

  test('должен иметь все элементы ввода для слов (TC-IWM-003)', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `IWM Words UI Test ${Date.now()}`,
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    
    // Открываем редактор
    await page.locator('[data-testid="add-image-word-match-button"]').click();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Поля ввода слов рендерятся только на шаге Words — переходим на него
    await activateStep(page, 1);
    await expect(page.locator('[data-testid="words-step"]')).toBeVisible();
    await expect(page.locator('[data-testid="word-input"]')).toBeAttached();
    await expect(page.locator('[data-testid="translation-input"]')).toBeAttached();
    await expect(page.locator('[data-testid="add-word-button"]')).toBeAttached();
    
    // Проверяем что можно ввести слово
    await page.locator('[data-testid="word-input"]').fill('test-word');
    await page.locator('[data-testid="translation-input"]').fill('тестовое-слово');
    
    // Кнопка Add должна быть активна
    await expect(page.locator('[data-testid="add-word-button"]')).toBeEnabled();
  });

  test('должен иметь инструменты для hotspots (TC-IWM-004)', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `IWM Hotspots UI Test ${Date.now()}`,
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    
    // Открываем редактор
    await page.locator('[data-testid="add-image-word-match-button"]').click();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Canvas и инструменты рендерятся только на шаге Hotspots — переходим на него
    await activateStep(page, 2);
    await expect(page.locator('[data-testid="hotspots-step"]')).toBeVisible();
    await expect(page.locator('.hotspot-canvas-container')).toBeAttached();
    
    // Проверяем инструменты
    await expect(page.locator('button[title="Select/Move"]')).toBeAttached();
    await expect(page.locator('button[title="Draw Rectangle"]')).toBeAttached();
    await expect(page.locator('button[title="Draw Circle"]')).toBeAttached();
    
    // Проверяем zoom контролы
    await expect(page.locator('button[title="Zoom In"]')).toBeAttached();
    await expect(page.locator('button[title="Zoom Out"]')).toBeAttached();
    await expect(page.locator('button[title="Reset Zoom"]')).toBeAttached();
  });

  test('должен иметь кнопки сохранения (TC-IWM-006)', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `IWM Save UI Test ${Date.now()}`,
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    
    // Открываем редактор
    await page.locator('[data-testid="add-image-word-match-button"]').click();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Кнопка Cancel — в шапке редактора, видна всегда
    await expect(page.locator('button:has-text("Cancel")')).toBeVisible();

    // Кнопка сохранения рендерится только на шаге Preview — переходим на него
    await activateStep(page, 3);
    await expect(page.locator('[data-testid="preview-step"]')).toBeVisible();
    await expect(page.locator('[data-testid="save-question-button"]')).toBeAttached();
  });

  test('должен переключать шаги при клике на индикатор (TC-IWM-010)', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `IWM Navigation Test ${Date.now()}`,
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    
    // Открываем редактор
    await page.locator('[data-testid="add-image-word-match-button"]').click();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Проверяем что виден первый шаг
    await expect(page.locator('[data-testid="image-step"]')).toBeVisible();
    
    // Кликаем на Words в индикаторе шагов
    await activateStep(page, 1);
    await page.waitForTimeout(500);
    
    // Проверяем что виден шаг Words
    await expect(page.locator('[data-testid="words-step"]')).toBeVisible();
    
    // Кликаем на Hotspots
    await activateStep(page, 2);
    await page.waitForTimeout(500);
    
    // Проверяем что виден шаг Hotspots
    await expect(page.locator('[data-testid="hotspots-step"]')).toBeVisible();
    
    // Кликаем на Preview
    await activateStep(page, 3);
    await page.waitForTimeout(500);
    
    // Проверяем что виден шаг Preview
    await expect(page.locator('[data-testid="preview-step"]')).toBeVisible();
  });

  test('полный флоу создания вопроса - UI проверка (TC-IWM-007)', async ({ page }) => {
    const testTitle = `Full IWM UI Test ${Date.now()}`;

    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: testTitle,
      description: 'Complete Image Word Match test',
      category: 'Test Category',
    });
    await testFormPage.save();

    // Проверяем что тест создан
    await expect(page.locator(`text=${testTitle}`)).toBeVisible();

    // Добавляем IMAGE_WORD_MATCH вопрос
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);
    await page.locator('[data-testid="add-image-word-match-button"]').click();

    // Проверяем что редактор открылся (скроллим к нему)
    await page.locator('h2:has-text("Create Image-Word Match Question")').scrollIntoViewIfNeeded();
    await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
    
    // Step 1: Image - проверяем элементы
    await expect(page.locator('[data-testid="image-step"]')).toBeVisible();
    await expect(page.locator('text=Step 1: Upload Image')).toBeVisible();
    
    // Переходим на Words
    await activateStep(page, 1);
    await page.waitForTimeout(500);
    
    // Step 2: Words - проверяем элементы
    await expect(page.locator('[data-testid="words-step"]')).toBeVisible();
    await expect(page.locator('text=Step 2: Add Words')).toBeVisible();
    await expect(page.locator('[data-testid="word-input"]')).toBeVisible();
    await expect(page.locator('[data-testid="add-word-button"]')).toBeVisible();
    
    // Добавляем слова
    await page.locator('[data-testid="word-input"]').fill('table');
    await page.locator('[data-testid="add-word-button"]').click();
    await page.locator('[data-testid="word-input"]').fill('chair');
    await page.locator('[data-testid="add-word-button"]').click();
    
    // Проверяем что слова добавлены
    await expect(page.locator('.word-text:has-text("table")')).toBeVisible();
    await expect(page.locator('.word-text:has-text("chair")')).toBeVisible();
    
    // Переходим на Hotspots
    await activateStep(page, 2);
    await page.waitForTimeout(500);
    
    // Step 3: Hotspots - проверяем элементы
    await expect(page.locator('[data-testid="hotspots-step"]')).toBeVisible();
    await expect(page.locator('text=Step 3: Draw Hotspots')).toBeVisible();
    await expect(page.locator('.hotspot-canvas-container')).toBeVisible();
    
    // Переходим на Preview
    await activateStep(page, 3);
    await page.waitForTimeout(500);
    
    // Step 4: Preview - проверяем элементы
    await expect(page.locator('[data-testid="preview-step"]')).toBeVisible();
    await expect(page.locator('.step-indicator .step').nth(3)).toBeVisible();
    await expect(page.locator('[data-testid="save-question-button"]')).toBeAttached();
  });
});

/**
 * Тесты для типа DRAG_DROP_MATCH
 */
test.describe('Drag-Drop-Match Editor', () => {
  let testsPage: TestsPage;
  let testFormPage: TestFormPage;

  test.beforeEach(async ({ page }) => {
    testsPage = new TestsPage(page);
    testFormPage = new TestFormPage(page);
  });

  test('должен создавать DRAG_DROP_MATCH вопрос', async ({ page }) => {
    // Создаем тест
    await testsPage.goto();
    await testsPage.clickAddTest();
    await testFormPage.fillForm({
      title: `Drag Drop Match Test ${Date.now()}`,
      category: 'Test Category',
    });
    await testFormPage.save();

    // Переходим на вкладку Questions
    await page.locator('[data-testid="questions-tab"]').click();
    await page.waitForTimeout(500);

    // Добавляем Matching вопрос
    await page.locator('[data-testid="add-matching-button"]').click();

    // Проверяем что появилась карточка нового вопроса
    await expect(page.locator('[data-testid^="question-card-"]').first()).toBeVisible();
    // Тексты 'Step 1/2/3' редактора IWM отсутствуют — это обычный matching-вопрос
    await expect(page.locator('h3:has-text("Step 1: Upload Image")')).toHaveCount(0);
  });
});
