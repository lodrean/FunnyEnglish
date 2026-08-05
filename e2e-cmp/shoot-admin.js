// Скриншот реального Grading inbox в admin-web (docker :3000)
const { chromium } = require('@playwright/test');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1400, height: 950 } });
  await page.goto('http://localhost:3000', { waitUntil: 'networkidle', timeout: 30000 });
  // Логин
  const email = page.locator('input[type="email"], input[name="email"]').first();
  if (await email.isVisible({ timeout: 5000 }).catch(() => false)) {
    await email.fill('admin@sotospeak.com');
    await page.locator('input[type="password"]').first().fill('admin123');
    await page.locator('button[type="submit"], button:has-text("Войти"), button:has-text("Sign in"), button:has-text("Login")').first().click();
    await page.waitForTimeout(4000);
  }
  await page.screenshot({ path: '../docs/qa/design-conformance/admin-after-login.png' });

  // Grading inbox — через sidebar
  const gradingLink = page.locator('text=Grading').first();
  if (await gradingLink.isVisible({ timeout: 5000 }).catch(() => false)) {
    await gradingLink.click();
    await page.waitForTimeout(4000);
  }
  await page.screenshot({ path: '../docs/qa/design-conformance/admin-grading.png' });
  await page.locator('button:has-text("Review")').first().click(); await page.waitForTimeout(4000); await page.screenshot({ path: '../docs/qa/design-conformance/admin-grading-review.png' }); console.log('done', page.url());
  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
