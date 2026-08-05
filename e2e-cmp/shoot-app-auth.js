// Дизайн-прогон: auth-флоу (gate → register → login → auth library/profile)
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

  // Onboarding → Library
  await click(195, 773, 800);
  await click(195, 773, 800);
  await click(195, 773, 5000);

  // Library → Topics («Разговорный английский» — 2-я карточка ~280)
  await click(195, 280, 4000);
  // Topics → Video (1-й топик)
  await click(195, 120, 4000);
  // Video → Questions
  await click(195, 770, 4000);
  await shot('questions');

  // Gate → Register («Зарегистрироваться» ~750)
  await click(195, 750, 3000);
  await shot('register');

  // Register → Login («Уже есть аккаунт? Войти» ~572)
  await click(240, 572, 2500);
  await shot('login');

  // Логин admin (Email ~205, Пароль ~283, «Войти» ~377)
  await click(195, 205, 500);
  await page.keyboard.type('admin@sotospeak.com', { delay: 15 });
  await click(195, 283, 500);
  await page.keyboard.type('admin123', { delay: 15 });
  await click(195, 377, 8000);
  await shot('library-auth');

  // Profile (auth)
  await click(325, 810, 4000);
  await shot('profile');

  await browser.close();
  console.log('done');
})().catch(e => { console.error(e); process.exit(1); });
