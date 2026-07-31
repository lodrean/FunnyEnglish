import { test, expect, Page } from '@chromatic-com/playwright';

/**
 * TC-E2E-001: Create Image Word Match Test (Happy Path)
 * Priority: P0 (Critical)
 *
 * Спек переписан под РЕАЛЬНЫЙ TestEditor + ImageWordMatchEditor (2026-07-21):
 * - auth через storageState (setup-проект), ручной логин убран
 * - реальные data-testid вместо несуществующих select/nth-input'ов
 */

/** Создаёт тест через UI и открывает IWM-редактор на вкладке Questions */
async function openIwmEditor(page: Page) {
  await page.goto('/content/tests');
  await page.locator('[data-testid="page-title"]').waitFor({ timeout: 15000 });
  await page.locator('[data-testid="add-test-button"]').click();
  await page.waitForURL(/.*tests\/new.*/, { timeout: 10000 });

  // Title
  await page.locator('[data-testid="test-title-input"] input, input[name="title"]').first().fill(`IWM Create ${Date.now()}`);

  // Category: combobox + первая реальная опция (клавиатурой — стабильно на mobile)
  await page.locator('[role="combobox"]').first().click();
  await page.locator('[role="option"]').first().waitFor({ timeout: 5000 });
  await page.waitForTimeout(400);
  await page.keyboard.press('ArrowDown');
  await page.keyboard.press('Enter');

  // Save и ждём завершения мутации (навигация на detail)
  await page.locator('[data-testid="save-test-button"]').click();
  await page.waitForURL(/\/content\/tests\/[0-9a-f-]{36}/, { timeout: 15000 });

  // Questions tab → добавить IWM-вопрос
  await page.locator('[data-testid="questions-tab"]').click();
  await page.locator('[data-testid="add-image-word-match-button"]').click();
  await expect(page.locator('[data-testid="image-word-match-editor"]')).toBeVisible({ timeout: 10000 });
}

/** Переход на шаг индикатора (mobile: focus+Enter — step перекрыт чипом) */
async function activateStep(page: Page, index: number) {
  await page.locator('.step-indicator .step').nth(index).focus();
  await page.keyboard.press('Enter');
  await page.waitForTimeout(300);
}

test.describe('Image Word Match - Create Test', () => {

  test('should create Image Word Match test with image and words', async ({ page }) => {
    await openIwmEditor(page);

    // Step 1: загружаем изображение (после успешного upload редактор сам переходит на Words)
    await page.locator('[data-testid="image-upload-input"]').setInputFiles('e2e/fixtures/test-image.jpg');
    await expect(page.locator('[data-testid="words-step"]')).toBeVisible({ timeout: 20000 });
    await page.locator('[data-testid="word-input"]').fill('cat');
    await page.locator('[data-testid="add-word-button"]').click();
    await page.locator('[data-testid="word-input"]').fill('dog');
    await page.locator('[data-testid="add-word-button"]').click();
    await expect(page.locator('.word-text:has-text("cat")')).toBeVisible();
    await expect(page.locator('.word-text:has-text("dog")')).toBeVisible();

    // Continue → hotspots доступен (>= 2 слов)
    await expect(page.locator('[data-testid="continue-to-hotspots"]')).toBeEnabled();

    // Step 3: canvas для hotspot'ов
    await activateStep(page, 2);
    await expect(page.locator('[data-testid="hotspots-step"]')).toBeVisible();
    await expect(page.locator('.hotspot-canvas-container')).toBeVisible();

    console.log('✅ TC-E2E-001: Create IWM Test - PASSED');
  });

  test('should disable save without image @validation', async ({ page }) => {
    await openIwmEditor(page);

    // Добавляем 2 слова, но НЕ загружаем изображение
    await activateStep(page, 1);
    await page.locator('[data-testid="word-input"]').fill('cat');
    await page.locator('[data-testid="add-word-button"]').click();
    await page.locator('[data-testid="word-input"]').fill('dog');
    await page.locator('[data-testid="add-word-button"]').click();

    // На Preview кнопка сохранения должна быть disabled (нет изображения/hotspots)
    await activateStep(page, 3);
    await expect(page.locator('[data-testid="preview-step"]')).toBeVisible();
    await expect(page.locator('[data-testid="save-question-button"]')).toBeDisabled();

    console.log('✅ TC-E2E-002: Validation No Image - PASSED');
  });

  test('should disable continue with insufficient words @validation', async ({ page }) => {
    await openIwmEditor(page);

    // Загружаем изображение (после upload редактор сам переходит на Words)
    await page.locator('[data-testid="image-upload-input"]').setInputFiles('e2e/fixtures/test-image.jpg');
    await expect(page.locator('[data-testid="words-step"]')).toBeVisible({ timeout: 20000 });

    // Добавляем только 1 слово
    await page.locator('[data-testid="word-input"]').fill('cat');
    await page.locator('[data-testid="add-word-button"]').click();
    await expect(page.locator('.word-text:has-text("cat")')).toBeVisible();

    // Continue → hotspots disabled (нужно минимум 2 слова)
    await expect(page.locator('[data-testid="continue-to-hotspots"]')).toBeDisabled();

    console.log('✅ TC-E2E-003: Validation Insufficient Words - PASSED');
  });
});
