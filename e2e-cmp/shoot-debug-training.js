const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, 'test-results', 'debug-training');
const VP = { width: 390, height: 844 };
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

  // onboarding → library
  await click(195, 773, 800);
  await click(195, 773, 800);
  await click(195, 773, 5000);
  // library → topics → video → questions
  await click(195, 150, 4000);
  await click(195, 120, 4000);
  await click(195, 770, 4000);
  await shot('00-questions');

  // try clicking Training at different Y positions
  for (const y of [500, 520, 540, 560, 580]) {
    await click(195, y, 3000);
    await shot(`01-click-y${y}`);
    // go back if needed
    await page.goBack().catch(() => {});
    await page.waitForTimeout(2000);
  }

  await browser.close();
  console.log('DONE');
}
main().catch(e => { console.error(e); process.exit(1); });
