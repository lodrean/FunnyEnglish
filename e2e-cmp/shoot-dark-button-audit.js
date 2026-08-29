// Аудит dark primary-кнопки (bd FunnyEnglish-2oz.2, tokens.css v1.3.1):
// пара «dark-кнопка мокапа ↔ dark-кнопка приложения».
// Мокап: file:// .docs/design-system/mockups.html + data-theme="dark".
// Приложение: статическая раздача composeApp/build/wasm-dist (свежий бандл — ./gradlew :composeApp:wasmJsBrowserDistribution),
// тема — prefers-color-scheme: dark (AppThemeMode.SYSTEM). Backend не нужен: onboarding/guest-first — локальные.
const { chromium } = require('@playwright/test');
const http = require('http');
const path = require('path');
const fs = require('fs');

const ROOT = path.join(__dirname, '..');
const OUT = path.join(ROOT, 'docs', 'qa', 'design-conformance');
const DIST = path.join(ROOT, 'composeApp', 'build', 'wasm-dist');
const PORT = 8093;

const MIME = {
  '.html': 'text/html', '.js': 'text/javascript', '.map': 'application/json',
  '.wasm': 'application/wasm', '.ttf': 'font/ttf', '.otf': 'font/otf',
  '.png': 'image/png', '.svg': 'image/svg+xml', '.json': 'application/json',
  '.css': 'text/css', '.m4a': 'audio/mp4', '.vtt': 'text/vtt', '.mp4': 'video/mp4',
};

function serve() {
  return new Promise((resolve) => {
    const srv = http.createServer((req, res) => {
      const urlPath = decodeURIComponent(req.url.split('?')[0]);
      let file = path.join(DIST, urlPath === '/' ? 'index.html' : urlPath);
      if (!file.startsWith(DIST) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) {
        file = path.join(DIST, 'index.html'); // SPA fallback
      }
      res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' });
      fs.createReadStream(file).pipe(res);
    });
    srv.listen(PORT, () => resolve(srv));
  });
}

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();

  // --- 1. Мокап, dark: primary-кнопки frame-onboarding («Начать») и frame-questions («Тренировка») ---
  const mock = await browser.newPage({ viewport: { width: 1400, height: 950 } });
  const url = 'file://' + path.resolve(ROOT, '.docs', 'design-system', 'mockups.html').replace(/\\/g, '/');
  await mock.goto(url, { waitUntil: 'networkidle' });
  await mock.waitForTimeout(1200);
  await mock.evaluate(() => document.documentElement.setAttribute('data-theme', 'dark'));
  for (const id of ['frame-onboarding', 'frame-questions']) {
    await mock.evaluate((fid) => {
      document.querySelectorAll('.frame-wrap').forEach(f => f.classList.remove('active'));
      document.getElementById(fid)?.classList.add('active');
    }, id);
    await mock.waitForTimeout(400);
    await mock.locator('#' + id).screenshot({ path: path.join(OUT, 'mockup-dark-' + id.replace('frame-', '') + '.png') });
    console.log('✅ mockup dark', id);
  }
  await mock.close();

  // --- 2. Приложение (wasm-dist), dark: onboarding «Начать» (primary filled) ---
  const srv = await serve();
  const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, colorScheme: 'dark' });
  const page = await ctx.newPage();
  await page.goto(`http://localhost:${PORT}`, { waitUntil: 'load', timeout: 60000 });
  await page.waitForSelector('canvas', { timeout: 90000 });
  await page.waitForTimeout(12000); // первый кадр Compose + шрифты
  await page.screenshot({ path: path.join(OUT, 'wasm-dark-onboarding.png') });
  console.log('✅ app dark onboarding');
  await ctx.close();
  srv.close();

  await browser.close();
})().catch(e => { console.error(e); process.exit(1); });
