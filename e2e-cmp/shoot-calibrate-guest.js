// Калибровка гостевого пути до SpeakingGate на QuestionsScreen (1280x720).
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

  // Onboarding → Library (guest)
  await click(640, 648, 1200);
  await click(640, 648, 1200);
  await click(640, 648, 5000);
  // Library → seed «Разговорный английский» (2-я карточка) → Topics → 1-й топик → Video
  await click(640, 240, 4000);
  await click(640, 115, 4000);
  // Video error-стаб → «К вопросам»
  await click(707, 441, 4000);
  await shot('20-questions-guest-gate');
  // «Войти» (ghost) в гейте → Login
  await click(640, 676, 2500);
  await shot('21-after-gate-login-click');

  await browser.close();
  console.log('guest calibration done');
})().catch(e => { console.error(e); process.exit(1); });
