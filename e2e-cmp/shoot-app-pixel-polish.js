const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, 'test-results', 'pixel-report', 'app');
const VP = { width: 360, height: 800 };
const BASE = process.env.CMP_URL || 'http://localhost:8081';

async function main() {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: VP });
  page.on('console', msg => console.log('console:', msg.text().slice(0, 200)));
  page.on('pageerror', err => console.log('pageerror:', err.message));

  const canvas = page.locator('canvas');
  const click = async (x, y, settle = 2000) => {
    await canvas.click({ position: { x, y } });
    await page.waitForTimeout(settle);
  };
  const shot = async (name) => {
    await page.screenshot({ path: path.join(OUT, name + '.png') });
    console.log('shot:', name);
  };

  await page.goto(BASE, { waitUntil: 'load', timeout: 60000 });
  await canvas.waitFor({ state: 'visible', timeout: 60000 });
  await page.waitForTimeout(12000);

  // Onboarding
  await shot('onboarding-1');
  await click(180, 733, 800);
  await shot('onboarding-2');
  await click(180, 733, 800);
  await shot('onboarding-3');
  await click(180, 733, 5000); // Начать → Library

  // Library
  await shot('library');

  // Topics
  await click(180, 140, 4000);
  await shot('topics');

  // Video
  await click(180, 110, 4000);
  await shot('video');

  // Questions
  await click(180, 730, 4000);
  await shot('questions');

  // Register (through locked gate)
  await click(180, 700, 3000);
  await shot('register');

  // Login (ссылка «Уже есть аккаунт? Войти» — реальная позиция ~242,512 на 360x800)
  await click(242, 512, 2500);
  await shot('login');

  // Login as admin
  await click(180, 200, 500);
  await page.keyboard.type('admin@sotospeak.com', { delay: 10 });
  await click(180, 280, 500);
  await page.keyboard.type('admin123', { delay: 10 });
  await click(180, 360, 6000);
  await shot('library-auth');

  // Profile auth
  await click(300, 760, 4000);
  await shot('profile');

  // MySubmissions
  await click(180, 760, 4000);
  await shot('submissions');

  await browser.close();
  console.log('DONE');
}
main().catch(e => { console.error(e); process.exit(1); });
