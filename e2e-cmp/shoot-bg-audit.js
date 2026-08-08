// Регрессия фона экранов (M3, 2026-08-07): обход ключевых экранов 1280x720
// в light и dark, pixel-ассерт фона (#EEF3FF / #161A2E, допуск ±4/канал).
// Запуск: node shoot-bg-audit.js  (CMP_URL по умолчанию http://192.168.1.148:8082)
const { chromium } = require('@playwright/test');
const { PNG } = require('pngjs');
const path = require('path');
const fs = require('fs');

const BASE = process.env.CMP_URL || 'http://192.168.1.148:8082';
const OUT = path.join(__dirname, 'test-results', 'bg-audit');
const VP = { width: 1280, height: 720 };

const EXPECTED = {
  light: [0xEE, 0xF3, 0xFF],
  dark: [0x16, 0x1A, 0x2E],
};
const TOL = 4;

// Точки сэмпла фона: левая полоса-зазор между rail и карточками (x=87),
// правый верхний угол, зазор между 1-й и 2-й карточками.
const SAMPLES = [
  [1272, 8],    // правый верхний угол
  [87, 200],    // зазор rail|контент
  [87, 500],    // зазор rail|контент ниже
  [640, 206],   // вертикальный зазор между карточками (Library)
];
// Экраны-исключения: video — чёрный плеер by design (mockup frame-video)
const BG_EXCEPTIONS = { video: [[0, 0, 0]] };

function px(png, x, y) {
  const i = (png.width * y + x) << 2;
  return [png.data[i], png.data[i + 1], png.data[i + 2]];
}
function close(a, b) { return Math.abs(a[0] - b[0]) <= TOL && Math.abs(a[1] - b[1]) <= TOL && Math.abs(a[2] - b[2]) <= TOL; }

const violations = [];

async function walk(page, theme, shot, click) {
  const S = (name) => shot(theme, name);

  // Onboarding 3 слайда
  await S('onboarding-1');
  await click(640, 648, 700); await S('onboarding-2');
  await click(640, 648, 700); await S('onboarding-3');
  await click(640, 648, 6000); // «Начать» → Library
  await S('library');

  // Topics (seed — первая карточка после purge)
  await click(640, 140, 3500); await S('topics');
  // Video (error-стаб на wasm) — ждём появления error-UI (seed-видео грузится дольше)
  await click(640, 115, 8000); await S('video');
  // «К вопросам» → Questions (с retry: error-стаб может появиться с задержкой)
  await click(711, 445, 4000);
  let q = await page.screenshot();
  let qpng = PNG.sync.read(q);
  if (px(qpng, 640, 206)[0] < 10) { // всё ещё чёрный плеер — ждём и кликаем ещё раз
    await page.waitForTimeout(5000);
    await click(711, 445, 4000);
  }
  await S('questions');
  // Training (гость)
  await click(640, 285, 4000); await S('training');
  // назад на Questions → rail MySubmissions (locked) → rail Profile (guest)
  await click(40, 360, 3000); await S('mysubmissions-locked');
  await click(40, 425, 3000); await S('profile-guest');
  // «Уже есть аккаунт? Войти» → Login
  await click(743, 499, 2500); await S('login');
  // «Регистрация» → Register
  await click(690, 413, 2500); await S('register');
}

async function pass(theme) {
  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: VP, colorScheme: theme });
  const page = await context.newPage();

  const canvas = page.locator('canvas');
  const click = async (x, y, settle = 1000) => { await canvas.click({ position: { x, y } }); await page.waitForTimeout(settle); };
  const shot = async (t, name) => {
    await page.mouse.move(640, 60); await page.waitForTimeout(150);
    const buf = await page.screenshot();
    const dir = path.join(OUT, t);
    fs.mkdirSync(dir, { recursive: true });
    fs.writeFileSync(path.join(dir, name + '.png'), buf);
    const png = PNG.sync.read(buf);
    const exp = EXPECTED[t];
    const allowed = [exp, ...(BG_EXCEPTIONS[name] || [])];
    // Фон считается присутствующим, если ХОТЯ БЫ 2 сэмпла матчат разрешённые цвета
    // (часть точек может попадать на карточки/контент — это нормально)
    const hits = SAMPLES.filter(([x, y]) => {
      const c = px(png, x, y);
      return allowed.some(a => close(c, a));
    }).length;
    if (hits < 2) {
      const detail = SAMPLES.map(([x, y]) => `(${x},${y})=#${px(png, x, y).map(v => v.toString(16).padStart(2, '0')).join('')}`).join(' ');
      violations.push(`${t}/${name}: ${detail}`);
      console.log(`BG-FAIL ${t}/${name}: ${detail}`);
    } else {
      console.log(`ok ${t}/${name}`);
    }
  };

  await page.goto(BASE, { waitUntil: 'load', timeout: 60000 });
  await canvas.waitFor({ state: 'visible', timeout: 60000 });
  await page.waitForTimeout(10000);
  await walk(page, theme, shot, click);
  await browser.close();
}

(async () => {
  await pass('light');
  await pass('dark');
  console.log('\n=== BG AUDIT ===');
  if (violations.length) {
    console.log('VIOLATIONS:\n' + violations.join('\n'));
    process.exit(1);
  }
  console.log('ALL SCREENS OK');
})();
