// Скриншоты всех фреймов демо-макетов дизайн-системы
const { chromium } = require('@playwright/test');
const path = require('path');
const fs = require('fs');

const OUT = path.join(__dirname, '..', 'docs', 'qa', 'design-conformance');
const FRAMES = ['frame-library', 'frame-video', 'frame-training', 'frame-practice', 'frame-grading', 'frame-onboarding', 'frame-login', 'frame-register', 'frame-locked', 'frame-profile', 'frame-profile-guest'];

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1400, height: 950 } });
  const url = 'file://' + path.resolve(__dirname, '..', '.docs', 'design-system', 'mockups.html').replace(/\\/g, '/');
  await page.goto(url, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1500);

  for (const id of FRAMES) {
    await page.evaluate((fid) => {
      document.querySelectorAll('.frame-wrap').forEach(f => f.classList.remove('active'));
      document.getElementById(fid)?.classList.add('active');
    }, id);
    await page.waitForTimeout(400);
    const el = page.locator('#' + id);
    await el.screenshot({ path: path.join(OUT, 'mockup-' + id.replace('frame-', '') + '.png') });
    console.log('✅', id);
  }
  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
