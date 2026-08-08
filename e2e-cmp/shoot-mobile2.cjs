const { chromium } = require('@playwright/test');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 390, height: 844 } });
  const canvas = page.locator('canvas');
  const click = async (x, y, s = 1200) => { await canvas.click({ position: { x, y } }); await page.waitForTimeout(s); };
  const shot = async (n) => { await page.mouse.move(195, 60); await page.waitForTimeout(150); await page.screenshot({ path: 'test-results/mobile-audit/' + n + '.png' }); console.log('shot', n); };
  await page.goto('http://192.168.1.148:8082', { waitUntil: 'load', timeout: 60000 });
  await canvas.waitFor({ state: 'visible', timeout: 60000 });
  await page.waitForTimeout(10000);
  await click(195, 774, 800); await click(195, 774, 800); await click(195, 774, 5000);
  await click(195, 150, 3500);            // seed library card
  await click(195, 115, 8000);            // topic «Знакомство» → video
  await click(260, 445, 5000);            // «К вопросам»
  await shot('05-questions');
  await click(325, 800, 3000);            // bottom nav «Профиль»
  await shot('07-profile-guest');
  await click(190, 800, 3000);            // bottom nav «Отправки»
  await shot('08-submissions-locked');
  await browser.close();
})();
