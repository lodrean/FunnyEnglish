// Дизайн-прогон v2 (после A/B + DC-2..DC-5): onboarding → guest-first → все экраны
const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, '..', 'docs', 'qa', 'design-conformance');

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 390, height: 844 } });
  page.on('response', r => { if (r.url().includes('/auth/')) console.log(`[auth ${r.status()}]`); });

  await page.goto('http://localhost:8081', { waitUntil: 'load', timeout: 60000 });
  await page.waitForSelector('canvas', { timeout: 90000 });
  await page.waitForTimeout(12000);
  const shot = (name) => page.screenshot({ path: path.join(OUT, 'app-' + name + '.png') });
  const canvas = page.locator('canvas');
  const click = (x, y, ms = 1500) => canvas.click({ position: { x, y } }).then(() => page.waitForTimeout(ms));

  // --- Onboarding ---
  await shot('onboarding-1');
  await click(195, 773, 800);
  await shot('onboarding-2');
  await click(195, 773, 800);
  await shot('onboarding-3');
  await click(195, 773, 5000); // «Начать» → Library (guest)

  // --- Library (guest) ---
  await shot('library');

  // --- Profile (гость) через bottom nav ---
  await click(325, 810, 3000);
  await shot('profile-guest');
  await click(65, 810, 3000); // назад на «Темы»

  // --- Topics ---
  await click(195, 150, 4000);
  await shot('topics');

  // --- Video ---
  await click(195, 120, 4000);
  await shot('video');

  // --- Questions (CTA «Перейти к вопросам» ~770) ---
  await click(195, 770, 4000);
  await shot('questions'); // гейт виден на самом экране (locked-зона внизу)

  // --- Training («Тренировка · 3 попытки» ~443) ---
  await click(195, 443, 4000);
  await shot('training');
  await click(30, 55, 2500); // back → Questions

  // --- Register (через гейт «Зарегистрироваться» ~750) ---
  await click(195, 750, 3000);
  await shot('register');

  // --- Login («Уже есть аккаунт? Войти» — пробуем ~700) ---
  await click(195, 700, 2500);
  await shot('login');

  // Логин admin (поля ~400/480, кнопка ~580 по текущему layout)
  await click(195, 220, 500);
  await page.keyboard.type('admin@sotospeak.com', { delay: 15 });
  await click(195, 300, 500);
  await page.keyboard.type('admin123', { delay: 15 });
  await click(195, 377, 8000); // «Войти»
  await shot('library-auth');

  // --- Profile (auth) ---
  await click(325, 810, 4000);
  await shot('profile');

  await browser.close();
  console.log('done');
})().catch(e => { console.error(e); process.exit(1); });
