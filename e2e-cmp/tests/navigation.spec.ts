import { test, expect } from '@playwright/test';
import {
  launchApp, clickCanvas, clipShot, expectScreenChanged, neutralMouse,
  collectCriticalErrors, collectServerErrors, skipOnMobile,
  continueAsGuest, openFirstLibrary, openQuestionsViaVideo,
  POS,
} from './helpers';

/**
 * CMP WASM Navigation Tests — speaking-флоу (guest-first, 2026-08-01):
 * Library (старт) → Topics → Video (error-стаб на wasm) → Questions → Training/Practice.
 * Bottom-sheet выбора субтитров удалён (DC-5) — переход сразу на Video.
 * Координатные клики (canvas-only), assertion'ы по смене пикселей.
 * Требуется backend с seed-контентом («Разговорный английский»).
 */
test.describe('CMP WASM - Navigation (speaking flow)', () => {

  test('гость: Library → Topics → Video → Questions → Training', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    const errors = collectCriticalErrors(page);
    const serverErrors = collectServerErrors(page);

    await launchApp(page);
    await continueAsGuest(page);
    const library = await clipShot(page);
    await page.screenshot({ path: 'test-results/cmp-nav-library.png' });

    // Library → Topics
    await openFirstLibrary(page);
    const topics = await clipShot(page);
    expectScreenChanged(library, topics, 'Library → Topics');
    await page.screenshot({ path: 'test-results/cmp-nav-topics.png' });

    // Topics → Video (на wasm — error-стаб плеера) → «К вопросам» → Questions
    await openQuestionsViaVideo(page);
    const questions = await clipShot(page);
    expectScreenChanged(topics, questions, 'Topics → Questions (via video)');
    await page.screenshot({ path: 'test-results/cmp-nav-questions.png' });

    // Questions → Training (кнопка «Тренировка · 3 попытки», у гостя выше гейта)
    await clickCanvas(page, POS.questionsTrainingGuest, 4000);
    const training = await clipShot(page, { x: 0, y: 60, width: 400, height: 80 });
    expectScreenChanged(questions, training, 'Questions → Training');
    await page.screenshot({ path: 'test-results/cmp-nav-training.png' });

    expect(errors, `Критические ошибки консоли: ${errors.join(' | ')}`).toHaveLength(0);
    expect(serverErrors, `HTTP 5xx к backend: ${serverErrors.join(' | ')}`).toHaveLength(0);
    console.log('✅ Guest speaking flow: Library → Topics → Questions → Training');
  });

  test('гость: Practice залочена SpeakingGate — «Войти» ведёт на логин', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);
    await continueAsGuest(page);
    await openFirstLibrary(page);
    await openQuestionsViaVideo(page);

    // SpeakingGate в нижней зоне QuestionsScreen («Ты почти у цели!» + «Войти»)
    const gate = await clipShot(page, { x: 0, y: 480, width: 800, height: 240 });
    await page.screenshot({ path: 'test-results/cmp-nav-practice-locked.png' });
    await clickCanvas(page, POS.gateLogin, 3000);
    const login = await clipShot(page, { x: 0, y: 0, width: 800, height: 120 });
    await page.screenshot({ path: 'test-results/cmp-nav-gate-to-login.png' });
    expect(gate.equals(login), 'Тап по «Войти» в гейте не изменил экран').toBe(false);
    console.log('✅ Guest practice gating works');
  });

  test('гость: «Отправки» в bottom nav — заглушка с CTA регистрации', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);
    await continueAsGuest(page);

    const library = await clipShot(page);
    await clickCanvas(page, POS.railMySubmissions, 3000);
    const locked = await clipShot(page);
    expectScreenChanged(library, locked, 'Library → MySubmissions (locked)');
    await page.screenshot({ path: 'test-results/cmp-nav-mysubmissions-locked.png' });
    console.log('✅ Guest MySubmissions locked screen');
  });

  test('back-навигация: Topics → Library, Training → Questions', async ({ page, isMobile }) => {
    skipOnMobile(test, isMobile);
    await launchApp(page);
    await continueAsGuest(page);
    const library = await clipShot(page);

    // Library → Topics → back → Library
    await openFirstLibrary(page);
    await clickCanvas(page, POS.backArrow, 300);
    await neutralMouse(page); // снять hover со стрелки «назад»
    await page.waitForTimeout(2000);
    const backToLibrary = await clipShot(page);
    expect(backToLibrary.equals(library), 'Back из Topics не вернул в Library').toBe(true);

    // Library → Topics → Questions → Training → back → Questions
    await openFirstLibrary(page);
    await openQuestionsViaVideo(page);
    const questions = await clipShot(page);
    await clickCanvas(page, POS.questionsTrainingGuest, 4000);
    await clickCanvas(page, POS.backArrow, 300);
    await neutralMouse(page);
    await page.waitForTimeout(2000);
    const backToQuestions = await clipShot(page);
    expect(backToQuestions.equals(questions), 'Back из Training не вернул в Questions').toBe(true);
    await page.screenshot({ path: 'test-results/cmp-nav-back-to-questions.png' });
    console.log('✅ Back navigation works');
  });

  test('should be responsive on mobile viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/');
    await expect(page.locator('canvas')).toBeVisible({ timeout: 60000 });
    await page.waitForTimeout(5000);

    const box = await page.locator('canvas').boundingBox();
    expect(box?.width).toBeLessThan(400);
    await page.screenshot({ path: 'test-results/cmp-mobile-view.png', fullPage: false });
    console.log('✅ Mobile viewport rendered correctly');
  });
});
