import { Page, expect } from '@playwright/test';

/**
 * Хелперы для WASM E2E speaking-приложения.
 *
 * CMP 1.7.1 (wasmJs) рендерит всё в единственный <canvas>:
 * семантика/testTag'и в DOM НЕ экспонируются (проверено пробником 2026-08-01).
 * Поэтому тесты — координатные клики + скриншоты + assertion'ы:
 *  - смена экрана = изменение пикселей clipped-региона;
 *  - отсутствие критических console.error и HTTP 5xx к /api.
 *
 * Все координаты откалиброваны под viewport 1280x720 (проект chromium)
 * ПОСЛЕ guest-first редизайна (2026-08-01, bd FunnyEnglish-pyt,
 * скрипты shoot-calibrate*.js):
 *  - онбординг: 3 слайда, «Далее»×2 → «Начать» → Library (гость по умолчанию,
 *    экрана выбора режима «Как начнём?» БОЛЬШЕ НЕТ);
 *  - Login/Register доступны из гостевого профиля и SpeakingGate;
 *  - bottom-sheet выбора субтитров удалён (DC-5): Topics → сразу Video;
 *  - на wasm видео не играет (стаб плеера) — error-плашка с кнопкой «К вопросам».
 * На мобильном проекте координатные тесты скипаются (см. skipOnMobile).
 */

export const VP = { width: 1280, height: 720 } as const;

/** Калиброванные позиции кликов (viewport 1280x720) */
export const POS = {
  onboardingNext: { x: 640, y: 648 },        // «Далее»/«Начать» на слайдах онбординга
  guestProfileRegister: { x: 640, y: 399 },  // «Зарегистрироваться» в гостевом профиле
  guestProfileLoginLink: { x: 705, y: 456 }, // «Уже есть аккаунт? Войти» (accent) в гостевом профиле
  loginEmail: { x: 640, y: 175 },
  loginPassword: { x: 640, y: 285 },
  loginSubmit: { x: 640, y: 353 },
  loginToRegisterLink: { x: 685, y: 449 },   // «Нет аккаунта? Регистрация» на Login
  registerToLoginLink: { x: 705, y: 552 },   // «Уже есть аккаунт? Войти» на Register
  firstLibrary: { x: 640, y: 240 },          // seed «Разговорный английский» (2-я карточка после E2E-библиотек)
  firstTopic: { x: 640, y: 115 },
  backArrow: { x: 26, y: 33 },
  videoErrorToQuestions: { x: 707, y: 441 }, // «К вопросам» в error-стабе плеера (wasm)
  questionsTrainingGuest: { x: 640, y: 340 },// «Тренировка · 3 попытки» (гость — выше, гейт снизу)
  questionsTrainingAuth: { x: 640, y: 607 }, // «Тренировка · 3 попытки» (авторизованный)
  questionsPracticeAuth: { x: 640, y: 676 }, // «Практика · 30 сек» (авторизованный)
  gateRegister: { x: 640, y: 624 },          // «Зарегистрироваться» в SpeakingGate (Questions)
  gateLogin: { x: 640, y: 680 },             // «Войти» в SpeakingGate (Questions)
  bottomNavLibrary: { x: 210, y: 670 },
  bottomNavMySubmissions: { x: 640, y: 670 },// «Отправки»
  bottomNavProfile: { x: 1065, y: 670 },
} as const;

/** Клип-регион заголовка экрана (для diff-assertion'ов): только текст заголовка,
 *  БЕЗ back-стрелки — иначе hover-состояние стрелки ломает pixel-equality */
export const TITLE_CLIP = { x: 55, y: 15, width: 180, height: 40 };

/** Полный скриншот для diff-assertion'ов «экран изменился» */
export async function fullShot(page: Page): Promise<Buffer> {
  return page.screenshot();
}

/** Увести курсор в нейтральную зону (снять hover перед pixel-compare) */
export async function neutralMouse(page: Page) {
  await page.mouse.move(640, 450);
  await page.waitForTimeout(300);
}

/** Пропустить координатный тест вне desktop-проекта chromium */
export function skipOnMobile(test: any, isMobile: boolean | undefined) {
  test.skip(!!isMobile, 'Координатные клики откалиброваны только под 1280x720 (chromium)');
}

/** Загрузить приложение и дождаться рендера (Splash → первый экран) */
export async function launchApp(page: Page) {
  await page.goto('/');
  await expect(page.locator('canvas')).toBeVisible({ timeout: 60000 });
  await page.waitForTimeout(10000); // Splash + инициализация
}

export async function clickCanvas(page: Page, pos: { x: number; y: number }, settleMs = 1000) {
  await page.locator('canvas').click({ position: pos });
  await page.waitForTimeout(settleMs);
}

/** Скриншот клипа как Buffer для сравнения */
export async function clipShot(page: Page, clip = TITLE_CLIP): Promise<Buffer> {
  return page.screenshot({ clip });
}

/** Assertion: экран изменился (пиксели клипа отличаются) */
export function expectScreenChanged(before: Buffer, after: Buffer, what: string) {
  expect(before.equals(after), `Экран не изменился после перехода: ${what}`).toBe(false);
}

/** Сбор критических ошибок консоли (шум source map/favicon/HMR фильтруем) */
export function collectCriticalErrors(page: Page): string[] {
  const errors: string[] = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      const t = msg.text();
      if (!t.includes('source map') && !t.includes('favicon') && !t.includes('DevTools')) {
        errors.push(t);
      }
    }
  });
  return errors;
}

/** Сбор HTTP 5xx ответов backend'а */
export function collectServerErrors(page: Page): string[] {
  const failures: string[] = [];
  page.on('response', r => {
    if (r.url().includes('/api/') && r.status() >= 500) {
      failures.push(`${r.status()} ${r.url()}`);
    }
  });
  return failures;
}

/** Онбординг: 3 слайда («Далее»×2 → «Начать») → Library.
 *  Гость-first: гостевая сессия стартует синхронно, выбора режима нет. */
export async function passOnboarding(page: Page) {
  await clickCanvas(page, POS.onboardingNext, 800);  // слайд 1 → 2
  await clickCanvas(page, POS.onboardingNext, 800);  // слайд 2 → 3
  await clickCanvas(page, POS.onboardingNext, 5000); // «Начать» → Library
}

/** Первый запуск → Library в гостевом режиме (guest-first: гость по умолчанию) */
export async function continueAsGuest(page: Page) {
  await passOnboarding(page);
}

/** Library → Профиль → гостевой профиль → «Войти» → Login */
export async function openLoginScreen(page: Page) {
  await passOnboarding(page);
  await clickCanvas(page, POS.bottomNavProfile, 3000);      // гостевой профиль
  await clickCanvas(page, POS.guestProfileLoginLink, 2500); // «Войти»
}

/** Library → Профиль → «Зарегистрироваться» → Register */
export async function openRegisterScreen(page: Page) {
  await passOnboarding(page);
  await clickCanvas(page, POS.bottomNavProfile, 3000);
  await clickCanvas(page, POS.guestProfileRegister, 2500);
}

/** Логин администратором dev-стека (docker-compose.yml) */
export async function loginAsAdmin(page: Page) {
  const email = process.env.E2E_USER_EMAIL || 'admin@sotospeak.com';
  const password = process.env.E2E_USER_PASSWORD || 'admin123';
  await clickCanvas(page, POS.loginEmail, 500);
  await page.keyboard.type(email, { delay: 15 });
  await clickCanvas(page, POS.loginPassword, 500);
  await page.keyboard.type(password, { delay: 15 });
  await clickCanvas(page, POS.loginSubmit, 100);
}

/** Library → Topics (seed «Разговорный английский») */
export async function openFirstLibrary(page: Page) {
  await clickCanvas(page, POS.firstLibrary, 4000);
}

/** Topics → первый топик → Video (error-стаб плеера на wasm) → «К вопросам» → Questions */
export async function openQuestionsViaVideo(page: Page) {
  await clickCanvas(page, POS.firstTopic, 3000);
  await clickCanvas(page, POS.videoErrorToQuestions, 4000);
}
