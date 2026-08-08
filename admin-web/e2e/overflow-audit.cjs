// Аудит переполнений: элементы, вылезающие за границы viewport/родителя (admin-web)
const { chromium } = require('@playwright/test');
const PAGES = ['/', '/speaking/libraries', '/speaking/topics', '/speaking/questions', '/grading', '/users', '/analytics', '/logs', '/settings'];
const WIDTHS = [1280, 768, 390];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  const issues = [];
  for (const w of WIDTHS) {
    await page.setViewportSize({ width: w, height: 844 });
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle', timeout: 30000 }).catch(() => {});
    if (w === WIDTHS[0]) {
      await page.fill('input[type="email"]', 'admin@sotospeak.com');
      await page.fill('input[type="password"]', 'admin123');
      await page.click('button[type="submit"]');
      await page.waitForTimeout(2500);
    }
    for (const path of PAGES) {
      await page.goto('http://localhost:5173' + path, { waitUntil: 'networkidle', timeout: 30000 }).catch(() => {});
      await page.waitForTimeout(800);
      const res = await page.evaluate(() => {
        const out = { docOverflow: document.documentElement.scrollWidth - document.documentElement.clientWidth, elems: [] };
        const vw = document.documentElement.clientWidth;
        for (const el of document.querySelectorAll('body *')) {
          const r = el.getBoundingClientRect();
          if (r.width === 0 || r.height === 0) continue;
          const style = getComputedStyle(el);
          if (style.position === 'fixed') continue;
          // вылез за правый край viewport более чем на 2px
          if (r.right > vw + 2) {
            const tag = el.tagName.toLowerCase() + (el.className && typeof el.className === 'string' ? '.' + el.className.split(' ').slice(0, 2).join('.') : '');
            const text = (el.innerText || '').slice(0, 40).replace(/\n/g, ' ');
            out.elems.push(`${tag} right=${Math.round(r.right)} text="${text}"`);
          }
        }
        out.elems = out.elems.slice(0, 8);
        return out;
      });
      if (res.docOverflow > 2 || res.elems.length) {
        issues.push(`[${w}px] ${path}: docOverflow=${res.docOverflow}px` + (res.elems.length ? ' :: ' + res.elems.join(' | ') : ''));
      }
    }
  }
  console.log(issues.length ? issues.join('\n') : 'NO OVERFLOW ISSUES');
  await browser.close();
})();
