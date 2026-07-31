import { test, expect } from '@playwright/test';

/**
 * E2E Chain — Part A: создание теста через админку.
 *
 * Цепочка: A (админ создаёт тест) → B (ученик проходит в app, Maestro) →
 * C (админ видит результат + шлёт комментарий) → D (ученик читает в app).
 *
 * Auth: storageState из setup-проекта (admin уже залогинен).
 * Запуск: SKIP_WEB_SERVER=1 ADMIN_URL=http://localhost:3000 npx playwright test e2e-chain-a-create
 */

export const CHAIN_TEST_TITLE = 'E2E Chain Test';
export const CHAIN_Q1_TEXT = "What is 'кошка' in English?";
export const CHAIN_Q2_TEXT = "What is 'собака' in English?";

test.describe('E2E Chain — A: Create test via admin', () => {
  test('admin creates TEXT_SELECT test via Test Editor', async ({ page }) => {
    // 1. Tests page → Add Test
    await page.goto('/content/tests');
    await page.waitForLoadState('networkidle');
    await page.locator('button:has-text("Add Test"), [data-testid="add-test-button"]').first().click();
    await page.waitForURL(/.*new|.*create/, { timeout: 10000 });

    // 2. General tab: title + category
    await page.locator('[data-testid="test-title-input"] input').fill(CHAIN_TEST_TITLE);

    // Category — MUI Select: кликаем combobox и выбираем РЕАЛЬНУЮ категорию
    // (первая опция — плейсхолдер «Select a category...»)
    await page.locator('[role="combobox"]').first().click();
    await page.locator('[role="option"]').nth(1).click();

    // 3. Questions tab → add multiple choice ×2
    await page.locator('[data-testid="questions-tab"]').click();

    const questions: Array<{ text: string; options: string[]; correct: string }> = [
      { text: CHAIN_Q1_TEXT, options: ['cat', 'dog', 'bird', 'fish'], correct: 'cat' },
      { text: CHAIN_Q2_TEXT, options: ['dog', 'cat', 'horse', 'mouse'], correct: 'dog' },
    ];

    for (let q = 0; q < questions.length; q++) {
      await page.locator('[data-testid="add-multiple-choice-button"]').click();
      const card = page.locator('[data-testid^="question-card-"]').nth(q);
      await card.locator('textarea').first().fill(questions[q].text);

      for (let i = 0; i < questions[q].options.length; i++) {
        await card.locator(`input[placeholder="Option ${i + 1}"]`).fill(questions[q].options[i]);
      }
      // Отметить правильный ответ: switch'ы идут в том же порядке, что и опции.
      // force:true — MUI Switch input визуально скрыт, на узких вьюпортах
      // точка клика перехватывается соседними полями (mobile viewport).
      const correctIndex = questions[q].options.indexOf(questions[q].correct);
      await card.locator('input[type="checkbox"]').nth(correctIndex).click({ force: true });
    }

    // 4. Save (+ подтверждение «Save & Leave», если появился диалог)
    await page.locator('[data-testid="save-test-button"]').click();
    const saveLeave = page.locator('button:has-text("Save & Leave")');
    if (await saveLeave.isVisible({ timeout: 3000 }).catch(() => false)) {
      await saveLeave.click();
    }

    // Дожидаемся завершения saveMutation: сначала сохраняется тест, потом
    // ПОСЛЕДОВАТЕЛЬНО все вопросы (POST /questions). Если уйти со страницы
    // раньше — мутация прерывается и вопросы не отправляются.
    await Promise.race([
      page.waitForURL(/\/content\/tests\/[0-9a-f-]{36}/, { timeout: 20000 }),
      page.locator('text=Test saved successfully').waitFor({ timeout: 20000 }),
    ]).catch(() => {});

    // 5. Verify: тест появился в списке
    await page.goto('/content/tests');
    await page.waitForLoadState('networkidle');
    await expect(page.locator(`text=${CHAIN_TEST_TITLE}`).first()).toBeVisible({ timeout: 15000 });

    // 6. Verify через API: вопросы реально сохранились (а не только черновик)
    const loginResp = await page.request.post('/api/auth/login', {
      data: { email: 'admin@funnyenglish.com', password: 'admin123' },
    });
    const { token } = await loginResp.json();
    const testsResp = await page.request.get('/api/admin/tests', {
      headers: { Authorization: `Bearer ${token}` },
    });
    const tests = await testsResp.json();
    const chainTests = tests.filter((t: any) => t.title === CHAIN_TEST_TITLE);
    expect(chainTests.length, 'тест должен появиться в списке').toBeGreaterThan(0);

    // NOTE: GET /api/admin/tests (list) НЕ включает questions (TODO в TestService.getAllTestsForAdmin) —
    // вопросы проверяем через detail-эндпоинт, который грузит их через findByIdWithQuestions.
    const testId = chainTests[chainTests.length - 1].id;
    const detailResp = await page.request.get(`/api/admin/tests/${testId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const detail = await detailResp.json();
    expect(detail.questions?.length ?? 0, 'тест с вопросами должен сохраниться').toBeGreaterThanOrEqual(2);

    console.log('✅ Test created via admin UI with', detail.questions.length, 'questions');
  });
});
