// Калибровка координат 1280x720 под guest-first флоу (MVP-1), итерация 2.
const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, 'test-results', 'calibrate');

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1280, height: 720 } });
  page.on('response', r => { if (r.url().includes('/api/') && r.status() >= 400) console.log(`[api ${r.status()}] ${r.url()}`); });

  await page.goto('http://localhost:8081', { waitUntil: 'load', timeout: 60000 });
  await page.waitForSelector('canvas', { timeout: 90000 });
  await page.waitForTimeout(12000);
  const shot = (name) => page.screenshot({ path: path.join(OUT, name + '.png') });
  const canvas = page.locator('canvas');
  const click = (x, y, ms = 1500) => canvas.click({ position: { x, y } }).then(() => page.waitForTimeout(ms));

  // Onboarding: 3 слайда → «Начать» → Library
  await click(640, 648, 1200);
  await click(640, 648, 1200);
  await click(640, 648, 5000);
  await shot('04-library-guest');

  // Profile → GuestProfileStub → «Войти» (accent-ссылка)
  await click(1065, 670, 3000);
  await shot('05-profile-guest');
  await click(705, 456, 2500);
  await shot('06-login');

  // Логин admin: Email (175), Пароль (285), «Войти» (353)
  await click(640, 175, 500);
  await page.keyboard.type('admin@sotospeak.com', { delay: 15 });
  await click(640, 285, 500);
  await page.keyboard.type('admin123', { delay: 15 });
  await shot('07-login-filled');
  await click(640, 353, 8000);
  await shot('08-after-login');

  // Library → Topics (seed «Разговорный английский» — 2-я карточка, y≈240)
  await click(640, 240, 4000);
  await shot('09-topics');
  // Topics → Video (первый топик)
  await click(640, 115, 4000);
  await shot('10-video');
  // Video → Questions: error-стаб плеера → кнопка «К вопросам» (707,441)
  await click(707, 441, 4000);
  await shot('11-questions-gate');

  await browser.close();
  console.log('calibration done');
})().catch(e => { console.error(e); process.exit(1); });
