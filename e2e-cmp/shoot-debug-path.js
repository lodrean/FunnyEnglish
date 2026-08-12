const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, 'test-results', 'debug-path');
const VP = { width: 390, height: 844 };
const BASE = process.env.CMP_URL || 'http://localhost:8081';

async function main() {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: VP });
  page.on('console', msg => console.log('console:', msg.text().slice(0, 200)));
  page.on('pageerror', err => console.log('pageerror:', err.message));

  const canvas = page.locator('canvas');
  const click = async (x, y, settle = 2000, name) => {
    await canvas.click({ position: { x, y } });
    await page.waitForTimeout(settle);
    if (name) {
      await page.screenshot({ path: path.join(OUT, name + '.png') });
      console.log('shot:', name, 'after click', x, y);
    }
  };
  const shot = async (name) => {
    await page.screenshot({ path: path.join(OUT, name + '.png') });
    console.log('shot:', name);
  };

  await page.goto(BASE, { waitUntil: 'load', timeout: 60000 });
  await canvas.waitFor({ state: 'visible', timeout: 60000 });
  await page.waitForTimeout(12000); // splash + init

  // onboarding: click "Далее" 3 times then "Начать"
  await shot('00-onboarding-1');
  await click(195, 773, 1000, '01-onboarding-2');
  await click(195, 773, 1000, '02-onboarding-3');
  await click(195, 773, 5000, '03-library');

  // click first library card (top area)
  await click(195, 150, 4000, '04-topics');

  // click first topic
  await click(195, 120, 4000, '05-video');

  // click "Перейти к вопросам"
  await click(195, 770, 4000, '06-questions');

  // click "Тренировка · 3 попытки"
  await click(195, 550, 4000, '07-training');

  // click rec button center
  await click(195, 600, 2000, '08-training-recording');

  await browser.close();
  console.log('DONE');
}
main().catch(e => { console.error(e); process.exit(1); });
