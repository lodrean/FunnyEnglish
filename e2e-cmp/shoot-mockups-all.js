const { chromium } = require('@playwright/test');
const fs = require('fs');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 700, height: 1000 } });
  await page.goto('file:///C:/Users/etaba/IdeaProjects/projects/Packages/FunnyEnglish/.docs/design-system/mockups.html', { waitUntil: 'load' });
  await page.waitForTimeout(2000);
  const ids = (await page.evaluate(() => Array.from(document.querySelectorAll('.frame-wrap[id]')).map(e => e.id))).filter(i => i !== '__theme');
  console.log('frames:', ids.length);
  for (const theme of ['light', 'dark']) {
    fs.mkdirSync('test-results/pixel-report/mockups-' + theme, { recursive: true });
    if (theme === 'dark') { await page.click('#themeToggle'); await page.waitForTimeout(500); }
    for (const id of ids) {
      await page.evaluate((fid) => {
        document.querySelectorAll('.frame-wrap').forEach(f => { f.style.display = 'none'; f.classList.remove('active'); });
        const el = document.getElementById(fid);
        if (el) { el.style.display = 'flex'; el.classList.add('active'); }
        window.scrollTo(0, 0);
      }, id);
      await page.waitForTimeout(300);
      await page.locator('#' + id).screenshot({ path: `test-results/pixel-report/mockups-${theme}/${id}.png` });
    }
  }
  console.log('mockup frames done');
  await browser.close();
})();
