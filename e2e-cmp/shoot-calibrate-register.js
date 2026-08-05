// Калибровка Register-экрана (1280x720): гостевой профиль → «Зарегистрироваться» → Register → «Войти».
const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, 'test-results', 'calibrate');

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1280, height: 720 } });

  await page.goto('http://localhost:8081', { waitUntil: 'load', timeout: 60000 });
  await page.waitForSelector('canvas', { timeout: 90000 });
  await page.waitForTimeout(12000);
  const shot = (name) => page.screenshot({ path: path.join(OUT, name + '.png') });
  const canvas = page.locator('canvas');
  const click = (x, y, ms = 1500) => canvas.click({ position: { x, y } }).then(() => page.waitForTimeout(ms));

  // Onboarding → Library → Profile → «Зарегистрироваться»
  await click(640, 648, 1200);
  await click(640, 648, 1200);
  await click(640, 648, 5000);
  await click(1065, 670, 3000);
  await click(640, 399, 2500);
  await shot('30-register');
  // «Уже есть аккаунт? Войти» на Register — пробуем y≈449
  await click(705, 449, 2500);
  await shot('31-register-to-login');

  await browser.close();
  console.log('register calibration done');
})().catch(e => { console.error(e); process.exit(1); });
