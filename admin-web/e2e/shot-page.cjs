const { chromium } = require('@playwright/test');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1280, height: 844 } });
  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle' });
  await page.fill('input[type="email"]', 'admin@sotospeak.com');
  await page.fill('input[type="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await page.waitForTimeout(2500);
  for (const p of ['/users', '/speaking/topics']) {
    await page.goto('http://localhost:5173' + p, { waitUntil: 'networkidle' });
    await page.waitForTimeout(800);
    await page.screenshot({ path: 'test-results/overflow-' + p.replace(/\//g, '_') + '.png', fullPage: false });
  }
  await browser.close();
})();
