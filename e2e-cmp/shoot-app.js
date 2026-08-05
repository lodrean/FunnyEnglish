// Дизайн-прогон: login → Questions (auth) → Practice
const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, '..', 'docs', 'qa', 'design-conformance');

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 390, height: 844 } });
  page.on('response', r => { if (r.url().includes('/auth/login')) console.log(`[login ${r.status()}]`); });

  await page.goto('http://localhost:8081', { waitUntil: 'load', timeout: 60000 });
  await page.waitForSelector('canvas', { timeout: 90000 });
  await page.waitForTimeout(10000);
  const shot = (name) => page.screenshot({ path: path.join(OUT, 'app-' + name + '.png') });
  const canvas = page.locator('canvas');
  const click = (x, y, ms = 1000) => canvas.click({ position: { x, y } }).then(() => page.waitForTimeout(ms));

  await click(195, 575, 900);
  await click(195, 575, 900);
  await click(195, 640, 2500); // → Register
  await click(260, 705, 2500); // → Login
  await click(195, 457, 500);
  await page.keyboard.type('admin@sotospeak.com', { delay: 15 });
  await click(195, 537, 500);
  await page.keyboard.type('admin123', { delay: 15 });
  await click(195, 623, 6000); // Войти
  await shot('library-auth');

  await click(195, 195, 4000); // «Разговорный английский»
  await click(195, 130, 3000); // «Знакомство»
  await click(195, 788, 4000); // «Пропустить видео»
  await click(195, 800, 4000); // «Практика · 30 сек»
  await shot('practice');
  await shot('questions-auth');
  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
