const { chromium } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 700, height: 1000 } });
  const mockupsPath = path.join(__dirname, '..', '.docs', 'design-system', 'mockups.html');
  await page.goto('file:///' + mockupsPath.replace(/\\/g, '/'), { waitUntil: 'load' });
  await page.waitForTimeout(2000);

  const ids = (await page.evaluate(() =>
    Array.from(document.querySelectorAll('.frame-wrap[id]')).map(e => e.id)
  )).filter(i => i !== '__theme');

  console.log('frames:', ids.length);

  for (const theme of ['light', 'dark']) {
    const outDir = path.join(__dirname, 'test-results', 'pixel-report', `mockups-${theme}-phone`);
    fs.mkdirSync(outDir, { recursive: true });
    if (theme === 'dark') {
      await page.click('#themeToggle');
      await page.waitForTimeout(500);
    }
    for (const id of ids) {
      await page.evaluate((fid) => {
        document.querySelectorAll('.frame-wrap').forEach(f => { f.style.display = 'none'; f.classList.remove('active'); });
        const el = document.getElementById(fid);
        if (el) { el.style.display = 'flex'; el.classList.add('active'); }
        window.scrollTo(0, 0);
      }, id);
      await page.waitForTimeout(300);
      const phone = await page.locator(`#${id} .phone`).first();
      try {
        await phone.screenshot({ path: path.join(outDir, `${id}.png`) });
      } catch (e) {
        // fallback: screenshot whole frame
        await page.locator('#' + id).screenshot({ path: path.join(outDir, `${id}.png`) });
      }
    }
  }
  console.log('mockup phone frames done');
  await browser.close();
})();
